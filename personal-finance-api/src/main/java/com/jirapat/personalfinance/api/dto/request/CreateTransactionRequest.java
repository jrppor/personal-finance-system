package com.jirapat.personalfinance.api.dto.request;

import com.jirapat.personalfinance.api.entity.TransactionType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class CreateTransactionRequest {

    @NotNull(message = "Account ID is required")
    private Long accountId;

    private Long categoryId;
    private Long transferToAccountId;

    @NotNull(message = "Transaction type is required")
    private String type;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    private String description;
    private String note;

    @NotNull(message = "Transaction date is required")
    private LocalDate transactionDate;
}
