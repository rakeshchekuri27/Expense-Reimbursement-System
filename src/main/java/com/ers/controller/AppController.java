package com.ers.controller;

import com.ers.dao.DepartmentDaoImpl;
import com.ers.dao.EmployeeDaoImpl;
import com.ers.dao.ExpenseClaimDaoImpl;
import com.ers.dao.FinanceExecutiveDaoImpl;
import com.ers.model.ExpenseClaim;
import com.ers.service.ExpenseClaimServiceImpl;
import com.ers.service.FinanceExecutiveServiceImpl;
import com.ers.util.JDBCUtil;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class AppController {
    public static void main(String[] args) {
        JDBCUtil jdbcUtil = new JDBCUtil();
        ExpenseClaimController claimController = new ExpenseClaimController(
                new ExpenseClaimServiceImpl(
                        new ExpenseClaimDaoImpl(jdbcUtil),
                        new EmployeeDaoImpl(jdbcUtil),
                        new DepartmentDaoImpl(jdbcUtil)
                )
        );

        FinanceExecutiveController financeController = new FinanceExecutiveController(
                new FinanceExecutiveServiceImpl(new FinanceExecutiveDaoImpl(jdbcUtil))
        );

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Expense Reimbursement");
            System.out.println("1. Employee");
            System.out.println("2. Manager");
            System.out.println("3. Finance");
            System.out.println("0. Exit");
            switch (readInt(scanner, "Choose a role: ")) {
                case 1 -> runEmployee(scanner, claimController);
                case 2 -> runManager(scanner, claimController);
                case 3 -> runFinance(scanner, financeController);
                case 0 -> { }
                default -> System.out.println("Choose 1, 2, 3, or 0.");
            }
        }
    }

    private static void runEmployee(Scanner scanner, ExpenseClaimController claimController) {
        int employeeId = readInt(scanner, "Enter your employee id: ");
        if (employeeId <= 0) {
            System.out.println("Enter a valid employee id.");
            return;
        }

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("1. Create a draft claim");
            System.out.println("2. View my claims");
            System.out.println("3. Submit a draft claim for review");
            System.out.println("0. Exit");
            switch (readInt(scanner, "Choose an option: ")) {
                case 1 -> createClaim(scanner, claimController, employeeId);
                case 2 -> viewClaims(claimController, employeeId);
                case 3 -> submitClaim(scanner, claimController, employeeId);
                case 0 -> running = false;
                default -> System.out.println("Choose 1, 2, 3, or 0.");
            }
        }
    }

    private static void runManager(Scanner scanner, ExpenseClaimController claimController) {
        int managerId = readInt(scanner, "Enter your manager employee id: ");
        if (managerId <= 0) {
            System.out.println("Enter a valid employee id.");
            return;
        }
        if (claimController.getSubmittedClaimsForManager(managerId) == null) {
            System.out.println("This employee does not manage a department.");
            return;
        }

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("1. View claims waiting for approval");
            System.out.println("2. Approve a claim");
            System.out.println("3. Reject a claim");
            System.out.println("0. Exit");
            switch (readInt(scanner, "Choose an option: ")) {
                case 1 -> viewSubmittedClaims(claimController, managerId);
                case 2 -> approveClaim(scanner, claimController, managerId);
                case 3 -> rejectClaim(scanner, claimController, managerId);
                case 0 -> running = false;
                default -> System.out.println("Choose 1, 2, 3, or 0.");
            }
        }
    }

    private static void runFinance(Scanner scanner, FinanceExecutiveController financeController) {
        int financeExecutiveId = readInt(scanner, "Enter your finance executive id: ");
        if (financeExecutiveId <= 0) {
            System.out.println("Enter a valid employee id.");
            return;
        }
        if (financeController.getFinanceExecutiveById(financeExecutiveId) == null) {
            System.out.println("This employee is not a finance executive.");
            return;
        }

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("1. View approved claims");
            System.out.println("2. Pay a claim");
            System.out.println("0. Exit");
            switch (readInt(scanner, "Choose an option: ")) {
                case 1 -> viewApprovedClaims(financeController);
                case 2 -> payClaim(scanner, financeController, financeExecutiveId);
                case 0 -> running = false;
                default -> System.out.println("Choose 1, 2, or 0.");
            }
        }
    }

    private static void createClaim(Scanner scanner, ExpenseClaimController claimController, int employeeId) {
        System.out.print("Description: ");
        String description = scanner.nextLine().trim();
        double amount = readDouble(scanner, "Amount: ");
        LocalDate claimDate = readDate(scanner, "Claim date (yyyy-MM-dd): ");
        System.out.print("Receipt path (optional): ");
        String documentPath = scanner.nextLine().trim();
        if (documentPath.isEmpty()) {
            documentPath = null;
        }

        if (description.isBlank() || amount <= 0 || claimDate == null) {
            System.out.println("A claim needs a description, a date, and an amount greater than 0.");
            return;
        }

        ExpenseClaim created = claimController.addExpenseClaim(
                new ExpenseClaim(employeeId, description, amount, claimDate, "DRAFT", documentPath)
        );
        if (created == null) {
            System.out.println("Claim was not created. Check that this employee id exists.");
            return;
        }

        System.out.println("Draft claim created. Claim id: " + created.getClaimId());
        System.out.print("Submit this claim for review now? (y/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            printSubmitResult(claimController.submitClaim(created.getClaimId(), employeeId));
        }
    }

    private static void viewClaims(ExpenseClaimController claimController, int employeeId) {
        List<ExpenseClaim> claims = claimController.getClaimsByEmployeeId(employeeId);
        if (claims == null || claims.isEmpty()) {
            System.out.println("You have no claims.");
            return;
        }
        for (ExpenseClaim claim : claims) {
            System.out.printf(
                    "Claim %d | %s | %.2f | %s | %s%n",
                    claim.getClaimId(),
                    claim.getClaimDate(),
                    claim.getClaimAmount(),
                    claim.getStatus(),
                    claim.getClaimDesc()
            );
        }
    }

    private static void submitClaim(Scanner scanner, ExpenseClaimController claimController, int employeeId) {
        List<ExpenseClaim> claims = claimController.getClaimsByEmployeeId(employeeId);
        boolean hasDraft = false;
        if (claims != null) {
            for (ExpenseClaim claim : claims) {
                if ("DRAFT".equals(claim.getStatus())) {
                    hasDraft = true;
                    System.out.printf(
                            "Claim %d | %s | %.2f | %s%n",
                            claim.getClaimId(),
                            claim.getClaimDate(),
                            claim.getClaimAmount(),
                            claim.getClaimDesc()
                    );
                }
            }
        }
        if (!hasDraft) {
            System.out.println("You have no draft claims to submit.");
            return;
        }

        int claimId = readInt(scanner, "Claim id to submit: ");
        if (claimId <= 0) {
            System.out.println("Enter a valid claim id.");
            return;
        }
        printSubmitResult(claimController.submitClaim(claimId, employeeId));
    }

    private static void printSubmitResult(boolean submitted) {
        if (submitted) {
            System.out.println("Claim submitted to your manager for review.");
        } else {
            System.out.println("Claim was not submitted. Only your own draft can be sent, and your department must have a manager.");
        }
    }

    private static void viewSubmittedClaims(ExpenseClaimController claimController, int managerId) {
        List<ExpenseClaim> claims = claimController.getSubmittedClaimsForManager(managerId);
        if (claims == null) {
            System.out.println("This employee does not manage a department.");
            return;
        }
        if (claims.isEmpty()) {
            System.out.println("No claims are waiting for your approval.");
            return;
        }
        for (ExpenseClaim claim : claims) {
            System.out.printf(
                    "Claim %d | employee %d | %s | %.2f | %s%n",
                    claim.getClaimId(),
                    claim.getEmployeeId(),
                    claim.getClaimDate(),
                    claim.getClaimAmount(),
                    claim.getClaimDesc()
            );
        }
    }

    private static void approveClaim(Scanner scanner, ExpenseClaimController claimController, int managerId) {
        viewSubmittedClaims(claimController, managerId);
        List<ExpenseClaim> claims = claimController.getSubmittedClaimsForManager(managerId);
        if (claims == null || claims.isEmpty()) {
            return;
        }
        int claimId = readInt(scanner, "Claim id to approve: ");
        if (claimId <= 0) {
            System.out.println("Enter a valid claim id.");
            return;
        }
        if (claimController.approveClaim(claimId, managerId)) {
            System.out.println("Claim approved.");
        } else {
            System.out.println("Claim was not approved. Only a submitted claim from your department can be approved.");
        }
    }

    private static void rejectClaim(Scanner scanner, ExpenseClaimController claimController, int managerId) {
        viewSubmittedClaims(claimController, managerId);
        List<ExpenseClaim> claims = claimController.getSubmittedClaimsForManager(managerId);
        if (claims == null || claims.isEmpty()) {
            return;
        }
        int claimId = readInt(scanner, "Claim id to reject: ");
        if (claimId <= 0) {
            System.out.println("Enter a valid claim id.");
            return;
        }
        System.out.print("Reason: ");
        String reason = scanner.nextLine().trim();
        if (reason.isBlank()) {
            System.out.println("Enter a reason.");
            return;
        }
        if (claimController.rejectClaim(claimId, managerId, reason)) {
            System.out.println("Claim rejected.");
        } else {
            System.out.println("Claim was not rejected. Only a submitted claim from your department can be rejected.");
        }
    }

    private static void viewApprovedClaims(FinanceExecutiveController financeController) {
        List<ExpenseClaim> claims = financeController.getPendingClaims();
        if (claims == null || claims.isEmpty()) {
            System.out.println("No approved claims are waiting for payment.");
            return;
        }
        for (ExpenseClaim claim : claims) {
            System.out.printf(
                    "Claim %d | employee %d | %s | %.2f | %s%n",
                    claim.getClaimId(),
                    claim.getEmployeeId(),
                    claim.getClaimDate(),
                    claim.getClaimAmount(),
                    claim.getClaimDesc()
            );
        }
    }

    private static void payClaim(Scanner scanner, FinanceExecutiveController financeController, int financeExecutiveId) {
        viewApprovedClaims(financeController);
        List<ExpenseClaim> claims = financeController.getPendingClaims();
        if (claims == null || claims.isEmpty()) {
            return;
        }
        int claimId = readInt(scanner, "Claim id to pay: ");
        if (claimId <= 0) {
            System.out.println("Enter a valid claim id.");
            return;
        }
        System.out.println("1. BANK_TRANSFER");
        System.out.println("2. CASH");
        System.out.println("3. UPI");
        System.out.println("4. CHEQUE");
        String paymentMode = switch (readInt(scanner, "Payment mode: ")) {
            case 1 -> "BANK_TRANSFER";
            case 2 -> "CASH";
            case 3 -> "UPI";
            case 4 -> "CHEQUE";
            default -> null;
        };
        if (paymentMode == null) {
            System.out.println("Choose a payment mode from 1 to 4.");
            return;
        }
        if (financeController.processPayment(claimId, financeExecutiveId, paymentMode)) {
            System.out.println("Claim reimbursed.");
        } else {
            System.out.println("Payment was not recorded. Only an approved claim can be paid.");
        }
    }

    private static int readInt(Scanner scanner, String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return Integer.MIN_VALUE;
        }
    }

    private static double readDouble(Scanner scanner, String prompt) {
        System.out.print(prompt);
        try {
            return Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static LocalDate readDate(Scanner scanner, String prompt) {
        System.out.print(prompt);
        try {
            return LocalDate.parse(scanner.nextLine().trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
