package com.example.apigateway.security;



import com.example.apigateway.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.security.SignatureException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GatewayFilter {


        private String secretKey;

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain){
            ServerHttpRequest request = exchange.getRequest();
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
                String username = claims.getSubject();

                // Set the authenticated user in the Gateway's context
                exchange.getAttributes().put("username", username); // Assuming you want to use the username in downstream services
                // Optionally, set other claims (role, etc.)

                return chain.filter(exchange); // Proceed to the next filter
            } else {
                return Mono.error(new RuntimeException("Unauthorized")); // No token provided
            }
        }


//    private final JwtService jwtService;
//    private final UserDetailsService userDetailsService;
//
//    @Override
//    protected void doFilterInternal(
//            @NonNull HttpServletRequest request,
//            @NonNull HttpServletResponse response,
//            @NonNull FilterChain filterChain
//    ) throws ServletException, IOException {
//        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
//
//        if (isInvalidAuthHeader(authHeader)) {
//            filterChain.doFilter(request, response);
//            log.warn("Authorization header not found");
//            return;
//        }
//
//        String jwt = extractJwt(authHeader);
//        String username = jwtService.extractUsername(jwt);
//
//        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//            authenticateUser(request, jwt, username);
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//    private void authenticateUser(HttpServletRequest request, String jwt, String username) {
//        UserDetails user = userDetailsService.loadUserByUsername(username);
//
//        if (jwtService.isTokenValid(jwt, user)) {
//            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
//            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//        }
//    }
//
//    private boolean isInvalidAuthHeader(String authHeader) {
//        return authHeader == null || !authHeader.startsWith("Bearer ");
//    }
//
//    private String extractJwt(String authHeader) {
//        return authHeader.substring(7);
//    }

    }

