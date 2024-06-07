package com.example.user.resource;

import com.example.user.dto.CardDto;
import com.example.user.dto.ErrorResponse;
import com.example.user.dto.LoginRequest;
import com.example.user.dto.RegisterRequest;
import com.example.user.entity.User;
import com.example.user.exception.*;
import com.example.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Slf4j
public class UserResource {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> saveUser(@RequestBody RegisterRequest request) {
        log.info("REST request to register user: {}", request);
        try {
            User user = userService.saveUser(request);
            return ResponseEntity.created(new URI("/api/users/" + user.getId()))
                    .body("Registered successfully");
        } catch (PhoneNumberAlreadyUsedException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Phone number already used!"));
        } catch (UsernameAlreadyUsedException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Username already used!"));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error!"));
        }
    }
    @PostMapping("/sign-in")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        log.info("REST request to login");
        try {
            String username = userService.login(request);
            return ResponseEntity.ok().body(username);
        } catch (IncorrectPasswordException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Incorrect password!"));
        } catch (PhoneNumberNotUsedException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Phone number is not registered!"));
        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error!"));
        }
    }


//    @GetMapping("/{userId}")
//    public Boolean ifUserExists(@PathVariable("userId") int userId) {
//        log.info("REST request to check if user exists: {}", userId);
//        return userService.checkUserExistenceById(userId);
//    }


    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable Integer userId, @AuthenticationPrincipal UserDetails user) {
        log.info("REST request to get user details: {}", user);
        try {
            User foundUser = userService.getUserById(userId, user);
            log.info("Found user: {}", foundUser);

            return ResponseEntity.ok().body(foundUser);
        } catch (UserNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("User not found"));
        } catch (UnauthorizedException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Unauthorized access"));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error!"));
        }

    }

    @GetMapping("/{userId}/cards")
    public ResponseEntity<?> getUserCards(@PathVariable Integer userId,
                                                      @AuthenticationPrincipal UserDetails user) {
        log.info("REST request to get cards of user: {}", userId);
        try {
            List<CardDto> cards = userService.getAllCardsOfUser(userId, user);
            log.info("Found cards: {}", cards);
            return ResponseEntity.ok().body(cards);

        } catch (UserNotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("User not found"));
        } catch (UnauthorizedException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Unauthorized access"));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Internal server error!"));
        }
    }




}
