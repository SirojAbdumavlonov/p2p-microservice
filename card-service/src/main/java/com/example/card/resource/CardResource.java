package com.example.card.resource;

import com.example.card.entity.Card;
import com.example.card.exception.UnauthorizedException;
import com.example.card.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

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
        try {
            Card card = cardService.getCardDetailById(cardId, userDetails);
            log.info("Found card: {}", card);
            return ResponseEntity.ok().body(card);
        } catch (UnauthorizedException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized access");
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error!");
        }
    }


}
