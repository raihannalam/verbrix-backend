package com.verbrix.security.config;

import com.verbrix.security.jwt.AuthEntryPointJwt;
import com.verbrix.security.jwt.AuthTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.CrossOriginOpenerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthEntryPointJwt unauthorizedHandler;
    private final AuthTokenFilter authTokenFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http

                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)

                .headers(headers -> headers
                        .crossOriginOpenerPolicy(policy ->
                                policy.policy(CrossOriginOpenerPolicyHeaderWriter.CrossOriginOpenerPolicy.SAME_ORIGIN_ALLOW_POPUPS)
                        )
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(unauthorizedHandler))

                .authorizeHttpRequests(auth -> auth

                        // Preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/internal/**").permitAll()

                        // Auth
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Public APIs
                        .requestMatchers("/api/v1/public/**").permitAll()
                        .requestMatchers("/api/v1/metadata/**").permitAll()

                        // Webhooks (secured by signature internally)
                        .requestMatchers("/api/v1/webhooks/**").permitAll()

                        // WebSockets
                        .requestMatchers("/ws/**").permitAll()

                        // Ping
                        .requestMatchers("/api/health").permitAll()

                        // Static content
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()

                        // Everything else
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        authTokenFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
