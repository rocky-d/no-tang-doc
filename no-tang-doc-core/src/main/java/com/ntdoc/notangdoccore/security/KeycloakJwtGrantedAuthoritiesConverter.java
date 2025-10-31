package com.ntdoc.notangdoccore.security;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Converts Keycloak JWT (realm_access + resource_access + scope) into Spring Security GrantedAuthorities.
 * - realm roles => ROLE_<role>
 * - client roles (resource_access[clientId].roles) => ROLE_<role>
 * - scopes (scope claim, space separated; or scp array) => SCOPE_<scope>
 */
public class KeycloakJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final String clientId;

    public KeycloakJwtGrantedAuthoritiesConverter(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<String> roleNames = new HashSet<>();

        // realm_access.roles
        Object realmAccess = jwt.getClaims().get("realm_access");
        if (realmAccess instanceof Map<?,?> realmMap) {
            Object roles = realmMap.get("roles");
            if (roles instanceof Collection<?> coll) {
                coll.forEach(r -> roleNames.add(String.valueOf(r)));
            }
        }

        // resource_access[clientId].roles
        Object resourceAccess = jwt.getClaims().get("resource_access");
        if (resourceAccess instanceof Map<?,?> resMap) {
            Object clientObj = resMap.get(clientId);
            if (clientObj instanceof Map<?,?> clientMap) {
                Object roles = clientMap.get("roles");
                if (roles instanceof Collection<?> coll) {
                    coll.forEach(r -> roleNames.add(String.valueOf(r)));
                }
            }
        }

        // scopes (space separated)
        Set<String> scopes = new HashSet<>();
        Object scopeClaim = jwt.getClaims().get("scope");
        if (scopeClaim instanceof String scopeStr) {
            scopes.addAll(Arrays.stream(scopeStr.split(" ")).filter(s -> !s.isBlank()).collect(Collectors.toSet()));
        }
        Object scpClaim = jwt.getClaims().get("scp"); // some providers use 'scp' array
        if (scpClaim instanceof Collection<?> coll) {
            coll.forEach(s -> scopes.add(String.valueOf(s)));
        }

        // Build authorities
        Set<GrantedAuthority> authorities = new HashSet<>();
        roleNames.stream()
                .filter(r -> !r.isBlank())
                .forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));

        scopes.stream()
                .filter(s -> !s.isBlank())
                .forEach(s -> authorities.add(new SimpleGrantedAuthority("SCOPE_" + s)));

        return authorities;
    }
}