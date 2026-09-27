package com.ers.service;

import com.ers.dao.IDepartmentDao;
import com.ers.dao.IEmployeeDao;
import com.ers.dao.IExpenseClaimDao;
import com.ers.model.Department;
import com.ers.model.Employee;
import com.ers.model.ExpenseClaim;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpenseClaimServiceImplTest {
    private FakeExpenseClaimDao claimDao;
    private ExpenseClaimServiceImpl service;

    @BeforeEach
    void setUp() {
        claimDao = new FakeExpenseClaimDao();
        service = new ExpenseClaimServiceImpl(claimDao, new FakeEmployeeDao(), new FakeDepartmentDao());
    }

    @Test
    void addExpenseClaimStoresADraftForAKnownEmployee() {
        ExpenseClaim created = service.addExpenseClaim(sampleClaim(10, 250.0));

        assertEquals(1, created.getClaimId());
        assertEquals("DRAFT", created.getStatus());
        assertEquals(10, created.getEmployeeId());
    }

    @Test
    void addExpenseClaimRejectsAnUnknownEmployee() {
        assertNull(service.addExpenseClaim(sampleClaim(99, 250.0)));
    }

    @Test
    void addExpenseClaimRejectsANonPositiveAmount() {
        assertNull(service.addExpenseClaim(sampleClaim(10, 0)));
    }

    @Test
    void submitClaimMovesTheOwnersDraftToSubmitted() {
        ExpenseClaim created = service.addExpenseClaim(sampleClaim(10, 250.0));

        assertTrue(service.submitClaim(created.getClaimId(), 10));
        assertEquals("SUBMITTED", claimDao.getExpenseClaimById(created.getClaimId()).getStatus());
    }

    @Test
    void submitClaimRejectsAnotherEmployeesDraft() {
        ExpenseClaim created = service.addExpenseClaim(sampleClaim(10, 250.0));

        assertFalse(service.submitClaim(created.getClaimId(), 11));
        assertEquals("DRAFT", claimDao.getExpenseClaimById(created.getClaimId()).getStatus());
    }

    @Test
    void submitClaimRejectsAClaimThatIsAlreadySubmitted() {
        ExpenseClaim created = service.addExpenseClaim(sampleClaim(10, 250.0));
        service.submitClaim(created.getClaimId(), 10);

        assertFalse(service.submitClaim(created.getClaimId(), 10));
    }

    @Test
    void managerSeesSubmittedClaimsFromTheirDepartment() {
        ExpenseClaim created = service.addExpenseClaim(sampleClaim(10, 250.0));
        service.submitClaim(created.getClaimId(), 10);

        List<ExpenseClaim> waiting = service.getSubmittedClaimsForManager(11);

        assertEquals(1, waiting.size());
        assertEquals(created.getClaimId(), waiting.get(0).getClaimId());
    }

    @Test
    void approveClaimMovesASubmittedClaimToApproved() {
        ExpenseClaim created = service.addExpenseClaim(sampleClaim(10, 250.0));
        service.submitClaim(created.getClaimId(), 10);

        assertTrue(service.approveClaim(created.getClaimId(), 11));
        assertEquals("APPROVED", claimDao.getExpenseClaimById(created.getClaimId()).getStatus());
        assertTrue(service.getSubmittedClaimsForManager(11).isEmpty());
    }

    @Test
    void approveClaimRejectsADraftAndANonManager() {
        ExpenseClaim created = service.addExpenseClaim(sampleClaim(10, 250.0));

        assertFalse(service.approveClaim(created.getClaimId(), 11));
        service.submitClaim(created.getClaimId(), 10);
        assertFalse(service.approveClaim(created.getClaimId(), 10));
        assertEquals("SUBMITTED", claimDao.getExpenseClaimById(created.getClaimId()).getStatus());
    }

    @Test
    void rejectClaimStoresTheReasonAndLeavesTheQueue() {
        ExpenseClaim created = service.addExpenseClaim(sampleClaim(10, 250.0));
        service.submitClaim(created.getClaimId(), 10);

        assertFalse(service.rejectClaim(created.getClaimId(), 11, "  "));
        assertTrue(service.rejectClaim(created.getClaimId(), 11, "Missing receipt"));
        assertEquals("REJECTED", claimDao.getExpenseClaimById(created.getClaimId()).getStatus());
        assertTrue(service.getSubmittedClaimsForManager(11).isEmpty());
    }

    private ExpenseClaim sampleClaim(int employeeId, double amount) {
        return new ExpenseClaim(employeeId, "Client visit", amount, LocalDate.of(2026, 9, 26), "APPROVED", null);
    }

    private static class FakeDepartmentDao implements IDepartmentDao {
        private final Department sales = department();

        @Override
        public Department addDepartment(Department department) {
            return null;
        }

        @Override
        public boolean updateDepartment(Department department) {
            return false;
        }

        @Override
        public Department getDepartmentById(int departmentId) {
            return departmentId == 1 ? sales : null;
        }

        @Override
        public List<Department> getAllDepartments() {
            return List.of(sales);
        }

        @Override
        public boolean deleteDepartmentById(int departmentId) {
            return false;
        }

        @Override
        public List<Employee> getEmployeesByDepartmentId(int departmentId) {
            if (departmentId != 1) {
                return List.of();
            }
            Employee employee = new Employee(1, "Test Employee", "test@example.com", 1);
            employee.setEmployeeId(10);
            return List.of(employee);
        }

        @Override
        public Department getDepartmentByManagerId(int managerId) {
            return managerId == 11 ? sales : null;
        }

        private Department department() {
            Department department = new Department("Sales", 11);
            department.setDepartmentId(1);
            return department;
        }
    }

    private static class FakeEmployeeDao implements IEmployeeDao {
        @Override
        public Employee addEmployee(Employee employee) {
            return null;
        }

        @Override
        public boolean updateEmployee(Employee employee) {
            return false;
        }

        @Override
        public Employee getEmployeeById(int employeeId) {
            if (employeeId == 10 || employeeId == 11) {
                return new Employee(1, "Test Employee", "test@example.com", 1);
            }
            return null;
        }

        @Override
        public List<Employee> getAllEmployees() {
            return List.of();
        }

        @Override
        public boolean deleteEmployeeById(int employeeId) {
            return false;
        }
    }

    private static class FakeExpenseClaimDao implements IExpenseClaimDao {
        private final List<ExpenseClaim> claims = new ArrayList<>();
        private int nextId = 1;

        @Override
        public ExpenseClaim addExpenseClaim(ExpenseClaim expenseClaim) {
            expenseClaim.setClaimId(nextId++);
            claims.add(expenseClaim);
            return expenseClaim;
        }

        @Override
        public boolean updateExpenseClaim(ExpenseClaim expenseClaim) {
            return false;
        }

        @Override
        public ExpenseClaim getExpenseClaimById(int claimId) {
            return claims.stream().filter(claim -> claim.getClaimId() == claimId).findFirst().orElse(null);
        }

        @Override
        public List<ExpenseClaim> getAllExpenseClaims() {
            return List.copyOf(claims);
        }

        @Override
        public boolean deleteExpenseClaimById(int claimId) {
            return false;
        }

        @Override
        public List<ExpenseClaim> getClaimsByEmployeeId(int employeeId) {
            return claims.stream().filter(claim -> claim.getEmployeeId() == employeeId).toList();
        }

        @Override
        public boolean submitClaim(int claimId) {
            ExpenseClaim claim = getExpenseClaimById(claimId);
            if (claim == null) {
                return false;
            }
            claim.setStatus("SUBMITTED");
            return true;
        }

        @Override
        public boolean approveClaim(int claimId) {
            ExpenseClaim claim = getExpenseClaimById(claimId);
            if (claim == null) {
                return false;
            }
            claim.setStatus("APPROVED");
            return true;
        }

        @Override
        public boolean rejectClaim(int claimId, String reason) {
            ExpenseClaim claim = getExpenseClaimById(claimId);
            if (claim == null || reason == null || reason.isBlank()) {
                return false;
            }
            claim.setStatus("REJECTED");
            return true;
        }

        @Override
        public List<ExpenseClaim> getClaimsByStatus(String status) {
            return List.of();
        }
    }
}
