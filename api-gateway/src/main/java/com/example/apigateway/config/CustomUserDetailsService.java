package com.example.apigateway.config;

import com.example.apigateway.dto.UserDetailsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService{

    private final WebClient.Builder webClient;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Integer id = Integer.parseInt(username);

        UserDetailsDto userDetailsDto =
                webClient
                        .build()
                        .get()
                        .uri("http://user-service/{userId}", id)
                        .retrieve()
                        .toEntity(UserDetailsDto.class)
                        //above line converts list of cards to list of cardDtos
                        .block().getBody();

        List<String> roles = List.of(userDetailsDto.role());

        return User.withUsername(String.valueOf(id))
                .password(userDetailsDto.password())
                .authorities((GrantedAuthority) roles.stream()
                        .map(role -> "ROLE_" + role)
                        .collect(Collectors.toList()))
                .build();

    }


}
