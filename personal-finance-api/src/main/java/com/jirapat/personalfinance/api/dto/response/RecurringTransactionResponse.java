package com.jirapat.personalfinance.api.dto.response;

import com.jirapat.personalfinance.api.entity.Frequency;
import com.jirapat.personalfinance.api.entity.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class RecurringTransactionResponse {
    private Long id;
    private AccountResponse account;
    private CategoryResponse category;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private Frequency frequency;
    private LocalDate nextOccurrence;
    private LocalDate endDate;
    private Boolean isActive;
    private LocalDateTime lastExecutedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

