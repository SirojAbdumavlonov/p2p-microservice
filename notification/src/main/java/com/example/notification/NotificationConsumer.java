package com.example.notification;

import com.example.notification.email.MailService;
import com.example.notification.kafka.card.CardActionEvent;
import com.example.notification.kafka.card.CardBalanceUpdateEvent;
import com.example.notification.kafka.user.UserRegistrationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final JavaMailSender mailSender;
    private final MailService mailService;


    @KafkaListener(topics = "card-update-balance-topic", groupId = "card-update-group")
    public void consumerCardBalanceUpdate(CardBalanceUpdateEvent updateBalanceEvent){
        log.info("Consuming message from card-update-balance-topic: {}", updateBalanceEvent);
        //get and send as email
        SimpleMailMessage mailMessage = mailService.createCardBalanceUpdateMail(updateBalanceEvent);
        log.info("Card balance update mail is going to send");

        mailSender.send(mailMessage);
        log.info("Card balance update mail sent successfully");

    }

    @KafkaListener(topics = "card-action-topic", groupId = "card-action-group")
    public void consumerCardActionEvent(CardActionEvent cardActionEvent){
        log.info("Consuming message from card-action-topic: {}", cardActionEvent);
        //get and send as email
        SimpleMailMessage mailMessage = mailService.createCardActionMail(cardActionEvent);
        log.info("Card {} mail is going to send", cardActionEvent.actionType());

        mailSender.send(mailMessage);
        log.info("Card {} mail sent successfully", cardActionEvent.actionType());

    }

    @KafkaListener(topics = "user-topic", groupId = "user-group")
    public void consumeUserRegistrationEvent(UserRegistrationEvent userRegistrationEvent){
        log.info("Consuming message from user-topic: {}", userRegistrationEvent);
        System.out.println("he;llllllo");
//        SimpleMailMessage mailMessage = mailService.createUserRegistrationEventMail(userRegistrationEvent);
        log.info("User registration mail is going to send");

//        mailSender.send(mailMessage);
        log.info("User registration mail sent successfully");

    }


}
