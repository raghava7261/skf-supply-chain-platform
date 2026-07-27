package com.skf.scm.po.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Real RBAC via OAuth2/JWT: tokens are issued by Keycloak (see
 * infra/keycloak/scm-realm.json for the realm/client/role/user setup) and
 * validated here against Keycloak's public keys (issuer-uri in
 * application.yml) — this service never sees or stores a password.
 *
 * Roles come from the token's "realm_access.roles" claim, which is the
 * standard place Keycloak puts realm-level roles. A client calling this API
 * first authenticates against Keycloak (e.g. the OAuth2 password or
 * authorization_code grant) to get a JWT, then sends it as
 * "Authorization: Bearer <token>".
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("OPTIONS", "/**").permitAll() // CORS preflight
                        .requestMatchers("GET", "/api/v1/purchase-orders/**").hasAnyRole(
                                "PROCUREMENT_MANAGER", "WAREHOUSE_STAFF", "VIEWER")
                        .requestMatchers("POST", "/api/v1/purchase-orders").hasAnyRole(
                                "PROCUREMENT_MANAGER", "WAREHOUSE_STAFF")
                        .requestMatchers("POST", "/api/v1/purchase-orders/*/approve").hasRole("PROCUREMENT_MANAGER")
                        .requestMatchers("POST", "/api/v1/purchase-orders/*/receive").hasRole("WAREHOUSE_STAFF")
                        .requestMatchers("POST", "/api/v1/purchase-orders/*/cancel").hasRole("PROCUREMENT_MANAGER")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        return http.build();
    }

    /**
     * Keycloak nests realm roles under "realm_access": { "roles": [...] }.
     * Spring's default JwtGrantedAuthoritiesConverter only reads a flat
     * "scope"/"scp" claim, so this pulls realm_access.roles out manually and
     * prefixes each with ROLE_ so hasRole("X") checks work as expected.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractRealmRoles);
        return converter;
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null || !(realmAccess.get("roles") instanceof List<?> roles)) {
            return List.of();
        }
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }
}
