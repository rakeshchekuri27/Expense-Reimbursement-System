package com.ers.service;

import com.ers.dao.IEmployeeDao;
import com.ers.dao.IExpenseClaimDao;
import com.ers.model.ExpenseClaim;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ExpenseClaimServiceImpl implements IExpenseClaimService {
    private static final Logger log = LoggerFactory.getLogger(ExpenseClaimServiceImpl.class);

    private final IExpenseClaimDao expenseClaimDao;
    private final IEmployeeDao employeeDao;

    public ExpenseClaimServiceImpl(IExpenseClaimDao expenseClaimDao, IEmployeeDao employeeDao) {
        this.expenseClaimDao = expenseClaimDao;
        this.employeeDao = employeeDao;
    }

    @Override
    public ExpenseClaim addExpenseClaim(ExpenseClaim expenseClaim) {
        if (!isValidNewClaim(expenseClaim)) {
            return null;
        }
        expenseClaim.setStatus("DRAFT");
        if (expenseClaim.getDocumentPath() != null && expenseClaim.getDocumentPath().isBlank()) {
            expenseClaim.setDocumentPath(null);
        }
        ExpenseClaim saved = expenseClaimDao.addExpenseClaim(expenseClaim);
        if (saved == null) {
            log.error("Create claim failed for employee {}", expenseClaim.getEmployeeId());
            return null;
        }
        log.info("Draft claim {} created for employee {}", saved.getClaimId(), saved.getEmployeeId());
        return saved;
    }

    @Override
    public boolean updateExpenseClaim(ExpenseClaim expenseClaim) {
        return false;
    }

    @Override
    public ExpenseClaim getExpenseClaimById(int claimId) {
        return expenseClaimDao.getExpenseClaimById(claimId);
    }

    @Override
    public List<ExpenseClaim> getAllExpenseClaims() {
        return List.of();
    }

    @Override
    public boolean deleteExpenseClaimById(int claimId) {
        return false;
    }

    @Override
    public List<ExpenseClaim> getClaimsByEmployeeId(int employeeId) {
        if (employeeId <= 0) {
            return List.of();
        }
        return expenseClaimDao.getClaimsByEmployeeId(employeeId);
    }

    @Override
    public boolean submitClaim(int claimId, int employeeId) {
        if (employeeId <= 0 || employeeDao.getEmployeeById(employeeId) == null) {
            log.warn("Submit rejected: employee {} does not exist", employeeId);
            return false;
        }
        ExpenseClaim claim = expenseClaimDao.getExpenseClaimById(claimId);
        if (claim == null) {
            log.warn("Submit rejected: claim {} was not found", claimId);
            return false;
        }
        if (claim.getEmployeeId() != employeeId) {
            log.warn("Submit rejected: employee {} does not own claim {}", employeeId, claimId);
            return false;
        }
        if (!"DRAFT".equals(claim.getStatus())) {
            log.warn("Submit rejected: claim {} is {}", claimId, claim.getStatus());
            return false;
        }
        if (!expenseClaimDao.submitClaim(claimId)) {
            log.error("Submit failed for claim {}", claimId);
            return false;
        }
        log.info("Claim {} submitted by employee {}", claimId, employeeId);
        return true;
    }

    @Override
    public boolean approveClaim(int claimId) {
        return false;
    }

    @Override
    public boolean rejectClaim(int claimId, String reason) {
        return false;
    }

    @Override
    public List<ExpenseClaim> getClaimsByStatus(String status) {
        return List.of();
    }

    private boolean isValidNewClaim(ExpenseClaim expenseClaim) {
        if (expenseClaim == null) {
            log.warn("Create claim rejected: claim is missing");
            return false;
        }
        if (expenseClaim.getEmployeeId() <= 0 || employeeDao.getEmployeeById(expenseClaim.getEmployeeId()) == null) {
            log.warn("Create claim rejected: employee {} does not exist", expenseClaim.getEmployeeId());
            return false;
        }
        if (expenseClaim.getClaimDesc() == null || expenseClaim.getClaimDesc().isBlank()) {
            log.warn("Create claim rejected: description is missing for employee {}", expenseClaim.getEmployeeId());
            return false;
        }
        if (expenseClaim.getClaimAmount() <= 0 || expenseClaim.getClaimDate() == null) {
            log.warn("Create claim rejected: amount or date is invalid for employee {}", expenseClaim.getEmployeeId());
            return false;
        }
        return true;
    }
}
