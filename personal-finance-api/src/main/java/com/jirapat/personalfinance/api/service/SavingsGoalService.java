package com.jirapat.personalfinance.api.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jirapat.personalfinance.api.dto.request.ContributeRequest;
import com.jirapat.personalfinance.api.dto.request.CreateSavingsGoalRequest;
import com.jirapat.personalfinance.api.dto.request.UpdateSavingsGoalRequest;
import com.jirapat.personalfinance.api.dto.response.SavingsGoalResponse;
import com.jirapat.personalfinance.api.entity.Notification;
import com.jirapat.personalfinance.api.entity.NotificationType;
import com.jirapat.personalfinance.api.entity.SavingsContribution;
import com.jirapat.personalfinance.api.entity.SavingsGoal;
import com.jirapat.personalfinance.api.entity.SavingsGoalStatus;
import com.jirapat.personalfinance.api.entity.User;
import com.jirapat.personalfinance.api.exception.BadRequestException;
import com.jirapat.personalfinance.api.exception.ResourceNotFoundException;
import com.jirapat.personalfinance.api.exception.UnauthorizedException;
import com.jirapat.personalfinance.api.mapper.SavingsGoalMapper;
import com.jirapat.personalfinance.api.repository.NotificationRepository;
import com.jirapat.personalfinance.api.repository.SavingContributionRepository;
import com.jirapat.personalfinance.api.repository.SavingsGoalRepository;
import com.jirapat.personalfinance.api.repository.specification.SavingsGoalSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SavingsGoalService {

    private final SecurityService securityService;
    private final SavingsGoalRepository savingsGoalRepository;
    private final SavingContributionRepository savingContributionRepository;
    private final NotificationRepository notificationRepository;
    private final SavingsGoalMapper savingsGoalMapper;

    @Transactional(readOnly = true)
    public Page<SavingsGoalResponse> getAllSavingGoals(Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        Specification<SavingsGoal> spec = SavingsGoalSpecification.hasUserId(currentUserId);

        return savingsGoalRepository.findAll(spec, pageable).map(this::toResponseWithComputedFields);
    }

    @Transactional(readOnly = true)
    public SavingsGoalResponse getSavingGoalById(Long id) {
        SavingsGoal savingsGoal = findSavingGoalById(id);
        validateOwnership(savingsGoal);
        return toResponseWithComputedFields(savingsGoal);
    }

    public SavingsGoalResponse createSavingGoal(CreateSavingsGoalRequest request) {
        User currentUser = securityService.getCurrentUser();
        log.info("Creating saving goal '{}' for user id: {}", request.getName(), currentUser.getId());

        SavingsGoal savingsGoal = savingsGoalMapper.toEntity(request);
        savingsGoal.setCurrentAmount(BigDecimal.ZERO);
        savingsGoal.setStatus(SavingsGoalStatus.IN_PROGRESS);
        savingsGoal.setUser(currentUser);

        SavingsGoal saved = savingsGoalRepository.save(savingsGoal);
        return toResponseWithComputedFields(saved);
    }
    
    public SavingsGoalResponse createContribute(Long id, ContributeRequest request) {
        User currentUser = securityService.getCurrentUser();
        log.info("Creating contribute '{}' for user id: {}", id, currentUser.getId());

        SavingsGoal savingsGoal = findSavingGoalById(id);
        validateOwnership(savingsGoal);
        validateCanContribute(savingsGoal);

        SavingsContribution savingsContribution = savingsGoalMapper.toContributionEntity(request);
        savingsContribution.setGoal(savingsGoal);
        savingsContribution.setContributedAt(LocalDateTime.now());
        savingContributionRepository.save(savingsContribution);

        BigDecimal currentAmount = savingsGoal.getCurrentAmount() != null ? savingsGoal.getCurrentAmount() : BigDecimal.ZERO;
        savingsGoal.setCurrentAmount(currentAmount.add(request.getAmount()));

        boolean achievedNow = refreshGoalStatus(savingsGoal);
        SavingsGoal saved = savingsGoalRepository.save(savingsGoal);
        if (achievedNow) {
            createGoalAchievedNotification(saved);
        }

        return toResponseWithComputedFields(saved);
    }

    public SavingsGoalResponse updateSavingGoal(Long id, UpdateSavingsGoalRequest request) {
        User currentUser = securityService.getCurrentUser();
        log.info("Updating saving goal '{}' by user id: {}", id, currentUser.getEmail());

        SavingsGoal savingsGoal = findSavingGoalById(id);
        validateOwnership(savingsGoal);
        savingsGoalMapper.updateEntity(request, savingsGoal);

        boolean achievedNow = refreshGoalStatus(savingsGoal);
        SavingsGoal saved = savingsGoalRepository.save(savingsGoal);
        if (achievedNow) {
            createGoalAchievedNotification(saved);
        }

        return toResponseWithComputedFields(saved);
    }

    public void deleteSavingGoal(Long id) {
        log.info("Deleting saving goal: {}", id);
        SavingsGoal savingsGoal = findSavingGoalById(id);
        validateOwnership(savingsGoal);
        savingsGoalRepository.delete(savingsGoal);
        log.info("Saving goal deleted successfully: {}", id);
    }

    private SavingsGoalResponse toResponseWithComputedFields(SavingsGoal goal) {
        SavingsGoalResponse response = savingsGoalMapper.toSavingGoalResponse(goal);

        BigDecimal currentAmount = goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO;
        BigDecimal targetAmount = goal.getTargetAmount();
        BigDecimal remainingAmount = targetAmount.subtract(currentAmount).max(BigDecimal.ZERO);

        // progressPercentage
        BigDecimal progressPercentage = BigDecimal.ZERO;
        if (targetAmount.compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = currentAmount.multiply(BigDecimal.valueOf(100))
                    .divide(targetAmount, 2, RoundingMode.HALF_UP);
        }

        response.setRemainingAmount(remainingAmount);
        response.setProgressPercentage(progressPercentage);

        if (goal.getId() != null) {
            List<SavingsContribution> contributions = savingContributionRepository.findByGoalIdOrderByContributedAtAsc(goal.getId());
            response.setRecentContributions(savingContributionRepository.findTop5ByGoalIdOrderByContributedAtDesc(goal.getId()).stream()
                    .map(savingsGoalMapper::toContributionResponse)
                    .toList());
            response.setEstimatedCompletionDate(calculateEstimatedCompletionDate(remainingAmount, contributions));
        }

        // deadline-based calculations
        if (goal.getDeadline() != null) {
            long daysUntilDeadline = ChronoUnit.DAYS.between(LocalDate.now(), goal.getDeadline());
            response.setDaysUntilDeadline(daysUntilDeadline);

            if (daysUntilDeadline > 0 && remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal monthsRemaining = BigDecimal.valueOf(daysUntilDeadline)
                        .divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP);
                if (monthsRemaining.compareTo(BigDecimal.ZERO) > 0) {
                    response.setSuggestedMonthlyContribution(
                            remainingAmount.divide(monthsRemaining, 2, RoundingMode.CEILING));
                }
            }

            // isOnTrack: compare progress % vs time elapsed %
            long totalDays = ChronoUnit.DAYS.between(goal.getCreatedAt().toLocalDate(), goal.getDeadline());
            if (totalDays > 0) {
                long elapsedDays = ChronoUnit.DAYS.between(goal.getCreatedAt().toLocalDate(), LocalDate.now());
                BigDecimal timeElapsedPct = BigDecimal.valueOf(elapsedDays * 100)
                        .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP);
                response.setIsOnTrack(progressPercentage.compareTo(timeElapsedPct) >= 0);
            }
        }

        return response;
    }

    private LocalDate calculateEstimatedCompletionDate(BigDecimal remainingAmount, List<SavingsContribution> contributions) {
        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return LocalDate.now();
        }
        List<SavingsContribution> datedContributions = contributions.stream()
                .filter(contribution -> contribution.getContributedAt() != null)
                .toList();
        if (datedContributions.isEmpty()) {
            return null;
        }

        BigDecimal totalContributed = datedContributions.stream()
                .map(SavingsContribution::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalContributed.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        LocalDate firstContributionDate = datedContributions.get(0).getContributedAt().toLocalDate();
        long contributionDays = Math.max(1, ChronoUnit.DAYS.between(firstContributionDate, LocalDate.now()) + 1);
        BigDecimal averageDailyContribution = totalContributed
                .divide(BigDecimal.valueOf(contributionDays), 6, RoundingMode.HALF_UP);
        if (averageDailyContribution.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        long estimatedDays = remainingAmount.divide(averageDailyContribution, 0, RoundingMode.CEILING).longValue();
        return LocalDate.now().plusDays(estimatedDays);
    }

    private boolean refreshGoalStatus(SavingsGoal goal) {
        if (SavingsGoalStatus.CANCELLED.equals(goal.getStatus())) {
            return false;
        }

        boolean wasAchieved = SavingsGoalStatus.ACHIEVED.equals(goal.getStatus());
        BigDecimal currentAmount = goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO;
        if (currentAmount.compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(SavingsGoalStatus.ACHIEVED);
        } else {
            goal.setStatus(SavingsGoalStatus.IN_PROGRESS);
        }

        return !wasAchieved && SavingsGoalStatus.ACHIEVED.equals(goal.getStatus());
    }

    private void validateCanContribute(SavingsGoal goal) {
        if (SavingsGoalStatus.CANCELLED.equals(goal.getStatus())) {
            throw new BadRequestException("Cannot contribute to a cancelled savings goal");
        }
        if (SavingsGoalStatus.ACHIEVED.equals(goal.getStatus())) {
            throw new BadRequestException("Savings goal is already achieved");
        }
    }

    private void createGoalAchievedNotification(SavingsGoal goal) {
        Notification notification = Notification.builder()
                .user(goal.getUser())
                .title("Savings goal achieved")
                .message(String.format("You reached your savings goal: %s", goal.getName()))
                .type(NotificationType.GOAL_ACHIEVED)
                .isRead(false)
                .referenceType("SAVINGS_GOAL")
                .referenceId(goal.getId())
                .build();

        notificationRepository.save(notification);
    }

    private SavingsGoal findSavingGoalById(Long id) {
        return savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SavingsGoal", "id", id.toString()));
    }

    private void validateOwnership(SavingsGoal goal) {
        Long currentUserId = securityService.getCurrentUserId();
        if (!goal.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You do not have permission to access this savings goal");
        }
    }
}
