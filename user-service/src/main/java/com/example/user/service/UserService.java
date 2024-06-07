package com.example.user.service;

import com.example.user.dto.CardDto;
import com.example.user.dto.LoginRequest;
import com.example.user.dto.RegisterRequest;
import com.example.user.entity.User;
import com.example.user.exception.*;
import com.example.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WebClient.Builder webClient;

    @Transactional
    public User saveUser(RegisterRequest request) {
        log.debug("Saving user: {}", request);

        checkIfUsernameExists(request.username());

        log.debug("Username is valid");

        checkIfPhoneNumberExists(request.phoneNumber());

        log.debug("Phone number is valid");

        User user = createUser(request);

        userRepository.save(user);
        return user;
    }

    private void checkIfPhoneNumberExists(String phoneNumber) {
        if(userRepository.existsByPhoneNumber(phoneNumber)){
            log.error("Phone number already exists: {}",phoneNumber);
            throw new PhoneNumberAlreadyUsedException(phoneNumber);
        }
    }
    private void checkIfUsernameExists(String username) {
        if(userRepository.existsByUsername(username)){
            log.error("Username already exists: {}",username);
            throw new UsernameAlreadyUsedException(username);
        }
    }
    private User createUser(RegisterRequest request){
        return User.builder()
                .phoneNumber(request.phoneNumber())
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .build();
    }


    public String login(LoginRequest request) {
        log.debug("Login request: {}", request);

        checkIfPhoneNumberUsed(request.phoneNumber());
        log.debug("Phone number is used");

        User user = userRepository.findByPhoneNumber(request.phoneNumber());
        log.debug("Found user: {}", user);

        checkIfPasswordsTheSame(request.password(), user.getPassword());
        log.debug("Passwords do match");

        return user.getUsername();
    }

    private void checkIfPasswordsTheSame(String password, String savedPassword) {
        if(!passwordEncoder.matches(password, savedPassword)){
            log.error("Passwords do not match");
            throw new IncorrectPasswordException();
        }
    }

    private void checkIfPhoneNumberUsed(String phoneNumber){
        if (!(userRepository.existsByPhoneNumber(phoneNumber))){
            log.error("Phone number not registered: {}",phoneNumber);
            throw new PhoneNumberNotUsedException(phoneNumber);
        }
    }

    public boolean checkUserExistenceById(int userId) {
        return userRepository.existsById(userId);
    }

    public User getUserById(Integer userId, UserDetails userDetails) {
        log.debug("Get user by username from token: {}", userDetails.getUsername());

        return getUserIfItIsAuthorizedAccess(userId, userDetails);
    }

    public List<CardDto> getAllCardsOfUser(Integer userId, UserDetails userDetails) {
        log.debug("Get cards of user with id: {}", userId);

        User user =
                getUserIfItIsAuthorizedAccess(userId, userDetails);

        ResponseEntity<List<CardDto>> response = webClient
                .build()
                .get()
                .uri("http://card-service/cards/user/{userId}", user.getId())
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<CardDto>>() {})
                //above line converts list of cards to list of cardDtos
                .block();
        return response.getBody();
    }

    private User getUserIfItIsAuthorizedAccess(Integer userId, UserDetails userDetails){
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException(userDetails.getUsername()));

        log.debug("Checking ids");
        checkIfUserIdTheSameWithIdOfUserFromToken(userId, user.getId());
        log.debug("Ids do match");

        return user;
    }

    private void checkIfUserIdTheSameWithIdOfUserFromToken(Integer userId, Integer id) {
        if (!userId.equals(id)){
            throw new UnauthorizedException("Unauthorized access");
        }
    }
}
