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
import com.jirapat.personalfinance.api.dto.response.TransactionResponse;
import com.jirapat.personalfinance.api.entity.Account;
import com.jirapat.personalfinance.api.entity.AccountType;
import com.jirapat.personalfinance.api.entity.Category;
import com.jirapat.personalfinance.api.entity.Transaction;
import com.jirapat.personalfinance.api.entity.TransactionType;
import com.jirapat.personalfinance.api.entity.User;
import com.jirapat.personalfinance.api.exception.BadRequestException;
import com.jirapat.personalfinance.api.exception.ResourceNotFoundException;
import com.jirapat.personalfinance.api.mapper.AccountMapper;
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
    private final AccountMapper accountMapper;

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAllTransactions(Long accountId, Long categoryId, TransactionType type, LocalDate dateFrom, LocalDate dateTo, BigDecimal min, BigDecimal max, Pageable pageable) {
        Long currentUserId = securityService.getCurrentUserId();
        Specification<Transaction> spec = TransactionSpecification.hasUserId(currentUserId)
                .and(TransactionSpecification.createdAfter(dateFrom))
                .and(TransactionSpecification.createdBefore(dateTo))
                .and(TransactionSpecification.hasAccountId(accountId))
                .and(TransactionSpecification.hasCategoryId(categoryId))
                .and(TransactionSpecification.hasType(type))
                .and(TransactionSpecification.amountBetween(min, max));

        return transactionRepository.findAll(spec, pageable)
                .map(transactionMapper::toTransactionResponse);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id) {
        log.info("Fetching Transaction by id: {}", id);
        Transaction transaction = findTransactionById(id);
        verifyTransactionOwnership(transaction);
        return transactionMapper.toTransactionResponse(transaction);
    }

    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        User currentUser = securityService.getCurrentUser();
        log.info("Creating transaction by user: {}", currentUser);

        Transaction transaction = transactionMapper.toEntity(request);
        transaction.setUser(currentUser);

        if (request.getCategoryId() != null) {
            Category category = findCategoryById(request.getCategoryId());
            transaction.setCategory(category);
        }

        Account account = findAccountById(request.getAccountId());
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
                validateSufficientBalance(account, transaction.getAmount());
                account.setBalance(account.getBalance().subtract(transaction.getAmount()));
                transferToAccount.setBalance(transferToAccount.getBalance().add(transaction.getAmount()));
                accountRepository.save(transferToAccount);
            }
        }
        accountRepository.save(account);

        Transaction saved = transactionRepository.save(transaction);
        return transactionMapper.toTransactionResponse(saved);
    }

    public TransactionResponse updateTransaction(Long id, UpdateTransactionRequest request) {
        User currentUser = securityService.getCurrentUser();
        log.info("Updating transaction {} by user: {}", id, currentUser.getEmail());

        Transaction transaction = findTransactionById(id);
        verifyTransactionOwnership(transaction);

        // 1. Reverse old transaction effect
        reverseTransaction(transaction);

        // 2. Update transaction fields
        transactionMapper.updateEntity(request, transaction);
        transaction.setType(TransactionType.valueOf(request.getType()));

        if (request.getCategoryId() != null) {
            Category category = findCategoryById(request.getCategoryId());
            transaction.setCategory(category);
        }

        // 3. Apply new transaction effect
        applyTransaction(transaction);

        Transaction saved = transactionRepository.save(transaction);
        return transactionMapper.toTransactionResponse(saved);
    }

    public void deleteTransaction(Long id) {
        log.info("Deleting transaction: {}", id);
        Transaction transaction = findTransactionById(id);
        verifyTransactionOwnership(transaction);

        reverseTransaction(transaction);

        transactionRepository.delete(transaction);
        log.info("Transaction deleted successfully: {}", id);
    }

    private void reverseTransaction(Transaction transaction) {
        Account account = transaction.getAccount();
        switch (transaction.getType()) {
            case INCOME -> {
                account.setBalance(account.getBalance().subtract(transaction.getAmount()));
            }
            case EXPENSE -> {
                account.setBalance(account.getBalance().add(transaction.getAmount()));
            }
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

    private void applyTransaction(Transaction transaction) {
        Account account = transaction.getAccount();
        switch (transaction.getType()) {
            case INCOME -> {
                account.setBalance(account.getBalance().add(transaction.getAmount()));
            }
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

    private void verifyTransactionOwnership(Transaction transaction) {
        Long currentUserId = securityService.getCurrentUserId();
        if (!transaction.getUser().getId().equals(currentUserId)) {
            throw new ResourceNotFoundException("Transaction", "id", transaction.getId().toString());
        }
    }

    public Transaction findTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", id.toString()));
    }

    public Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id.toString()));
    }

    public Account findAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id.toString()));
    }
}
