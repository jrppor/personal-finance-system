package com.jirapat.personalfinance.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.jirapat.personalfinance.api.entity.AccountType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountResponse {
    private Long id;
    private String user;
    private String name;
    private AccountType type;
    private BigDecimal balance;
    private String currency;
    private String color;
    private String icon;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
