package com.ntdoc.notangdoccore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

/**
 * Optional support config: provides an OAuth2AuthorizedClientManager you can
 * reuse later (e.g. server-to-server calls, or future BFF style token management).
 * For the current SPA front-end driven code+PKCE flow we still directly use
 * the token response clients in KeycloakClient.
 */
@Configuration
public class OAuth2ClientSupportConfig {

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(ClientRegistrationRepository registrations) {
        return new InMemoryOAuth2AuthorizedClientService(registrations);
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository registrations,
                                                                 OAuth2AuthorizedClientService clientService) {
        var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(registrations, clientService);
        manager.setAuthorizedClientProvider(
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .authorizationCode()
                        .refreshToken()
                        .build()
        );
        return manager;
    }
}