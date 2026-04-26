package com.jirapat.personalfinance.api.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.jirapat.personalfinance.api.entity.Frequency;
import com.jirapat.personalfinance.api.entity.TransactionType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateRecurringTransactionRequest {

    @NotNull(message = "Account ID is required")
    private Long accountId;

    private Long categoryId;

    @NotNull(message = "Transaction type is required")
    private TransactionType type;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotNull(message = "Frequency is required")
    private Frequency frequency;

    @NotNull(message = "Next occurrence is required")
    private LocalDate nextOccurrence;

    private LocalDate endDate;

    @NotNull(message = "IsActive is required")
    private Boolean isActive;
}
