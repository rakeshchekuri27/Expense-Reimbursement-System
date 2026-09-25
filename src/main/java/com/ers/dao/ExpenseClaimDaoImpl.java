package com.ers.dao;

import com.ers.model.ExpenseClaim;
import com.ers.util.JDBCUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ExpenseClaimDaoImpl implements IExpenseClaimDao {
    private final JDBCUtil jdbcUtil;

    public ExpenseClaimDaoImpl(JDBCUtil jdbcUtil) {
        this.jdbcUtil = jdbcUtil;
    }

    @Override
    public ExpenseClaim addExpenseClaim(ExpenseClaim expenseClaim) {
        String sql = "INSERT INTO expense_claims (employee_id, claim_description, claim_date, claim_amount, status, document_path) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, expenseClaim.getEmployeeId());
            ps.setString(2, expenseClaim.getClaimDesc());
            ps.setDate(3, Date.valueOf(expenseClaim.getClaimDate()));
            ps.setDouble(4, expenseClaim.getClaimAmount());
            ps.setString(5, expenseClaim.getStatus());
            ps.setString(6, expenseClaim.getDocumentPath());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    expenseClaim.setClaimId(keys.getInt(1));
                }
            }
            return expenseClaim;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean updateExpenseClaim(ExpenseClaim expenseClaim) {
        String sql = "UPDATE expense_claims SET employee_id = ?, claim_description = ?, claim_date = ?, claim_amount = ?, status = ?, document_path = ? WHERE claim_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, expenseClaim.getEmployeeId());
            ps.setString(2, expenseClaim.getClaimDesc());
            ps.setDate(3, Date.valueOf(expenseClaim.getClaimDate()));
            ps.setDouble(4, expenseClaim.getClaimAmount());
            ps.setString(5, expenseClaim.getStatus());
            ps.setString(6, expenseClaim.getDocumentPath());
            ps.setInt(7, expenseClaim.getClaimId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public ExpenseClaim getExpenseClaimById(int claimId) {
        String sql = "SELECT * FROM expense_claims WHERE claim_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, claimId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapClaim(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ExpenseClaim> getAllExpenseClaims() {
        return queryClaims("SELECT * FROM expense_claims", null);
    }

    @Override
    public boolean deleteExpenseClaimById(int claimId) {
        String sql = "DELETE FROM expense_claims WHERE claim_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, claimId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<ExpenseClaim> getClaimsByEmployeeId(int employeeId) {
        return queryClaims("SELECT * FROM expense_claims WHERE employee_id = ?", employeeId);
    }

    @Override
    public boolean submitClaim(int claimId) {
        return updateStatus(claimId, "SUBMITTED", null);
    }

    @Override
    public boolean approveClaim(int claimId) {
        return updateStatus(claimId, "APPROVED", null);
    }

    @Override
    public boolean rejectClaim(int claimId, String reason) {
        return updateStatus(claimId, "REJECTED", reason);
    }

    @Override
    public List<ExpenseClaim> getClaimsByStatus(String status) {
        List<ExpenseClaim> claims = new ArrayList<>();
        String sql = "SELECT * FROM expense_claims WHERE status = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    claims.add(mapClaim(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return claims;
    }

    private List<ExpenseClaim> queryClaims(String sql, Integer employeeId) {
        List<ExpenseClaim> claims = new ArrayList<>();
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (employeeId != null) {
                ps.setInt(1, employeeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    claims.add(mapClaim(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return claims;
    }

    private boolean updateStatus(int claimId, String status, String reason) {
        String sql = reason == null
                ? "UPDATE expense_claims SET status = ? WHERE claim_id = ?"
                : "UPDATE expense_claims SET status = ?, reason = ? WHERE claim_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            if (reason == null) {
                ps.setInt(2, claimId);
            } else {
                ps.setString(2, reason);
                ps.setInt(3, claimId);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ExpenseClaim mapClaim(ResultSet rs) throws SQLException {
        Date claimDate = rs.getDate("claim_date");
        ExpenseClaim claim = new ExpenseClaim(
                rs.getInt("employee_id"),
                rs.getString("claim_description"),
                rs.getDouble("claim_amount"),
                claimDate == null ? null : claimDate.toLocalDate(),
                rs.getString("status"),
                rs.getString("document_path")
        );
        claim.setClaimId(rs.getInt("claim_id"));
        return claim;
    }
}
