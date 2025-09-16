package com.ntdoc.notangdoccore.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

public class JwtRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    private static final String REALM_ACCESS = "realm_access";
    private static final String ROLES = "roles";

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        if (jwt.getClaims() == null) return List.of();
        Object realmAccessObj = jwt.getClaims().get(REALM_ACCESS);
        if (!(realmAccessObj instanceof Map<?,?> realmMap)) return List.of();
        Object rolesObj = realmMap.get(ROLES);
        if (!(rolesObj instanceof Collection<?> col)) return List.of();
        return col.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}

