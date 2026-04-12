package com.jirapat.personalfinance.api.entity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Refresh token stored in Redis.
 * Key pattern: refresh_token:{token} -> RefreshToken object
 * User index: user_refresh_tokens:{userId} -> Set of token strings
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken implements Serializable {

    private String token;
    private Long userId;
    private long ttlSeconds;
}
