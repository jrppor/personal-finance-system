package com.jirapat.personalfinance.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.jirapat.personalfinance.api.dto.request.ContributeRequest;
import com.jirapat.personalfinance.api.dto.request.CreateSavingsGoalRequest;
import com.jirapat.personalfinance.api.dto.request.UpdateSavingsGoalRequest;
import com.jirapat.personalfinance.api.dto.response.SavingsContributionResponse;
import com.jirapat.personalfinance.api.dto.response.SavingsGoalResponse;
import com.jirapat.personalfinance.api.entity.SavingsContribution;
import com.jirapat.personalfinance.api.entity.SavingsGoal;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SavingsGoalMapper {

    @Mapping(target = "progressPercentage", ignore = true)
    @Mapping(target = "remainingAmount", ignore = true)
    @Mapping(target = "estimatedCompletionDate", ignore = true)
    @Mapping(target = "daysUntilDeadline", ignore = true)
    @Mapping(target = "isOnTrack", ignore = true)
    @Mapping(target = "suggestedMonthlyContribution", ignore = true)
    SavingsGoalResponse toSavingGoalResponse(SavingsGoal savingsGoal);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "currentAmount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    SavingsGoal toEntity(CreateSavingsGoalRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "currentAmount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(UpdateSavingsGoalRequest request, @MappingTarget SavingsGoal savingsGoal);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "goal", ignore = true)
    @Mapping(target = "contributedAt", ignore = true)
    SavingsContribution toContributionEntity(ContributeRequest request);

    SavingsContributionResponse toContributionResponse(SavingsContribution savingsContribution);
}
