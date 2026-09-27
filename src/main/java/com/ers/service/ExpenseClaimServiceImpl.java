package com.ers.service;

import com.ers.dao.IDepartmentDao;
import com.ers.dao.IEmployeeDao;
import com.ers.dao.IExpenseClaimDao;
import com.ers.model.Department;
import com.ers.model.Employee;
import com.ers.model.ExpenseClaim;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ExpenseClaimServiceImpl implements IExpenseClaimService {
    private static final Logger log = LoggerFactory.getLogger(ExpenseClaimServiceImpl.class);

    private final IExpenseClaimDao expenseClaimDao;
    private final IEmployeeDao employeeDao;
    private final IDepartmentDao departmentDao;

    public ExpenseClaimServiceImpl(IExpenseClaimDao expenseClaimDao, IEmployeeDao employeeDao, IDepartmentDao departmentDao) {
        this.expenseClaimDao = expenseClaimDao;
        this.employeeDao = employeeDao;
        this.departmentDao = departmentDao;
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
        Department department = departmentForEmployee(employeeId);
        if (department == null || department.getManagerId() <= 0) {
            log.warn("Submit rejected: employee {} has no manager", employeeId);
            return false;
        }
        if (!expenseClaimDao.submitClaim(claimId)) {
            log.error("Submit failed for claim {}", claimId);
            return false;
        }
        log.info("Claim {} submitted by employee {} for manager {}", claimId, employeeId, department.getManagerId());
        return true;
    }

    @Override
    public List<ExpenseClaim> getSubmittedClaimsForManager(int managerId) {
        Department department = departmentForManager(managerId);
        if (department == null) {
            return null;
        }
        List<ExpenseClaim> submitted = new ArrayList<>();
        for (Employee employee : departmentDao.getEmployeesByDepartmentId(department.getDepartmentId())) {
            for (ExpenseClaim claim : expenseClaimDao.getClaimsByEmployeeId(employee.getEmployeeId())) {
                if ("SUBMITTED".equals(claim.getStatus())) {
                    submitted.add(claim);
                }
            }
        }
        submitted.sort(Comparator.comparingInt(ExpenseClaim::getClaimId));
        return submitted;
    }

    @Override
    public boolean approveClaim(int claimId, int managerId) {
        if (!canManagerReview(claimId, managerId)) {
            return false;
        }
        if (!expenseClaimDao.approveClaim(claimId)) {
            log.error("Approve failed for claim {}", claimId);
            return false;
        }
        log.info("Claim {} approved by manager {}", claimId, managerId);
        return true;
    }

    @Override
    public boolean rejectClaim(int claimId, int managerId, String reason) {
        if (reason == null || reason.isBlank()) {
            log.warn("Reject rejected: claim {} needs a reason", claimId);
            return false;
        }
        if (!canManagerReview(claimId, managerId)) {
            return false;
        }
        if (!expenseClaimDao.rejectClaim(claimId, reason.trim())) {
            log.error("Reject failed for claim {}", claimId);
            return false;
        }
        log.info("Claim {} rejected by manager {}", claimId, managerId);
        return true;
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

    private boolean canManagerReview(int claimId, int managerId) {
        Department department = departmentForManager(managerId);
        if (department == null) {
            return false;
        }
        ExpenseClaim claim = expenseClaimDao.getExpenseClaimById(claimId);
        if (claim == null) {
            log.warn("Review rejected: claim {} was not found", claimId);
            return false;
        }
        if (!"SUBMITTED".equals(claim.getStatus())) {
            log.warn("Review rejected: claim {} is {}", claimId, claim.getStatus());
            return false;
        }
        Employee employee = employeeDao.getEmployeeById(claim.getEmployeeId());
        if (employee == null || employee.getDepartmentId() != department.getDepartmentId()) {
            log.warn("Review rejected: claim {} is outside manager {} department", claimId, managerId);
            return false;
        }
        return true;
    }

    private Department departmentForEmployee(int employeeId) {
        Employee employee = employeeDao.getEmployeeById(employeeId);
        if (employee == null || employee.getDepartmentId() <= 0) {
            return null;
        }
        return departmentDao.getDepartmentById(employee.getDepartmentId());
    }

    private Department departmentForManager(int managerId) {
        if (managerId <= 0 || employeeDao.getEmployeeById(managerId) == null) {
            log.warn("Manager action rejected: employee {} does not exist", managerId);
            return null;
        }
        Department department = departmentDao.getDepartmentByManagerId(managerId);
        if (department == null) {
            log.warn("Manager action rejected: employee {} does not manage a department", managerId);
        }
        return department;
    }
}
