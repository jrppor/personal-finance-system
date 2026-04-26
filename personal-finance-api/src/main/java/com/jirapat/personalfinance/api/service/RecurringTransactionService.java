package com.jirapat.personalfinance.api.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.jirapat.personalfinance.api.dto.request.CreateRecurringTransactionRequest;
import com.jirapat.personalfinance.api.dto.request.UpdateRecurringTransactionRequest;
import com.jirapat.personalfinance.api.dto.response.RecurringTransactionResponse;
import com.jirapat.personalfinance.api.entity.Account;
import com.jirapat.personalfinance.api.entity.AccountType;
import com.jirapat.personalfinance.api.entity.Category;
import com.jirapat.personalfinance.api.entity.Frequency;
import com.jirapat.personalfinance.api.entity.RecurringTransaction;
import com.jirapat.personalfinance.api.entity.Transaction;
import com.jirapat.personalfinance.api.entity.TransactionType;
import com.jirapat.personalfinance.api.entity.User;
import com.jirapat.personalfinance.api.exception.BadRequestException;
import com.jirapat.personalfinance.api.exception.ResourceNotFoundException;
import com.jirapat.personalfinance.api.exception.UnauthorizedException;
import com.jirapat.personalfinance.api.mapper.RecurringTransactionMapper;
import com.jirapat.personalfinance.api.repository.AccountRepository;
import com.jirapat.personalfinance.api.repository.CategoryRepository;
import com.jirapat.personalfinance.api.repository.RecurringTransactionRepository;
import com.jirapat.personalfinance.api.repository.TransactionRepository;
import com.jirapat.personalfinance.api.repository.specification.RecurringTransactionSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RecurringTransactionService {

    private final SecurityService securityService;
    private final RecurringTransactionRepository recurringTransactionRepository;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final RecurringTransactionMapper recurringTransactionMapper;
    private final TransactionTemplate transactionTemplate;

    @Transactional(readOnly = true)
    public Page<RecurringTransactionResponse> getAllRecurringTransactions(Long accountId,
                                                                          Long categoryId,
                                                                          TransactionType type,
                                                                          Boolean isActive,
                                                                          Frequency frequency,
                                                                          Pageable pageable) {

        Long currentUserId = securityService.getCurrentUserId();
        Specification<RecurringTransaction> spec = RecurringTransactionSpecification.hasUserId(currentUserId)
                .and(RecurringTransactionSpecification.hasAccountId(accountId))
                .and(RecurringTransactionSpecification.hasCategoryId(categoryId))
                .and(RecurringTransactionSpecification.hasType(type))
                .and(RecurringTransactionSpecification.hasIsActive(isActive))
                .and(RecurringTransactionSpecification.hasFrequency(frequency));

        return recurringTransactionRepository.findAll(spec, pageable)
                .map(recurringTransactionMapper::toRecurringTransactionResponse);

    }

    @Transactional(readOnly = true)
    public RecurringTransactionResponse getRecurringTransactionById(Long id) {
        Long currentUserId = securityService.getCurrentUserId();
        RecurringTransaction recurringTransaction = findRecurringTransaction(id);
        validateOwnership(recurringTransaction, currentUserId);

        return recurringTransactionMapper.toRecurringTransactionResponse(recurringTransaction);
    }

    public RecurringTransactionResponse createRecurringTransaction(CreateRecurringTransactionRequest request) {
        User currentUser = securityService.getCurrentUser();
        Long currentUserId = currentUser.getId();
        log.info("Creating recurring transaction for account id: {}", request.getAccountId());

        validateRecurringType(request.getType());
        validateSchedule(request.getNextOccurrence(), request.getEndDate());

        RecurringTransaction recurringTransaction = recurringTransactionMapper.toEntity(request);
        recurringTransaction.setUser(currentUser);

        Category category = resolveCategory(request.getCategoryId(), currentUserId, request.getType());
        recurringTransaction.setCategory(category);

        Account account = findAccountById(request.getAccountId());
        validateAccountOwnership(account, currentUserId);
        recurringTransaction.setAccount(account);

        RecurringTransaction saved = recurringTransactionRepository.save(recurringTransaction);
        log.info("Recurring transaction created with id: {}", saved.getId());
        return recurringTransactionMapper.toRecurringTransactionResponse(saved);
    }

    public RecurringTransactionResponse updateRecurringTransaction(Long id, UpdateRecurringTransactionRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("Updating recurring transaction id: {}", id);

        validateRecurringType(request.getType());
        validateSchedule(request.getNextOccurrence(), request.getEndDate());

        RecurringTransaction recurringTransaction = findRecurringTransaction(id);
        validateOwnership(recurringTransaction, currentUserId);

        recurringTransactionMapper.updateEntity(request, recurringTransaction);

        Category category = resolveCategory(request.getCategoryId(), currentUserId, request.getType());
        recurringTransaction.setCategory(category);

        Account account = findAccountById(request.getAccountId());
        validateAccountOwnership(account, currentUserId);
        recurringTransaction.setAccount(account);

        RecurringTransaction saved = recurringTransactionRepository.save(recurringTransaction);
        return recurringTransactionMapper.toRecurringTransactionResponse(saved);
    }

    public void deleteRecurringTransaction(Long id) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("Deactivating recurring transaction id: {}", id);

        RecurringTransaction recurringTransaction = findRecurringTransaction(id);
        validateOwnership(recurringTransaction, currentUserId);

        recurringTransaction.setIsActive(false);
        recurringTransactionRepository.save(recurringTransaction);
        log.info("Recurring transaction deactivated successfully: {}", id);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public int processDueRecurringTransactions() {
        return processDueRecurringTransactions(LocalDate.now());
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public int processDueRecurringTransactions(LocalDate processingDate) {
        List<RecurringTransaction> dueRecurringTransactions = recurringTransactionRepository.findDueForProcessing(processingDate);
        int processedCount = 0;

        for (RecurringTransaction dueRecurringTransaction : dueRecurringTransactions) {
            Long recurringTransactionId = dueRecurringTransaction.getId();
            try {
                Integer processedForSchedule = transactionTemplate.execute(status -> processRecurringTransaction(recurringTransactionId, processingDate));
                processedCount += processedForSchedule == null ? 0 : processedForSchedule;
            } catch (RuntimeException exception) {
                log.error("Failed to process recurring transaction id: {}", recurringTransactionId, exception);
            }
        }

        return processedCount;
    }

    private RecurringTransaction findRecurringTransaction(Long id) {
        return recurringTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RecurringTransaction", "id", id.toString()));
    }

    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id.toString()));
    }

    private Account findAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id.toString()));
    }

    private int processRecurringTransaction(Long recurringTransactionId, LocalDate processingDate) {
        RecurringTransaction recurringTransaction = findRecurringTransaction(recurringTransactionId);
        int processedCount = 0;

        while (isDue(recurringTransaction, processingDate)) {
            LocalDate occurrenceDate = recurringTransaction.getNextOccurrence();

            if (isPastEndDate(recurringTransaction, occurrenceDate)) {
                recurringTransaction.setIsActive(false);
                break;
            }

            createTransactionFromRecurring(recurringTransaction, occurrenceDate);
            processedCount++;

            recurringTransaction.setLastExecutedAt(LocalDateTime.now());
            recurringTransaction.setNextOccurrence(calculateNextOccurrence(occurrenceDate, recurringTransaction.getFrequency()));

            if (isPastEndDate(recurringTransaction, recurringTransaction.getNextOccurrence())) {
                recurringTransaction.setIsActive(false);
                break;
            }
        }

        recurringTransactionRepository.save(recurringTransaction);
        return processedCount;
    }

    private void createTransactionFromRecurring(RecurringTransaction recurringTransaction, LocalDate occurrenceDate) {
        validateRecurringType(recurringTransaction.getType());

        Account account = recurringTransaction.getAccount();
        applyBalanceChange(account, recurringTransaction.getType(), recurringTransaction.getAmount());
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .user(recurringTransaction.getUser())
                .account(account)
                .category(recurringTransaction.getCategory())
                .type(recurringTransaction.getType())
                .amount(recurringTransaction.getAmount())
                .description(recurringTransaction.getDescription())
                .transactionDate(occurrenceDate)
                .build();

        transactionRepository.save(transaction);
    }

    private void applyBalanceChange(Account account, TransactionType type, BigDecimal amount) {
        switch (type) {
            case INCOME -> account.setBalance(account.getBalance().add(amount));
            case EXPENSE -> {
                validateSufficientBalance(account, amount);
                account.setBalance(account.getBalance().subtract(amount));
            }
            case TRANSFER -> throw new BadRequestException("Recurring transactions support only INCOME or EXPENSE");
        }
    }

    private LocalDate calculateNextOccurrence(LocalDate occurrenceDate, Frequency frequency) {
        return switch (frequency) {
            case DAILY -> occurrenceDate.plusDays(1);
            case WEEKLY -> occurrenceDate.plusWeeks(1);
            case BIWEEKLY -> occurrenceDate.plusWeeks(2);
            case MONTHLY -> occurrenceDate.plusMonths(1);
            case YEARLY -> occurrenceDate.plusYears(1);
        };
    }

    private boolean isDue(RecurringTransaction recurringTransaction, LocalDate processingDate) {
        return Boolean.TRUE.equals(recurringTransaction.getIsActive())
                && recurringTransaction.getNextOccurrence() != null
                && !recurringTransaction.getNextOccurrence().isAfter(processingDate);
    }

    private boolean isPastEndDate(RecurringTransaction recurringTransaction, LocalDate occurrenceDate) {
        return recurringTransaction.getEndDate() != null && occurrenceDate.isAfter(recurringTransaction.getEndDate());
    }

    private Category resolveCategory(Long categoryId, Long userId, TransactionType type) {
        if (categoryId == null) {
            return null;
        }

        Category category = findCategoryById(categoryId);
        validateCategoryOwnership(category, userId);
        validateCategoryType(category, type);
        return category;
    }

    private void validateRecurringType(TransactionType type) {
        if (type == null) {
            throw new BadRequestException("Transaction type is required");
        }
        if (type == TransactionType.TRANSFER) {
            throw new BadRequestException("Recurring transactions support only INCOME or EXPENSE");
        }
    }

    private void validateSchedule(LocalDate nextOccurrence, LocalDate endDate) {
        if (nextOccurrence == null) {
            throw new BadRequestException("Next occurrence is required");
        }
        if (endDate != null && endDate.isBefore(nextOccurrence)) {
            throw new BadRequestException("End date must be on or after next occurrence");
        }
    }

    private void validateOwnership(RecurringTransaction recurringTransaction, Long userId) {
        if (!recurringTransaction.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to access this recurring transaction");
        }
    }

    private void validateAccountOwnership(Account account, Long userId) {
        if (!account.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to access this account");
        }
    }

    private void validateCategoryOwnership(Category category, Long userId) {
        if (category.getUser() != null && !category.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to access this category");
        }
    }

    private void validateCategoryType(Category category, TransactionType type) {
        if (!category.getType().name().equals(type.name())) {
            throw new BadRequestException("Category type must match transaction type");
        }
    }

    private void validateSufficientBalance(Account account, BigDecimal amount) {
        if (account.getType() != AccountType.CREDIT_CARD && account.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient balance in account: " + account.getName());
        }
    }
}
