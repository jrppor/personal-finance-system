package com.jirapat.personalfinance.api.service;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jirapat.personalfinance.api.dto.request.CreateAccountRequest;
import com.jirapat.personalfinance.api.dto.request.UpdateAccountRequest;
import com.jirapat.personalfinance.api.dto.response.AccountResponse;
import com.jirapat.personalfinance.api.entity.Account;
import com.jirapat.personalfinance.api.entity.User;
import com.jirapat.personalfinance.api.exception.ResourceNotFoundException;
import com.jirapat.personalfinance.api.exception.UnauthorizedException;
import com.jirapat.personalfinance.api.mapper.AccountMapper;
import com.jirapat.personalfinance.api.repository.AccountRepository;
import com.jirapat.personalfinance.api.repository.specification.AccountSpecification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountService {

    private final SecurityService securityService;
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public Page<AccountResponse> getAllAccounts(
            LocalDate dateFrom,
            LocalDate dateTo,
            Pageable pageable
    ){
        Long currentUserId = securityService.getCurrentUserId();

        Specification<Account> spec = AccountSpecification.hasUserId(currentUserId)
                .and(AccountSpecification.createdAfter(dateFrom))
                .and(AccountSpecification.createdBefore(dateTo));

        return accountRepository.findAll(spec, pageable)
                .map(accountMapper::toListResponse);
    }


    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id) {
        log.info("Fetching Account by id: {}", id);
        Account account = findAccountById(id);
        validateOwnership(account);
        AccountResponse response = accountMapper.toAccountResponse(account);

        return response;
    }

    public AccountResponse createAccount(CreateAccountRequest request) {
        User currentUser = securityService.getCurrentUser();
        log.info("Creating account by user: {}", currentUser.getEmail());

        Account account = accountMapper.toEntity(request);
        account.setUser(currentUser);

        Account saved = accountRepository.save(account);
        return accountMapper.toAccountResponse(saved);
    }

    public AccountResponse updateAccount(Long id ,UpdateAccountRequest request) {
        User currentUser = securityService.getCurrentUser();
        log.info("Updating account {} by user: {}", id, currentUser.getEmail());

        Account account = findAccountById(id);
        validateOwnership(account);

        accountMapper.updateEntity(request, account);

        Account saved = accountRepository.save(account);
        return accountMapper.toAccountResponse(saved);
    }

    public void deleteAccount(Long id) {
        log.info("Deleting account: {}", id);
        Account account = findAccountById(id);
        validateOwnership(account);

        accountRepository.delete(account);
        log.info("Account delete successfully: {}", id);
    }

    public Account findAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id.toString()));
    }

    private void validateOwnership(Account account) {
        Long currentUserId = securityService.getCurrentUserId();
        if (!account.getUser().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You do not have permission to access this account");
        }
    }
}
