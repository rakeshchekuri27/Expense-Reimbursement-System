package com.ers.controller;

import com.ers.model.ExpenseClaim;
import com.ers.model.FinanceExecutive;
import com.ers.model.Reimbursement;
import com.ers.service.IFinanceExecutiveService;

import java.util.List;

public class FinanceExecutiveController {
    private IFinanceExecutiveService financeExecutiveService;

    public FinanceExecutiveController(IFinanceExecutiveService financeExecutiveService){
        this.financeExecutiveService = financeExecutiveService;
    }

    public FinanceExecutive addNewFinanceExecutive(){
        return null;
    }
    public boolean updateFinanceExecutive(FinanceExecutive financeExecutive){
        return false;
    }
    public FinanceExecutive getFinanceExecutiveById(int employeeId) {
        return financeExecutiveService.getFinanceExecutiveById(employeeId);
    }
    public List<FinanceExecutive> getAllFinanceExecutives() {
        return List.of();
    }
    public boolean deleteFinanceExecutiveById(int FinanceExecutiveId) {
        return false;
    }
    public List<ExpenseClaim> getPendingClaims() {
        return financeExecutiveService.getPendingClaims();
    }

    public ExpenseClaim getClaimById(int claimId) {
        return null;
    }

    public boolean processPayment(int claimId, int financeExecutiveId, String paymentMode) {
        return financeExecutiveService.processPayment(claimId, financeExecutiveId, paymentMode);
    }

    public List<Reimbursement> getReimbursementHistory(int financeExecutiveId) {
        return List.of();
    }
}
