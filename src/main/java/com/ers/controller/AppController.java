package com.ers.controller;

import com.ers.dao.EmployeeDaoImpl;
import com.ers.dao.ExpenseClaimDaoImpl;
import com.ers.model.ExpenseClaim;
import com.ers.service.ExpenseClaimServiceImpl;
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
                        new EmployeeDaoImpl(jdbcUtil)
                )
        );

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Expense Reimbursement — Employee");
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
            System.out.println("Claim submitted for review.");
        } else {
            System.out.println("Claim was not submitted. Only your own draft claims can be sent for review.");
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
