package com.jirapat.personalfinance.api.controller;

import com.jirapat.personalfinance.api.dto.request.ContributeRequest;
import com.jirapat.personalfinance.api.dto.request.CreateSavingsGoalRequest;
import com.jirapat.personalfinance.api.dto.request.UpdateSavingsGoalRequest;
import com.jirapat.personalfinance.api.dto.response.ApiResponse;
import com.jirapat.personalfinance.api.dto.response.SavingsGoalResponse;
import com.jirapat.personalfinance.api.service.SavingsGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/savings-goals")
@Tag(name = "SavingsGoals", description = "Savings Goals endpoints")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    @GetMapping
    @Operation(summary = "Get all savings goals for current user")
    public ResponseEntity<ApiResponse<Page<SavingsGoalResponse>>> getAllSavingsGoals(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<SavingsGoalResponse> response = savingsGoalService.getAllSavingGoals(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get savings goal by ID")
    public ResponseEntity<ApiResponse<SavingsGoalResponse>> getSavingGoalById(@PathVariable Long id) {
        SavingsGoalResponse response = savingsGoalService.getSavingGoalById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Create a new savings goal")
    public ResponseEntity<ApiResponse<SavingsGoalResponse>> createSavingGoal(
            @Valid @RequestBody CreateSavingsGoalRequest request
            ) {
        SavingsGoalResponse response = savingsGoalService.createSavingGoal(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Savings goal created successfully"));
    }



    @PutMapping("/{id}")
    @Operation(summary = "Update savings goal")
    public ResponseEntity<ApiResponse<SavingsGoalResponse>> updateSavingGoal(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSavingsGoalRequest request) {

        SavingsGoalResponse response = savingsGoalService.updateSavingGoal(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSavingGoal(@PathVariable Long id) {
        savingsGoalService.deleteSavingGoal(id);
        return ResponseEntity.ok(ApiResponse.success("Saving goal deleted successfully"));
    }

    @PostMapping("/{id}/contribute")
    public ResponseEntity<ApiResponse<SavingsGoalResponse>> createContribute(
            @PathVariable Long id,
            @Valid @RequestBody ContributeRequest request
    ){
        SavingsGoalResponse response = savingsGoalService.createContribute(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
