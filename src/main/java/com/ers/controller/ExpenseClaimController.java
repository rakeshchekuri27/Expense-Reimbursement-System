package com.ers.controller;

import com.ers.model.ExpenseClaim;
import com.ers.service.IExpenseClaimService;

import java.util.List;

public class ExpenseClaimController {
    private IExpenseClaimService expenseClaimService;

    public ExpenseClaimController(IExpenseClaimService expenseClaimService) {
        this.expenseClaimService = expenseClaimService;
    }

    public ExpenseClaim addExpenseClaim(ExpenseClaim expenseClaim) {
        return expenseClaimService.addExpenseClaim(expenseClaim);
    }

    public boolean updateExpenseClaim(ExpenseClaim expenseClaim) {
        return false;
    }

    public ExpenseClaim getExpenseClaimById(int claimId) {
        return expenseClaimService.getExpenseClaimById(claimId);
    }

    public List<ExpenseClaim> getAllExpenseClaims() {
        return null;
    }

    public boolean deleteExpenseClaimById(int claimId) {
        return false;
    }

    public List<ExpenseClaim> getClaimsByEmployeeId(int employeeId) {
        return expenseClaimService.getClaimsByEmployeeId(employeeId);
    }

    public boolean submitClaim(int claimId, int employeeId) {
        return expenseClaimService.submitClaim(claimId, employeeId);
    }

    public boolean approveClaim(int claimId) {
        return false;
    }

    public boolean rejectClaim(int claimId, String reason) {
        return false;
    }

    public List<ExpenseClaim> getClaimsByStatus(String status) {
        return null;
    }
}
