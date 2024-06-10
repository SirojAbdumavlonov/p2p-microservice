package com.example.notification.email;

import com.example.notification.kafka.card.CardActionEvent;
import com.example.notification.kafka.card.CardBalanceUpdateEvent;
import com.example.notification.kafka.user.UserRegistrationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@Slf4j
public class MailService {


    public SimpleMailMessage createCardBalanceUpdateMail(CardBalanceUpdateEvent event){
        log.debug("Preparing card balance update email: {}", event);
        SimpleMailMessage message = new SimpleMailMessage();

        String transactionType = formatTransactionType(event.getTransactionType());
        log.debug("TransactionType: {}", transactionType);

        String serviceName = getServiceName(event.getServiceName());
        log.debug("ServiceName: {}", serviceName);

        String balanceUpdateType = balanceUpdateType(event.getEventType());
        log.debug("Type of card balance update email: {}", balanceUpdateType);

        String formattedDate = formatDate(event.getEventDate());
        log.debug("Formatted date of card balance update email: {}", formattedDate);

        String cardNumber = hideCardNumber(event.getCardNumber());
        log.debug("Hidden card number of card balance update email: {}", cardNumber);

        message.setTo(event.getEmail());
        message.setSubject("Updated balance of card");
        message.setText(
                String.format("%s: %s, %s: date - %s, card-%s, \n" +
                "sum-%.2f UZS, balance-%.2f UZS",
                        transactionType, serviceName, balanceUpdateType, formattedDate,
                        cardNumber, event.getAmount(), event.getCardBalance()));

        log.debug("Ready update balance of card mail: {}", message);
        return message;
    }

    private String getServiceName(String serviceName) {
        if (serviceName == null) {
            serviceName = "";
        }
        return serviceName;
    }

    private String formatTransactionType(String transactionType) {
        String type;
        if(transactionType.equalsIgnoreCase("p2p_payment")){
            type = "P2P";
        } else {
            type = "Service Payment";
        }
        return type;
    }


    private String balanceUpdateType(String updateType){
        String type;
        if(updateType.equalsIgnoreCase("added")){
            type = "Transferred to card";
        } else {
            type = "Deducted from card";
        }
        return type;
    }

    private String formatDate(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return formatter.format(date);
    }
    private String hideCardNumber(String cardNumber){
        StringBuilder builder = new StringBuilder();
        builder.append("***");

        String last4Digits = getLast4DigitsOfCardNumber(cardNumber);

        builder.append(last4Digits);

        return builder.toString();

    }
    private String getLast4DigitsOfCardNumber(String cardNumber){
        return cardNumber.substring(cardNumber.length() - 4);
    }

    public SimpleMailMessage createUserRegistrationEventMail(UserRegistrationEvent event) {
        log.debug("Preparing user registration email: {}", event);
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(event.email());
        message.setSubject("Registration Event");
        message.setText(
                String.format("Dear %s,\n\n" +
                        "You are successfully registered in p2p and service payment app", event.username()));

        log.debug("Ready registration mail: {}", message);

        return message;
    }

    public SimpleMailMessage createCardActionMail(CardActionEvent event) {
        log.debug("Preparing card action email");
        SimpleMailMessage message = new SimpleMailMessage();

        String cardNumber = hideCardNumber(event.cardNumber());
        log.debug("Hidden card number of card {} mail: {}", event.actionType(), cardNumber);

        message.setTo(event.ownerEmail());
        message.setSubject(String.format("Card %s event", event.actionType()));
        message.setText(
                String.format("Card %s: %s",
                        event.actionType(), cardNumber));

        log.debug("Ready card {} mail: {}", event.actionType(), message);
        return message;
    }


}
