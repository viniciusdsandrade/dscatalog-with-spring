package com.restful.dscatalog.config.customgrant;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

public record CustomUserAuthorities(
        String username,
        Collection<? extends GrantedAuthority> authorities
) {
}