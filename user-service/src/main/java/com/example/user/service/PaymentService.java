package com.example.user.service;

import com.example.user.constant.TransactionStatus;
import com.example.user.constant.TransactionType;
import com.example.user.dto.*;
import com.example.user.entity.Transaction;
import com.example.user.entity.User;
import com.example.user.exception.NotEnoughFundsException;
import com.example.user.exception.UserNotFoundException;
import com.example.user.repository.TransactionRepository;
import com.example.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
    private final UserRepository userRepository;
    private final CommonMethodsService commonMethodsService;
    private final WebClient.Builder webClient;
    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, TransactionEvent> transactionTemplate;

//    @Transactional
//    public void sendPaymentToUser(Integer userId,
//                                  UserDetails userDetails,
//                                  P2pPaymentRequest request) {
//
//        User sender =
//                commonMethodsService.getUserIfItIsAuthorizedAccess
//                        (userId, userDetails);
//        log.debug("Sender: {}", sender.getId());
//        User recipient =
//                userRepository.findById(request.receiverId())
//                        .orElseThrow(() ->
//                                new UserNotFoundException(request.receiverId())
//                );
//        log.debug("Recipient: {}", recipient.getId());
//
//        BigDecimal senderBalance = getCardBalance(request.senderCardId());
//
//        checkIfFundsEnoughToPay(senderBalance, request.amount());
//
//        log.debug("Balance is checked");
//        deductMoneyFromSenderBalance(request.senderCardId(), request.amount());
//        log.debug("Deducted from sender balance: {}", sender.getId());
//
//        addMoneyToReceiverBalance(request.receiverCardId(), request.amount());
//        log.debug("Added money to receiver balance: {}", recipient.getId());
//
//        Transaction transaction =
//                Transaction.builder()
//                        .sender(sender)
//                        .recipientId(recipient.getId())
//                        .senderCardId(request.senderCardId())
//                        .recipientCardId(request.receiverCardId())
//                        .amount(request.amount())
//                        .status(TransactionStatus.COMPLETED)
//                        .description("p2p payment")
//                        .transactionDate(new Date())
//                        .type(TransactionType.P2P_PAYMENT)
//                        .build();
//
//        transactionRepository.save(transaction);
//        log.debug("Transaction created and saved: {}", transaction);
//
//    }

    @Transactional
    public void sendPayment(Integer userId, UserDetails userDetails, PaymentRequest request) {
        User sender = commonMethodsService.getUserIfItIsAuthorizedAccess(userId, userDetails);
        log.debug("Sender: {}", sender.getId());

        BigDecimal senderBalance = getCardBalance(request.senderCardId());
        checkIfFundsEnoughToPay(senderBalance, request.amount());
        log.debug("Balance is checked");

        deductMoneyFromSenderBalance(request.senderCardId(), request.amount());
        log.debug("Deducted from sender balance: {}", sender.getId());


        log.debug("Creating transaction");
        Transaction.TransactionBuilder transactionBuilder = Transaction.builder()
                .sender(sender)
                .senderCardId(request.senderCardId())
                .amount(request.amount())
                .status(TransactionStatus.COMPLETED)
                .transactionDate(new Date());

        log.debug("Creating transaction event");
        TransactionEvent. TransactionEventBuilder transactionEventBuilder =
                TransactionEvent.builder()
                        .senderEmail(sender.getEmail())
                        .amount(request.amount())
                        .sendCardId(request.senderCardId())
                        .eventDate(new Date());

        if (request instanceof ServicePaymentRequest serviceRequest) {

            ResponseEntity<ServiceDto> serviceDtoResponse = webClient.build()
                    .get()
                    .uri("http://service-service/{serviceId}", serviceRequest.serviceId())
                    .retrieve()
                    .toEntity(ServiceDto.class)
                    .block();

            ServiceDto service = serviceDtoResponse.getBody();
            log.debug("Found service: {}", service);


            transactionBuilder
                    .recipientId(service.id())
                    .recipientCardId(serviceRequest.accountId())
                    .description(service.description())
                    .type(TransactionType.SERVICE_PAYMENT);

            transactionEventBuilder
                    .transactionType(TransactionType.SERVICE_PAYMENT.name())
                    .receiverEmail(service.name());//name of service which is paid

        } else if (request instanceof P2pPaymentRequest p2pRequest) {


            User recipient = userRepository.findById(p2pRequest.receiverId())
                    .orElseThrow(() -> new UserNotFoundException(p2pRequest.receiverId()));
            log.debug("Recipient: {}", recipient.getId());

            addMoneyToReceiverBalance(p2pRequest.receiverCardId(), request.amount());
            log.debug("Added money to receiver balance: {}", recipient.getId());

            log.debug("Continue creating transaction for p2p");
            transactionBuilder
                    .recipientId(recipient.getId())
                    .recipientCardId(p2pRequest.receiverCardId())
                    .description("p2p payment")
                    .type(TransactionType.P2P_PAYMENT);
            log.debug("Continue creating transaction event for p2p");
            transactionEventBuilder
                    .transactionType(TransactionType.P2P_PAYMENT.name())
                    .receiverEmail(recipient.getEmail())
                    .receiverCardId(p2pRequest.receiverCardId());

        }

        Transaction transaction = transactionBuilder.build();

        TransactionEvent transactionEvent = transactionEventBuilder.build();

        transactionTemplate.send("transaction-to-card-topic", transactionEvent);

        transactionRepository.save(transaction);


        log.debug("Transaction created and saved: {}", transaction);
    }


    private void addMoneyToReceiverBalance(Integer receiverCardId, BigDecimal amount) {
        log.debug("Adding to receiver balance");
        webClient
                .build()
                .post()
                .uri("http://card-service/{cardId}/add", receiverCardId)
                .bodyValue(amount)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        log.debug("Added to receiver balance!");

    }

    private void deductMoneyFromSenderBalance(Integer senderCardId, BigDecimal amount) {
        log.debug("Deducting from sender balance");
        webClient
                .build()
                .post()
                .uri("http://card-service/{cardId}/deduct", senderCardId)
                .bodyValue(amount)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        log.debug("Deducted from sender balance");
    }

    BigDecimal getCardBalance(Integer senderCardId){

        ResponseEntity<BigDecimal> balance = webClient
                .build()
                .get()
                .uri("http://card-service/{cardId}/balance", senderCardId)
                .retrieve()
                .toEntity(BigDecimal.class)
                .block();

        return balance.getBody();
    }

