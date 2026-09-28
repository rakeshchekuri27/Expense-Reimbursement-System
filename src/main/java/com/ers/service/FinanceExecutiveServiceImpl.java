package com.ers.service;

import com.ers.dao.IFinanceExecutiveDao;
import com.ers.model.ExpenseClaim;
import com.ers.model.FinanceExecutive;
import com.ers.model.Reimbursement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

public class FinanceExecutiveServiceImpl implements IFinanceExecutiveService {
    private static final Logger log = LoggerFactory.getLogger(FinanceExecutiveServiceImpl.class);
    private static final Set<String> PAYMENT_MODES = Set.of("BANK_TRANSFER", "CASH", "UPI", "CHEQUE");

    private final IFinanceExecutiveDao financeExecutiveDao;

    public FinanceExecutiveServiceImpl(IFinanceExecutiveDao financeExecutiveDao) {
        this.financeExecutiveDao = financeExecutiveDao;
    }

    @Override
    public FinanceExecutive addFinanceExecutive(FinanceExecutive financeExecutive) {
        return null;
    }

    @Override
    public boolean updateFinanceExecutive(FinanceExecutive financeExecutive) {
        return false;
    }

    @Override
    public FinanceExecutive getFinanceExecutiveById(int employeeId) {
        if (employeeId <= 0) {
            return null;
        }
        return financeExecutiveDao.getFinanceExecutiveById(employeeId);
    }

    @Override
    public List<FinanceExecutive> getAllFinanceExecutives() {
        return List.of();
    }

    @Override
    public boolean deleteFinanceExecutiveById(int employeeId) {
        return false;
    }

    @Override
    public List<ExpenseClaim> getPendingClaims() {
        return financeExecutiveDao.getPendingClaims();
    }

    @Override
    public ExpenseClaim getClaimById(int claimId) {
        return null;
    }

    @Override
    public boolean processPayment(int claimId, int financeExecutiveId, String paymentMode) {
        if (getFinanceExecutiveById(financeExecutiveId) == null) {
            log.warn("Payment rejected: employee {} is not a finance executive", financeExecutiveId);
            return false;
        }
        if (paymentMode == null || !PAYMENT_MODES.contains(paymentMode)) {
            log.warn("Payment rejected: invalid payment mode for claim {}", claimId);
            return false;
        }
        ExpenseClaim claim = financeExecutiveDao.getClaimById(claimId);
        if (claim == null) {
            log.warn("Payment rejected: claim {} was not found", claimId);
            return false;
        }
        if (!"APPROVED".equals(claim.getStatus())) {
            log.warn("Payment rejected: claim {} is {}", claimId, claim.getStatus());
            return false;
        }
        if (!financeExecutiveDao.processPayment(claimId, financeExecutiveId, paymentMode)) {
            log.error("Payment failed for claim {}", claimId);
            return false;
        }
        log.info("Claim {} reimbursed by finance executive {} via {}", claimId, financeExecutiveId, paymentMode);
        return true;
    }

    @Override
    public List<Reimbursement> getReimbursementHistory(int financeExecutiveId) {
        return List.of();
    }
}
