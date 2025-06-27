package com.example.datalake.backend.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(org.springframework.security.config.web.server.ServerHttpSecurity http) {
        AuthenticationWebFilter authFilter = new AuthenticationWebFilter(authenticationManager());
        authFilter.setServerAuthenticationConverter(new BearerTokenServerAuthenticationConverter());
        http.csrf(org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.POST, "/records/**", "/storage/**").authenticated()
                        .pathMatchers(HttpMethod.PUT, "/records/**").authenticated()
                        .pathMatchers(HttpMethod.DELETE, "/records/**", "/storage/**").authenticated()
                        .anyExchange().permitAll())
                .addFilterAt(authFilter, SecurityWebFiltersOrder.AUTHENTICATION);
        return http.build();
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager() {
        return authentication -> {
            String token = (String) authentication.getCredentials();
            try {
                FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(token);
                String username = decoded.getEmail() != null ? decoded.getEmail() : decoded.getUid();
                User user = new User(username, "", List.of(new SimpleGrantedAuthority("ROLE_USER")));
                return Mono.just(new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities()));
            } catch (FirebaseAuthException e) {
                return Mono.error(new BadCredentialsException("Invalid token", e));
            }
        };
    }

    private static class BearerTokenServerAuthenticationConverter implements ServerAuthenticationConverter {
        @Override
        public Mono<Authentication> convert(ServerWebExchange exchange) {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                return Mono.just(new UsernamePasswordAuthenticationToken(token, token));
            }
            return Mono.empty();
        }
    }
}
