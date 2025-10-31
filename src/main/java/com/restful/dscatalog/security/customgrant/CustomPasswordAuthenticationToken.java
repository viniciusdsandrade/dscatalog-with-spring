package com.restful.dscatalog.security.customgrant;

import java.util.*;

import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import static java.util.Objects.requireNonNull;

@Getter
public class CustomPasswordAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {
    private final String username;
    private final String password;
    private final Set<String> scopes;

    public CustomPasswordAuthenticationToken(
            Authentication clientPrincipal,
            @Nullable Set<String> scopes,
            @Nullable Map<String, Object> additionalParameters
    ) {

        super(new AuthorizationGrantType("password"), clientPrincipal, additionalParameters);

        this.username = (String) requireNonNull(additionalParameters).get("username");
        this.password = (String) additionalParameters.get("password");
        this.scopes = Collections.unmodifiableSet(
                scopes != null ? new HashSet<>(scopes) : Collections.emptySet());
    }
}
