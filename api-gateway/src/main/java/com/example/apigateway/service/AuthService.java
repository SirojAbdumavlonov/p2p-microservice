package com.example.apigateway.service;

import com.example.apigateway.dto.LoginRequest;
import com.example.apigateway.dto.LoginResponse;
import com.example.apigateway.dto.SignUpRequest;
import com.example.apigateway.dto.UserDto;
import com.example.apigateway.security.UserForJwtToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final WebClient.Builder webClient;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    public LoginResponse signInUser(LoginRequest request) {
        log.debug("Singing in user: {}", request);
        UserDto userDto = webClient
                .build()
                .post()
                .uri("http://user-service/sign-in")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(UserDto.class)
                .block();
        log.debug("User logged in: {}", request);
        authenticateUser(userDto.id(), request.password());
        return createJwtTokenForUserAndReturn(userDto.role(), userDto.id());

    }

    private void authenticateUser(Integer userId, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userId, password
                )
        );
        log.debug("Authenticated user: {}", authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private LoginResponse createJwtTokenForUserAndReturn(String role, Integer userId) {
        UserForJwtToken user = new UserForJwtToken();
        user.setUsername(userId);
        user.setRole(role);

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
        claims.put("role", user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        log.debug("Claim: Username={}, role = {}", user.getUsername(), user.getAuthorities());
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
