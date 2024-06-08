package com.example.user.service;

import com.example.user.constant.TransactionStatus;
import com.example.user.dto.P2pPaymentRequest;
import com.example.user.dto.ServiceDto;
import com.example.user.dto.ServicePaymentRequest;
import com.example.user.entity.P2pTransaction;
import com.example.user.entity.ServiceTransaction;
import com.example.user.entity.User;
import com.example.user.exception.NotEnoughFundsException;
import com.example.user.exception.UserNotFoundException;
import com.example.user.repository.P2pTransactionRepository;
import com.example.user.repository.ServiceTransactionRepository;
import com.example.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    private final ServiceTransactionRepository serviceTransactionRepository;
    private final CommonMethodsService commonMethodsService;
    private final WebClient.Builder webClient;
    private final P2pTransactionRepository p2pTransactionRepository;

    @Transactional
    public void sendPaymentToUser(Integer userId,
                                  UserDetails userDetails,
                                  P2pPaymentRequest request) {

        User sender =
                commonMethodsService.getUserIfItIsAuthorizedAccess
                        (userId, userDetails);
        log.debug("Sender: {}", sender.getId());
        User recipient =
                userRepository.findById(request.receiverId())
                        .orElseThrow(() ->
                                new UserNotFoundException(request.receiverId())
                );
        log.debug("Recipient: {}", recipient.getId());

        BigDecimal senderBalance = getCardBalance(request.senderCardId());

        checkIfFundsEnoughToPay(senderBalance, request.amount());

        log.debug("Balance is checked");
        deductMoneyFromSenderBalance(request.senderCardId(), request.amount());
        log.debug("Deducted from sender balance: {}", sender.getId());

        addMoneyToReceiverBalance(request.receiverCardId(), request.amount());
        log.debug("Added money to receiver balance: {}", recipient.getId());

        P2pTransaction p2pTransaction =
                P2pTransaction.builder()
                        .sender(sender)
                        .recipient(recipient)
                        .amount(request.amount())
                        .status(TransactionStatus.COMPLETED)
                        .description("p2p payment")
                        .transactionDate(new Date())
                        .build();

        p2pTransactionRepository.save(p2pTransaction);
        log.debug("Transaction created and saved: {}", p2pTransaction);

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

    @Transactional
    public void sendPaymentToService(Integer userId, UserDetails userDetails,
                                     ServicePaymentRequest request) {
        User sender =
                commonMethodsService.getUserIfItIsAuthorizedAccess(userId, userDetails);
        ResponseEntity<ServiceDto> serviceDto =
                webClient
                        .build()
                        .get()
                        .uri("http://service-service/{serviceId}", request.serviceId())
                        .retrieve()
                        .toEntity(ServiceDto.class)
                        .block();
        ServiceDto service = serviceDto.getBody();
        log.debug("Found service: {}", service);

        BigDecimal priceOfService = service.price();
        log.debug("Price of service: {}", priceOfService);

        BigDecimal senderBalance = getCardBalance(request.senderCardId());
        log.debug("Sender balance: {}", senderBalance);

        checkIfFundsEnoughToPay(senderBalance, priceOfService);
        log.debug("Service can be paid by user: {}", sender.getId());

        deductMoneyFromSenderBalance(request.senderCardId(), priceOfService);
        log.debug("Deducted from sender balance: {}", sender.getId());

        ServiceTransaction serviceTransaction =
                ServiceTransaction.builder()
                        .sender(sender)
                        .senderCardId(request.senderCardId())
                        .serviceId(service.id())
                        .amount(service.price())
                        .description(service.description())
                        .status(TransactionStatus.COMPLETED)
                        .transactionDate(new Date())
                        .build();

        serviceTransactionRepository.save(serviceTransaction);
        log.debug("Transaction created and saved: {}", serviceTransaction);

    }

    private void checkIfFundsEnoughToPay(BigDecimal senderBalance, BigDecimal requestAmount) {
        if (senderBalance.compareTo(requestAmount) < 0){
            log.error("Not enough funds in balance!");
            throw new NotEnoughFundsException("Not enough funds in balance!");
        }
    }
}
