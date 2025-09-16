package com.ntdoc.notangdoccore.service;

import com.ntdoc.notangdoccore.entity.User;
import com.ntdoc.notangdoccore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
}
