package com.example.card.resource;

import com.example.card.entity.Card;
import com.example.card.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cards")
public class CardResource {
    private final CardService cardService;

    @PostMapping("")
    public ResponseEntity<?> addCardToUser(@Validated @RequestBody Card card,
                                     @AuthenticationPrincipal UserDetails userDetails) throws BadRequestException, URISyntaxException {
        log.info("REST request to save Card: {}", card);
        if(card.getId() != null){
            throw new BadRequestException("A new card cannot have an id!");
        }
        cardService.saveCard(card, userDetails);
        return ResponseEntity.created(new URI("/api/cards/" + card.getId()))
                .body("Card added successfully");
    }
    @GetMapping("/{cardId}")
    public ResponseEntity<?> getCardById(@PathVariable Integer cardId,
                                         @AuthenticationPrincipal UserDetails userDetails) {
        log.info("REST request to get Card : {}", cardId);

        Card card = cardService.getCardDetailById(cardId, userDetails);
        log.info("Found card: {}", card);
        return ResponseEntity.ok().body(card);

    }

    @GetMapping("/{cardId}/balance")
    public ResponseEntity<BigDecimal> getBalanceOfCardById(@PathVariable Integer cardId){
        log.info("REST request to get Balance of Card : {}", cardId);

        BigDecimal balance = cardService.getCardBalanceById(cardId);
        log.info("Found balance: {}", balance);

        return ResponseEntity.ok().body(balance);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Card>> getCardsByUserId(@PathVariable Integer userId){
        log.info("REST request to get Cards by User : {}", userId);

        List<Card> cards = cardService.getAllCardsOfUser(userId);
        log.info("Found cards: {}", cards);

        return ResponseEntity.ok().body(cards);
    }
    @PostMapping("/{cardId}/deduct")
    public void deductFromCard(@PathVariable Integer cardId,
                               @RequestBody BigDecimal amount){
        log.info("REST request to deduct from card : {}", cardId);
        cardService.deductFromBalance(cardId, amount);
        log.info("Balance of sender is updated and saved");

    }
    @PostMapping("/{cardId}/add")
    public void addToCard(@PathVariable Integer cardId,
                          @RequestBody BigDecimal amount){
        log.info("REST request to add to card : {}", cardId);
        cardService.addToBalance(cardId, amount);
        log.info("Balance of receiver is updated and saved");

    }


    @PutMapping("/{cardId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Card> updateCard(@PathVariable Long cardId, @Valid @RequestBody Card card) {
        if (!card.getId().equals(cardId)) {
            throw new RuntimeException("Card not found");
        }
        Card updatedCard = cardService.updateCard(card);
        return new ResponseEntity<>(updatedCard, HttpStatus.OK);
    }

    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Void> deleteCard(@PathVariable Integer cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }


}
