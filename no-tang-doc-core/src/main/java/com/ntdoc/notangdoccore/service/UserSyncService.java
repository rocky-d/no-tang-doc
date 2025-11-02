package com.ntdoc.notangdoccore.service;

import com.ntdoc.notangdoccore.entity.User;
import org.springframework.security.oauth2.jwt.Jwt;

public interface UserSyncService {
    User ensureLocalUser(String username,String email);

    User ensureFromJwt(Jwt jwt);
}
