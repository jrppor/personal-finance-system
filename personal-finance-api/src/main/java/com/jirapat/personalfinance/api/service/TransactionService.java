package com.jirapat.personalfinance.api.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jirapat.personalfinance.api.dto.request.CreateTransactionRequest;
import com.jirapat.personalfinance.api.dto.request.UpdateTransactionRequest;
import com.jirapat.personalfinance.api.dto.response.AccountResponse;
import com.jirapat.personalfinance.api.dto.response.TransactionResponse;
import com.jirapat.personalfinance.api.entity.Account;
import com.jirapat.personalfinance.api.entity.AccountType;
import com.jirapat.personalfinance.api.entity.Category;
import com.jirapat.personalfinance.api.entity.Transaction;
import com.jirapat.personalfinance.api.entity.TransactionType;
import com.jirapat.personalfinance.api.entity.User;
import com.jirapat.personalfinance.api.exception.BadRequestException;
import com.jirapat.personalfinance.api.exception.ResourceNotFoundException;
import com.jirapat.personalfinance.api.exception.UnauthorizedException;
import com.jirapat.personalfinance.api.mapper.TransactionMapper;
import com.jirapat.personalfinance.api.repository.AccountRepository;
import com.jirapat.personalfinance.api.repository.CategoryRepository;
import com.jirapat.personalfinance.api.repository.TransactionRepository;
import com.jirapat.personalfinance.api.repository.specification.TransactionSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionService {

    private final SecurityService securityService;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAllTransactions(Long accountId,
            Long categoryId,
            TransactionType type,
            LocalDate dateFrom,
            LocalDate dateTo,
            BigDecimal min,
            BigDecimal max,
            Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        Specification<Transaction> spec = TransactionSpecification.hasUserId(currentUserId)
                .and(TransactionSpecification.transactionDateFrom(dateFrom))
                .and(TransactionSpecification.transactionDateTo(dateTo))
                .and(TransactionSpecification.hasAccountId(accountId))
                .and(TransactionSpecification.hasCategoryId(categoryId))
                .and(TransactionSpecification.hasType(type))
                .and(TransactionSpecification.amountMin(min))
                .and(TransactionSpecification.amountMax(max));

        return transactionRepository.findAll(spec, pageable)
                .map(this::toResponseWithTransferAccount);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id) {
        Long currentUserId = securityService.getCurrentUserId();
        Transaction transaction = findTransactionById(id);
        validateOwnership(transaction, currentUserId);
        return toResponseWithTransferAccount(transaction);
    }

    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        User currentUser = securityService.getCurrentUser();
        log.info("Creating {} transaction for user id: {}", request.getType(), currentUser.getId());

        Transaction transaction = transactionMapper.toEntity(request);
        transaction.setUser(currentUser);

        if (request.getCategoryId() != null) {
            Category category = findCategoryById(request.getCategoryId());
            transaction.setCategory(category);
        }

        Account account = findAccountById(request.getAccountId());
        validateAccountOwnership(account, currentUser.getId());
        transaction.setAccount(account);

        switch (transaction.getType()) {
            case INCOME -> {
                account.setBalance(account.getBalance().add(transaction.getAmount()));
            }
            case EXPENSE -> {
                validateSufficientBalance(account, transaction.getAmount());
                account.setBalance(account.getBalance().subtract(transaction.getAmount()));
            }
            case TRANSFER -> {
                if (request.getTransferToAccountId() == null) {
                    throw new BadRequestException("Transfer destination account is required");
                }
                if (request.getAccountId().equals(request.getTransferToAccountId())) {
                    throw new BadRequestException("Source and destination accounts must be different");
                }
                Account transferToAccount = findAccountById(request.getTransferToAccountId());
                validateAccountOwnership(transferToAccount, currentUser.getId());
                validateSufficientBalance(account, transaction.getAmount());
                account.setBalance(account.getBalance().subtract(transaction.getAmount()));
                transferToAccount.setBalance(transferToAccount.getBalance().add(transaction.getAmount()));
                accountRepository.save(transferToAccount);
            }
        }
        accountRepository.save(account);

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction created with id: {}", saved.getId());
        return toResponseWithTransferAccount(saved);
    }

    public TransactionResponse updateTransaction(Long id, UpdateTransactionRequest request) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("Updating transaction id: {}", id);

        Transaction transaction = findTransactionById(id);
        validateOwnership(transaction, currentUserId);

        // 1. Reverse old transaction effect
        reverseTransaction(transaction);

        // 2. Update transaction fields (type is now mapped by MapStruct)
        transactionMapper.updateEntity(request, transaction);

        if (request.getCategoryId() != null) {
            Category category = findCategoryById(request.getCategoryId());
            transaction.setCategory(category);
        } else {
            transaction.setCategory(null);
        }

        // 3. Apply new transaction effect
        applyTransaction(transaction, currentUserId);

        Transaction saved = transactionRepository.save(transaction);
        return toResponseWithTransferAccount(saved);
    }

    public void deleteTransaction(Long id) {
        Long currentUserId = securityService.getCurrentUserId();
        log.info("Deleting transaction id: {}", id);

        Transaction transaction = findTransactionById(id);
        validateOwnership(transaction, currentUserId);

        reverseTransaction(transaction);

        transactionRepository.delete(transaction);
        log.info("Transaction deleted successfully: {}", id);
    }

    // -- helpers --
    private TransactionResponse toResponseWithTransferAccount(Transaction transaction) {
        TransactionResponse response = transactionMapper.toTransactionResponse(transaction);
        if (transaction.getTransferToAccountId() != null) {
            accountRepository.findById(transaction.getTransferToAccountId())
                    .ifPresent(account -> response.setTransferToAccount(
                            AccountResponse.builder()
                                    .id(account.getId())
                                    .name(account.getName())
                                    .type(account.getType())
                                    .balance(account.getBalance())
                                    .isActive(account.getIsActive())
                                    .createdAt(account.getCreatedAt())
                                    .updatedAt(account.getUpdatedAt())
                                    .build()));
        }
        return response;
    }

    private void reverseTransaction(Transaction transaction) {
        Account account = transaction.getAccount();
        switch (transaction.getType()) {
            case INCOME -> account.setBalance(account.getBalance().subtract(transaction.getAmount()));
            case EXPENSE -> account.setBalance(account.getBalance().add(transaction.getAmount()));
            case TRANSFER -> {
                account.setBalance(account.getBalance().add(transaction.getAmount()));
                if (transaction.getTransferToAccountId() != null) {
                    Account transferToAccount = findAccountById(transaction.getTransferToAccountId());
                    transferToAccount.setBalance(transferToAccount.getBalance().subtract(transaction.getAmount()));
                    accountRepository.save(transferToAccount);
                }
            }
        }
        accountRepository.save(account);
    }

    private void applyTransaction(Transaction transaction, Long userId) {
        Account account = transaction.getAccount();
        switch (transaction.getType()) {
            case INCOME -> account.setBalance(account.getBalance().add(transaction.getAmount()));
            case EXPENSE -> {
                validateSufficientBalance(account, transaction.getAmount());
                account.setBalance(account.getBalance().subtract(transaction.getAmount()));
            }
            case TRANSFER -> {
                if (transaction.getTransferToAccountId() == null) {
                    throw new BadRequestException("Transfer destination account is required");
                }
                if (transaction.getAccount().getId().equals(transaction.getTransferToAccountId())) {
                    throw new BadRequestException("Source and destination accounts must be different");
                }
                Account transferToAccount = findAccountById(transaction.getTransferToAccountId());
                validateAccountOwnership(transferToAccount, userId);
                validateSufficientBalance(account, transaction.getAmount());
                account.setBalance(account.getBalance().subtract(transaction.getAmount()));
                transferToAccount.setBalance(transferToAccount.getBalance().add(transaction.getAmount()));
                accountRepository.save(transferToAccount);
            }
        }
        accountRepository.save(account);
    }

    private void validateSufficientBalance(Account account, BigDecimal amount) {
        if (account.getType() != AccountType.CREDIT_CARD
                && account.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient balance in account: " + account.getName());
        }
    }

    private void validateOwnership(Transaction transaction, Long userId) {
        if (!transaction.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to access this transaction");
        }
    }

    private void validateAccountOwnership(Account account, Long userId) {
        if (!account.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to access this account");
        }
    }

    private Transaction findTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id.toString()));
    }

    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id.toString()));
    }

    private Account findAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id.toString()));
    }
}
