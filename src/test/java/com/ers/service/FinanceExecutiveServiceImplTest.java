package com.ers.service;

import com.ers.dao.IFinanceExecutiveDao;
import com.ers.model.ExpenseClaim;
import com.ers.model.FinanceExecutive;
import com.ers.model.Reimbursement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceExecutiveServiceImplTest {
    private FakeFinanceExecutiveDao financeDao;
    private FinanceExecutiveServiceImpl service;

    @BeforeEach
    void setUp() {
        financeDao = new FakeFinanceExecutiveDao();
        service = new FinanceExecutiveServiceImpl(financeDao);
    }

    @Test
    void processPaymentReimbursesAnApprovedClaim() {
        assertTrue(service.processPayment(1, 3, "UPI"));
        assertEquals("REIMBURSED", financeDao.getClaimById(1).getStatus());
    }

    @Test
    void processPaymentRejectsAClaimThatIsNotApproved() {
        assertFalse(service.processPayment(2, 3, "CASH"));
        assertEquals("SUBMITTED", financeDao.getClaimById(2).getStatus());
    }

    @Test
    void processPaymentRejectsAnUnknownFinanceExecutive() {
        assertFalse(service.processPayment(1, 9, "CASH"));
    }

    private static class FakeFinanceExecutiveDao implements IFinanceExecutiveDao {
        private final List<ExpenseClaim> claims = new ArrayList<>();

        FakeFinanceExecutiveDao() {
            claims.add(claim(1, "APPROVED"));
            claims.add(claim(2, "SUBMITTED"));
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
            return employeeId == 3 ? new FinanceExecutive(3, "Finance Executive", "finance@example.com", "Sales") : null;
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
            return claims.stream().filter(claim -> "APPROVED".equals(claim.getStatus())).toList();
        }

        @Override
        public ExpenseClaim getClaimById(int claimId) {
            return claims.stream().filter(claim -> claim.getClaimId() == claimId).findFirst().orElse(null);
        }

        @Override
        public boolean processPayment(int claimId, int financeExecutiveId, String paymentMode) {
            ExpenseClaim claim = getClaimById(claimId);
            if (claim == null) {
                return false;
            }
            claim.setStatus("REIMBURSED");
            return true;
        }

        @Override
        public List<Reimbursement> getReimbursementHistory(int financeExecutiveId) {
            return List.of();
        }

        private ExpenseClaim claim(int claimId, String status) {
            ExpenseClaim claim = new ExpenseClaim(10, "Travel", 100, LocalDate.of(2026, 9, 26), status, null);
            claim.setClaimId(claimId);
            return claim;
        }
    }
}
