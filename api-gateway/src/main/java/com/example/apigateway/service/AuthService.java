package com.example.apigateway.service;

import com.example.apigateway.dto.LoginRequest;
import com.example.apigateway.dto.LoginResponse;
import com.example.apigateway.dto.SignUpRequest;
import com.example.apigateway.security.UserForJwtToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final WebClient.Builder webClient;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    public LoginResponse signInUser(LoginRequest request) {
        log.debug("Singing in user: {}", request);
        String username = webClient
                .build()
                .post()
                .uri("http://user-service/sign-in")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        log.debug("User logged in: {}", request);
        authenticateUser(username, request.password());
        return createJwtTokenForUserAndReturn(username);

    }

    private void authenticateUser(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username, password
                )
        );
        log.debug("Authenticated user: {}", authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private LoginResponse createJwtTokenForUserAndReturn(String username) {
        UserForJwtToken user = new UserForJwtToken();
        user.setUsername(username);

        return new LoginResponse(generateJwtToken(user));
    }

    private String generateJwtToken(UserDetails userDetails) {
        Map<String, Object> claims = getClaimsFromUser(userDetails);
        return jwtService.generateToken(claims, userDetails);
    }

    private Map<String, Object> getClaimsFromUser(UserDetails user) {
        log.debug("Getting claims from user: {}", user);

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());

        log.debug("Claim: Username={}", user.getUsername());
        return claims;
    }
    public void registerUser(SignUpRequest request) {
        log.debug("Registering user: {}", request);
         webClient
                .build()
                .post()
                .uri("http://user-service/register")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(void.class)
                .block();
    }
}
