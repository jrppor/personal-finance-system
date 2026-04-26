package com.jirapat.personalfinance.api.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.jirapat.personalfinance.api.service.RecurringTransactionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecurringTransactionScheduler {

    private final RecurringTransactionService recurringTransactionService;

    @Scheduled(cron = "0 0 0 * * *")
    public void processDueRecurringTransactions() {
        int processedCount = recurringTransactionService.processDueRecurringTransactions();
        if (processedCount > 0) {
            log.info("Processed {} recurring transaction occurrence(s)", processedCount);
        }
    }
}