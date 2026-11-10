package edu.cit.dingding.tabletap.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

// This configuration secures the API with stateless JWT authentication.
@Configuration
public class SecurityConfig {

    @Bean
    public JwtEncoder jwtEncoder(@Value("${app.jwt.secret}") String secret) {
        return NimbusJwtEncoder.withSecretKey(secretKey(secret)).build();
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${app.jwt.secret}") String secret) {
        return NimbusJwtDecoder.withSecretKey(secretKey(secret))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt ->
                java.util.List.of(new SimpleGrantedAuthority("ROLE_" + jwt.getClaimAsString("role"))));
        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/health").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/tables/**", "/api/floor-plans/**").hasRole("OWNER")
                        .requestMatchers(HttpMethod.PUT, "/api/tables/**", "/api/floor-plans/**").hasRole("OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/api/tables/**", "/api/floor-plans/**").hasRole("OWNER")
                        .requestMatchers(HttpMethod.GET, "/api/tables/**", "/api/floor-plans/**").authenticated()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
        return http.build();
    }

    private SecretKey secretKey(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secret.length() < 32 || keyBytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret must be at least 32 characters");
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }
}
