package com.example.card.service;

import com.example.card.dto.CardActionEvent;
import com.example.card.dto.EmailDto;
import com.example.card.entity.Card;
import com.example.card.exception.CardAlreadyUsedException;
import com.example.card.exception.CardNotFoundException;
import com.example.card.exception.UnauthorizedException;
import com.example.card.repository.CardRepository;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {
    private final CardRepository cardRepository;
    private final WebClient.Builder webClient;
    private final KafkaTemplate<String, CardActionEvent> cardActionEventTemplate;
    @Transactional
    public void saveCard(Card card, UserDetails userDetails) {
        log.debug("Saving Card: {}", card);

        checkIfCardIsRegistered(card.getNumber());
        log.debug("Card with number {} was not registered before", card.getNumber());

        card.setUserId(Integer.valueOf(userDetails.getUsername()));

        boolean isExpired =
                checkExpirationOfCard(card.getExpirationDate());

        card.setExpired(isExpired);
        log.debug("Expiration of card {} is {}", card.getNumber(), isExpired);

        cardRepository.save(card);
        log.debug("Card saved successfully: {}", card);

        log.debug("Preparing sending owner email about adding!");

        createAndSendCardActionEvent(card, "added");
        log.debug("Card adding event is sent queue!");
//        checkOwnerOfCard(userDetails.getUsername());
    }

    private void createAndSendCardActionEvent(Card card, String actionType){

        String ownerEmail = getCardOwnerEmail(card);
        log.debug("Owner email is {}", ownerEmail);

        CardActionEvent cardActionEvent = new CardActionEvent(
                ownerEmail, card.getNumber(), actionType
        );
        log.debug("Card adding event is created: {}", cardActionEvent);

        cardActionEventTemplate.send("card-action-topic", cardActionEvent);
    }

    @Transactional
    public void deleteCard(Integer cardId) {
        log.debug("Deleting card with id: {}", cardId);
        ifCardExistsById(cardId);
        log.debug("Card exists with id: {}", cardId);

        Card card = cardRepository.findById(cardId)
                        .orElseThrow();

        cardRepository.deleteById(cardId);
        log.debug("Card deleted successfully: {}", card.getId());

        log.debug("Preparing sending owner email about deleting!");

        createAndSendCardActionEvent(card, "deleted");
        log.debug("Card deleted event is sent queue!");
    }

    public String getCardOwnerEmail(Card card) {
        ResponseEntity<EmailDto> emailDto =
                webClient
                        .build()
                        .get()
                        .uri("http://user-service/{userId}", card.getUserId())
                        .retrieve()
                        .toEntity(EmailDto.class)
                        //above line converts list of cards to list of cardDtos
                        .block();

        return emailDto.getBody().email();
    }

    private boolean checkExpirationOfCard(Date expirationDate) {
        return expirationDate.before(new Date()); // 01/23 06/2024
    }

//    private void checkOwnerOfCard(String ownerUsername) {
//        Boolean ifOwnerExistsById =
//                webClient
//                        .build()
//                        .get()
//                        .uri("http://user-service/{ownerUsername}", ownerUsername)
//                        .retrieve()
//                        .toEntity(Boolean.class)
//                        .block()
//                        .getBody();
//        if (!ifOwnerExistsById){
//            throw new OwnerOfCardNotExistException(ownerUsername, );
//        }
//    }

    private void checkIfCardIsRegistered(String number) {
        if(cardRepository.existsByNumber(number)){
            log.error("Card with number {} already registered", number);
            throw new CardAlreadyUsedException(number);
        }
    }


    public Card getCardDetailById(Integer cardId, UserDetails userDetails) {
        log.debug("Getting card details by id {}", cardId);
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        checkIfCardBelongsToRequestedUser(card.getUserId(), Integer.valueOf(userDetails.getUsername()));

        return card;
    }

    private void checkIfCardBelongsToRequestedUser(Integer cardUserId, Integer userId) {
        if (!cardUserId.equals(userId)) {
            log.error("unauthorized access");
            throw new UnauthorizedException("Unauthorized access");
        }
    }

    public BigDecimal getCardBalanceById(Integer cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        return card.getBalance();
    }

    public List<Card> getAllCardsOfUser(Integer userId) {
        return cardRepository.findByUserId(userId);
    }


    public void deductFromBalance(Integer cardId, BigDecimal amount) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        card.setBalance(card.getBalance().subtract(amount));
        log.debug("Subtracted from balance of card: {}",  card.getId());
        cardRepository.save(card);
    }

    public void addToBalance(Integer cardId, BigDecimal amount) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        card.setBalance(card.getBalance().add(amount));
        log.debug("Added money to balance of receiver of card: {}",  card.getId());
        cardRepository.save(card);
    }
    @Transactional
    public Card updateCard(Integer cardId, Card card) {
        log.debug("Updating card: {}", card.getId());
        ifCardExistsById(cardId);

        return cardRepository.save(card);
    }

    private void ifCardExistsById(Integer cardId) {
        if (!cardRepository.existsById(cardId)) {
            log.error("Card with id {} does not exist", cardId);
            throw new CardNotFoundException(cardId);
        }
    }

    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }
}
