package com.example.card.kafka;

import com.example.card.dto.CardBalanceUpdateEvent;
import com.example.card.dto.TransactionEvent;
import com.example.card.entity.Card;
import com.example.card.exception.CardNotFoundException;
import com.example.card.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionToCardTopicConsumer {

    private final CardRepository cardRepository;
    private final KafkaTemplate cardBalanceEvent;

    @KafkaListener(topics = "transaction-to-card-topic", groupId = "card-group")
    public void getTransactionAndSendToNotificationMS(TransactionEvent transactionEvent) {
        log.info("Received transaction event: {}", transactionEvent);

        Card senderCard = cardRepository.findById(transactionEvent.sendCardId())
                .orElseThrow(() -> new CardNotFoundException(transactionEvent.sendCardId()));

        prepareAndSendCardBalanceUpdateEvent(transactionEvent, senderCard, "deducted");
        log.info("Sent update balance event of sender");

        if(transactionEvent.transactionType().equalsIgnoreCase("p2p_payment")) {
            //if it is p2p transaction, it will find receivers
            Card receieverCard = cardRepository.findById(transactionEvent.receiverCardId())
                    .orElseThrow(() -> new CardNotFoundException(transactionEvent.receiverCardId()));
            //it will be sent to notification microservice

            prepareAndSendCardBalanceUpdateEvent(transactionEvent, receieverCard, "added");
            log.info("Sent update balance event of receiver");
        }

    }
    private void prepareAndSendCardBalanceUpdateEvent(TransactionEvent transactionEvent,
                                                      Card senderCard, String eventType) {

        String serviceName = null;
        if (transactionEvent.transactionType().equalsIgnoreCase("service_payment")) {
            serviceName = transactionEvent.receiverEmail();
            //service name is assigned as email in TransactionEvent class
        }


        log.debug("Preparing {} card balance update event", eventType);
        CardBalanceUpdateEvent event =
                CardBalanceUpdateEvent
                        .builder()
                        .email(transactionEvent.senderEmail())
                        .eventType(eventType)
                        .cardNumber(senderCard.getNumber())
                        .amount(transactionEvent.amount())
                        .cardBalance(senderCard.getBalance())
                        .eventDate(transactionEvent.eventDate())
                        .transactionType(transactionEvent.transactionType())
                        .serviceName(serviceName)
                        .build();

        cardBalanceEvent.send("card-balance-update-topic", event);
    }

}
