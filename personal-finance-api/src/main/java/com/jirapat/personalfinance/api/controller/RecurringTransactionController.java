package com.jirapat.personalfinance.api.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jirapat.personalfinance.api.dto.request.CreateRecurringTransactionRequest;
import com.jirapat.personalfinance.api.dto.request.UpdateRecurringTransactionRequest;
import com.jirapat.personalfinance.api.dto.response.ApiResponse;
import com.jirapat.personalfinance.api.dto.response.RecurringTransactionResponse;
import com.jirapat.personalfinance.api.entity.Frequency;
import com.jirapat.personalfinance.api.entity.TransactionType;
import com.jirapat.personalfinance.api.service.RecurringTransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recurring-transactions")
@Tag(name = "RecurringTransactions", description = "")
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    @GetMapping
    @Operation(summary = "Get all recurring transactions")
    public ResponseEntity<ApiResponse<Page<RecurringTransactionResponse>>> getAllRecurringTransactions(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Frequency frequency,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<RecurringTransactionResponse> response = recurringTransactionService.getAllRecurringTransactions(
                accountId, categoryId, type, isActive, frequency, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get recurring transaction by ID")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> getRecurringTransactionById(
            @PathVariable Long id
    ) {
        RecurringTransactionResponse response = recurringTransactionService.getRecurringTransactionById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    @PostMapping
    @Operation(summary = "Create recurring transaction")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> createRecurringTransaction(
            @Valid @RequestBody CreateRecurringTransactionRequest request
            ) {
        RecurringTransactionResponse response = recurringTransactionService.createRecurringTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Recurring transaction created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update recurring transaction")
    public ResponseEntity<ApiResponse<RecurringTransactionResponse>> updateRecurringTransaction(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRecurringTransactionRequest request
    ) {
        RecurringTransactionResponse response = recurringTransactionService.updateRecurringTransaction(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Recurring transaction updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate recurring transaction")
    public ResponseEntity<ApiResponse<Void>> deleteRecurringTransaction(@PathVariable Long id) {
        recurringTransactionService.deleteRecurringTransaction(id);
        return ResponseEntity.ok(ApiResponse.success("Recurring transaction deactivated successfully"));
    }
}
