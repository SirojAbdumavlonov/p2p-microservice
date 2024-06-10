package com.example.user.service;

import com.example.user.dto.CardDto;
import com.example.user.dto.LoginRequest;
import com.example.user.dto.RegisterRequest;
import com.example.user.dto.UserRegistrationEvent;
import com.example.user.entity.User;
import com.example.user.exception.*;
import com.example.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WebClient.Builder webClient;
    private final CommonMethodsService methodsService;
    private final KafkaTemplate<String, UserRegistrationEvent> userRegistrationTemplate;


    @Transactional
    public User saveUser(RegisterRequest request) {
        log.debug("Saving user: {}", request);

        checkIfUsernameExists(request.username());

        log.debug("Username is valid");

        checkIfEmailExists(request.email());

        log.debug("Email is valid");

        User user = createUser(request);
        log.info("Saved user: {}", user);
        User savedUser = userRepository.save(user);
        log.info("User is saved: {}", savedUser);

        log.info("Sending from kafka");
        createAndSentUserRegistrationEvent(request);
        log.debug("User registration event is sent!");
        log.info("Send from kafka");
        return user;
    }

    private void createAndSentUserRegistrationEvent(RegisterRequest request) {
        UserRegistrationEvent userRegistrationEvent = new UserRegistrationEvent();
        userRegistrationEvent.setEmail(request.email());
        userRegistrationEvent.setUsername(request.username());
        userRegistrationTemplate.send("user-topic", userRegistrationEvent);
    }

    private void checkIfEmailExists(String email) {
        if(userRepository.findByEmail(email).isPresent()){
            log.error("Email already exists: {}",email);
            throw new EmailAlreadyUsedException(email);
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
                .role("USER")
                .email(request.email())
                .phoneNumber(request.email())
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .createdAt(new Date())
                .build();
    }


    public String login(LoginRequest request) {
        log.debug("Login request: {}", request);

        checkIfEmailExists(request.email());
        log.debug("Email is used");

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException(request.email()));
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


    public User getUserById(Integer userId, UserDetails userDetails) {
        log.debug("Get user by username from token: {}", userDetails.getUsername());

        return methodsService.getUserIfItIsAuthorizedAccess(userId, userDetails);
    }

    public List<CardDto> getAllCardsOfUser(Integer userId, UserDetails userDetails) {
        log.debug("Get cards of user with id: {}", userId);

        User user =
                methodsService.getUserIfItIsAuthorizedAccess(userId, userDetails);

        ResponseEntity<List<CardDto>> response = webClient
                .build()
                .get()
                .uri("http://card-service/user/{userId}", user.getId())
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<CardDto>>() {})
                //above line converts list of cards to list of cardDtos
                .block();
        return response.getBody();
    }


    public User updateUser(Integer userId, User user) {
        if (!user.getId().equals(userId)) {
            log.error("User with user id {} does not match", userId);
            throw new UserNotFoundException("User not found");
        }
        ifUserExistsById(userId);
        return userRepository.save(user);
    }

    public void deleteUser(Integer userId) {
        log.debug("Deleting user with id: {}", userId);
        ifUserExistsById(userId);
        userRepository.deleteById(userId);
    }
    private void ifUserExistsById(Integer userId){
        if (!userRepository.existsById(userId)){
            log.error("User with user id {} does not exist", userId);
            throw new UserNotFoundException("User not found");
        }
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
