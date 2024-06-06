package com.example.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouterConfiguration {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r.path("/users/**")
                        .uri("lb://user-service"))
                .route("card-service", r -> r.path("/cards/**")
                        .uri("lb://card-service"))
                .route("service-service", r -> r.path("/services/**")
                        .uri("lb://service-service"))
                .build();
    }

}
