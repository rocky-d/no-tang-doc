package com.ntdoc.notangdoccore.service;

import com.ntdoc.notangdoccore.entity.User;
import com.ntdoc.notangdoccore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserSyncService {
    private final UserRepository userRepository;

    @Transactional
    public User ensureLocalUser(String username, String email) {
        Optional<User> existing = userRepository.findByUsername(username);
        if (existing.isPresent()) {
            User u = existing.get();
            boolean changed = false;
            if (email != null && (u.getEmail() == null || !email.equals(u.getEmail()))) {
                u.setEmail(email);
                changed = true;
            }
            return changed ? userRepository.save(u) : u;
        }
        User u = User.builder()
                .username(username)
                .email(email)
                .build();
        return userRepository.save(u);
    }

    @Transactional
    public User ensureFromJwt(Jwt jwt){
        // Get the keycloak UUID
        String sub = jwt.getSubject();
        if (sub == null || sub.isBlank()){
            throw new IllegalArgumentException("JWT Subject is required");
        }

        String preferedUsername = jwt.getClaim("preferred_username");
        String claimEmail = jwt.getClaim("email");

        String username = preferedUsername == null ? claimEmail : preferedUsername;

        Optional<User> existing = userRepository.findByKcUserId(sub);
        if(existing.isPresent()){
            User user = existing.get();
            boolean changed = false;

            if(!user.getUsername().equals(username)){
                user.setUsername(username);
                changed = true;
            }
            if(!user.getEmail().equals(claimEmail)){
                user.setEmail(claimEmail);
                changed = true;
            }

            return changed ? userRepository.save(user) : user;
        }

        try{
            User user = User.builder()
                    .kcUserId(sub)
                    .username(username)
                    .email(claimEmail)
                    .build();
            return userRepository.save(user);
        } catch(DataIntegrityViolationException e){
            return userRepository.findByKcUserId(sub).orElseThrow(()->e);
        }
    }
}
