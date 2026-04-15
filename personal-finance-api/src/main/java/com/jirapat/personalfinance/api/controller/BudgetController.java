package com.jirapat.personalfinance.api.controller;

import java.util.List;

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

import com.jirapat.personalfinance.api.dto.request.CreateBudgetRequest;
import com.jirapat.personalfinance.api.dto.request.UpdateBudgetRequest;
import com.jirapat.personalfinance.api.dto.response.ApiResponse;
import com.jirapat.personalfinance.api.dto.response.BudgetResponse;
import com.jirapat.personalfinance.api.dto.response.BudgetStatusResponse;
import com.jirapat.personalfinance.api.entity.BudgetPeriod;
import com.jirapat.personalfinance.api.service.BudgetService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/budgets")
@Tag(name = "Budgets", description = "Budget management endpoints")
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    @Operation(summary = "Get all budgets for current user")
    public ResponseEntity<ApiResponse<Page<BudgetResponse>>> getAllBudgets(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BudgetPeriod period,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<BudgetResponse> response = budgetService.getAllBudgets(categoryId, period, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status")
    @Operation(summary = "Get status summary of all active budgets")
    public ResponseEntity<ApiResponse<List<BudgetStatusResponse>>> getAllBudgetStatuses() {
        List<BudgetStatusResponse> response = budgetService.getAllBudgetStatuses();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get budget by ID")
    public ResponseEntity<ApiResponse<BudgetResponse>> getBudgetById(@PathVariable Long id) {
        BudgetResponse response = budgetService.getBudgetById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Get budget status with spending analysis")
    public ResponseEntity<ApiResponse<BudgetStatusResponse>> getBudgetStatus(@PathVariable Long id) {
        BudgetStatusResponse response = budgetService.getBudgetStatus(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create a new budget")
    public ResponseEntity<ApiResponse<BudgetResponse>> createBudget(
            @Valid @RequestBody CreateBudgetRequest request
    ) {
        BudgetResponse response = budgetService.createBudget(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Budget created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing budget")
    public ResponseEntity<ApiResponse<BudgetResponse>> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBudgetRequest request
    ) {
        BudgetResponse response = budgetService.updateBudget(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Budget updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a budget (soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(
            @PathVariable Long id
    ) {
        budgetService.deleteBudget(id);
        return ResponseEntity.ok(ApiResponse.success("Budget deleted successfully"));
    }
}