//    @Transactional
//    public void sendPaymentToService(Integer userId, UserDetails userDetails,
//                                     ServicePaymentRequest request) {
//        User sender =
//                commonMethodsService.getUserIfItIsAuthorizedAccess(userId, userDetails);
//        ResponseEntity<ServiceDto> serviceDto =
//                webClient
//                        .build()
//                        .get()
//                        .uri("http://service-service/{serviceId}", request.serviceId())
//                        .retrieve()
//                        .toEntity(ServiceDto.class)
//                        .block();
//        ServiceDto service = serviceDto.getBody();
//        log.debug("Found service: {}", service);
//
//        BigDecimal priceOfService = service.price();
//        log.debug("Price of service: {}", priceOfService);
//
//        BigDecimal senderBalance = getCardBalance(request.senderCardId());
//        log.debug("Sender balance: {}", senderBalance);
//
//        checkIfFundsEnoughToPay(senderBalance, priceOfService);
//        log.debug("Service can be paid by user: {}", sender.getId());
//
//        deductMoneyFromSenderBalance(request.senderCardId(), priceOfService);
//        log.debug("Deducted from sender balance: {}", sender.getId());
//
//        Transaction serviceTransaction =
//                Transaction.builder()
//                        .sender(sender)
//                        .senderCardId(request.senderCardId())
//                        .recipientId(service.id())//service id is like
//                        .recipientCardId(request.accountId())//service has a uniqueId of home
//                        //while paying for water service, it is paid for home
//                        .amount(service.price())
//                        .description(service.description())
//                        .status(TransactionStatus.COMPLETED)
//                        .transactionDate(new Date())
//                        .type(TransactionType.SERVICE_PAYMENT)
//                        .build();
//
//        transactionRepository.save(serviceTransaction);
//        log.debug("Transaction created and saved: {}", serviceTransaction);
//
//    }

    private void checkIfFundsEnoughToPay(BigDecimal senderBalance, BigDecimal requestAmount) {
        if (senderBalance.compareTo(requestAmount) < 0){
            log.error("Not enough funds in balance!");
            throw new NotEnoughFundsException("Not enough funds in balance!");
        }
    }
}
