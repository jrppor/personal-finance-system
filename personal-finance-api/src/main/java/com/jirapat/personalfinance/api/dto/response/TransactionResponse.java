package com.jirapat.personalfinance.api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.jirapat.personalfinance.api.entity.TransactionType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionResponse {
    private Long id;
    private AccountResponse account;
    private CategoryResponse category;
    private AccountResponse transferToAccount;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private String note;
    private LocalDate transactionDate;
    private String attachmentUrl;
    private LocalDateTime createdAt;
}
