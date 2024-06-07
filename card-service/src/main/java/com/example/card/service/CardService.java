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

import java.util.Date;

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

        card.setOwnerUsername(userDetails.getUsername());

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
        log.debug("Checking owner of card: {}", card.getOwnerUsername());
        checkIfCardBelongsToRequestedUser(card.getOwnerUsername(), userDetails.getUsername());

        return card;
    }

    private void checkIfCardBelongsToRequestedUser(String ownerUsername, String username) {
        if (!ownerUsername.equals(username)) {
            log.error("unauthorized access");
            throw new UnauthorizedException("Unauthorized access");
        }
    }
}
