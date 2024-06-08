package com.example.card.service;

import com.example.card.entity.Card;
import com.example.card.exception.CardAlreadyUsedException;
import com.example.card.exception.CardNotFoundException;
import com.example.card.exception.OwnerOfCardNotExistException;
import com.example.card.exception.UnauthorizedException;
import com.example.card.repository.CardRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
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

    @Transactional
    public void saveCard(Card card, UserDetails userDetails) throws BadRequestException {
        log.debug("Saving Card");

        checkIfCardIsRegistered(card.getNumber());
        log.debug("Card with number {} was not registered before", card.getNumber());

        card.setUserId(Integer.valueOf(userDetails.getUsername()));

        boolean isExpired =
                checkExpirationOfCard(card.getExpirationDate());
        card.setExpired(isExpired);
        log.debug("Expiration of card {} is {}", card.getNumber(), isExpired);

        cardRepository.save(card);
//        checkOwnerOfCard(userDetails.getUsername());
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

    public Card updateCard(Card card) {
        return cardRepository.save(card);
    }

    public void deleteCard(Integer cardId) {
        cardRepository.deleteById(cardId);
    }

}
