package com.example.apigateway.resource;

import com.example.apigateway.dto.LoginRequest;
import com.example.apigateway.dto.LoginResponse;
import com.example.apigateway.dto.SignUpRequest;
import com.example.apigateway.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthResource {

    private final AuthService authService;

    @PostMapping("/sign-in")
    public ResponseEntity<?> signInUser(@RequestBody LoginRequest request){
        log.info("REST request to sign-in: {}", request);
        LoginResponse response = authService.signInUser(request);
        log.info("User signed in: {}", request);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUpUser(@RequestBody SignUpRequest request){
        log.info("REST request to sign-up: {}", request);
        authService.registerUser(request);
        log.info("User registered: {}", request);
        return ResponseEntity.ok().body("User registered successfully");
    }

}
