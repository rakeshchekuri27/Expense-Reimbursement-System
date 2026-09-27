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
    List<ExpenseClaim> getSubmittedClaimsForManager(int managerId);
    boolean approveClaim(int claimId, int managerId);
    boolean rejectClaim(int claimId, int managerId, String reason);
    List<ExpenseClaim> getClaimsByStatus(String status);
}
