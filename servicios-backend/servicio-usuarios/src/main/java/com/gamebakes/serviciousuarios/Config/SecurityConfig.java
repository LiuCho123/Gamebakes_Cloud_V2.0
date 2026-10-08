package com.gamebakes.serviciousuarios.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${gamebakes.security.azure.issuer}")
    private String azureIssuer;

    @Value("${gamebakes.security.cognito.issuer}")
    private String cognitoIssuer;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        JwtIssuerAuthenticationManagerResolver authenticationManagerResolver =
                new JwtIssuerAuthenticationManagerResolver(azureIssuer, cognitoIssuer);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationManagerResolver(authenticationManagerResolver)
                );

        return http.build();
    }
}