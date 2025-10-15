package com.ntdoc.notangdoccore.keycloak;

import com.ntdoc.notangdoccore.config.KeycloakProperties;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KeycloakAdminService {
    private final KeycloakProperties props;

    private Keycloak adminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(props.getAuthServerUrl())
                .realm(props.getAdmin().getRealm())
                .grantType(OAuth2Constants.PASSWORD)
                .clientId(props.getAdmin().getClientId())
                .username(props.getAdmin().getUsername())
                .password(props.getAdmin().getPassword())
                .build();
    }

    public String createUser(String username, String email, String rawPassword, List<String> roles) {
        try (Keycloak kc = adminClient()) {
            RealmResource realm = kc.realm(props.getRealm());
            UsersResource users = realm.users();

            UserRepresentation user = new UserRepresentation();
            user.setUsername(username);
            user.setEmail(email);
            user.setEnabled(true);
            user.setEmailVerified(false);

            Response response = users.create(user);
            if (response.getStatus() >= 300) {
                throw new IllegalStateException("Create user failed status=" + response.getStatus());
            }
            String userId = CreatedResponseUtil.getCreatedId(response);

            if (rawPassword != null && !rawPassword.isBlank()) {
                CredentialRepresentation cred = new CredentialRepresentation();
                cred.setTemporary(false);
                cred.setType(CredentialRepresentation.PASSWORD);
                cred.setValue(rawPassword);
                users.get(userId).resetPassword(cred);
            }

            if (roles != null && !roles.isEmpty()) {
                List<RoleRepresentation> all = realm.roles().list();
                List<RoleRepresentation> attach = all.stream()
                        .filter(r -> roles.contains(r.getName()))
                        .toList();
                if (!attach.isEmpty()) {
                    users.get(userId).roles().realmLevel().add(attach);
                }
            }
            return userId;
        }
    }

    public Optional<String> findUserIdByUsername(String username) {
        try (Keycloak kc = adminClient()) {
            List<UserRepresentation> list = kc.realm(props.getRealm()).users().search(username, true);
            return list.stream().filter(u -> username.equals(u.getUsername())).findFirst().map(UserRepresentation::getId);
        }
    }
}

