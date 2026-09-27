package com.ers.service;

import com.ers.model.ExpenseClaim;

import java.util.List;

public interface IExpenseClaimService {
    ExpenseClaim addExpenseClaim(ExpenseClaim expenseClaim);
    boolean updateExpenseClaim(ExpenseClaim expenseClaim);
    ExpenseClaim getExpenseClaimById(int claimId);
    List<ExpenseClaim> getAllExpenseClaims();
    boolean deleteExpenseClaimById(int claimId);
    List<ExpenseClaim> getClaimsByEmployeeId(int employeeId);
    boolean submitClaim(int claimId, int employeeId);
    boolean approveClaim(int claimId);
    boolean rejectClaim(int claimId, String reason);
    List<ExpenseClaim> getClaimsByStatus(String status);
}
