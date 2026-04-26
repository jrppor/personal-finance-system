package com.jirapat.personalfinance.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jirapat.personalfinance.api.entity.SavingsContribution;

@Repository
public interface SavingContributionRepository extends JpaRepository<SavingsContribution, Long> {

    List<SavingsContribution> findByGoalIdOrderByContributedAtAsc(Long goalId);

    List<SavingsContribution> findTop5ByGoalIdOrderByContributedAtDesc(Long goalId);
}
