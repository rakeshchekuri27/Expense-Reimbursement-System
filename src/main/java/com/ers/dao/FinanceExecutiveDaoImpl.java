package com.ers.dao;

import com.ers.model.ExpenseClaim;
import com.ers.model.FinanceExecutive;
import com.ers.model.Reimbursement;
import com.ers.util.JDBCUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FinanceExecutiveDaoImpl implements IFinanceExecutiveDao {
    private final JDBCUtil jdbcUtil;

    public FinanceExecutiveDaoImpl(JDBCUtil jdbcUtil) {
        this.jdbcUtil = jdbcUtil;
    }

    @Override
    public FinanceExecutive addFinanceExecutive(FinanceExecutive financeExecutive) {
        String sql = "INSERT INTO finance_executives (employee_id, full_name, email, department) VALUES (?, ?, ?, ?)";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, financeExecutive.getEmployeeId());
            ps.setString(2, financeExecutive.getFullName());
            ps.setString(3, financeExecutive.getEmail());
            ps.setString(4, financeExecutive.getDepartment());
            ps.executeUpdate();
            return financeExecutive;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean updateFinanceExecutive(FinanceExecutive financeExecutive) {
        String sql = "UPDATE finance_executives SET full_name = ?, email = ?, department = ? WHERE employee_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, financeExecutive.getFullName());
            ps.setString(2, financeExecutive.getEmail());
            ps.setString(3, financeExecutive.getDepartment());
            ps.setInt(4, financeExecutive.getEmployeeId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public FinanceExecutive getFinanceExecutiveById(int employeeId) {
        String sql = "SELECT * FROM finance_executives WHERE employee_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapFinance(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<FinanceExecutive> getAllFinanceExecutives() {
        List<FinanceExecutive> executives = new ArrayList<>();
        String sql = "SELECT * FROM finance_executives";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                executives.add(mapFinance(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return executives;
    }

    @Override
    public boolean deleteFinanceExecutiveById(int employeeId) {
        String sql = "DELETE FROM finance_executives WHERE employee_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<ExpenseClaim> getPendingClaims() {
        List<ExpenseClaim> claims = new ArrayList<>();
        String sql = "SELECT * FROM expense_claims WHERE status = 'APPROVED'";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                claims.add(mapClaim(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return claims;
    }

    @Override
    public ExpenseClaim getClaimById(int claimId) {
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
    public boolean processPayment(int claimId, int financeExecutiveId, String paymentMode) {
        String findClaim = "SELECT claim_amount FROM expense_claims WHERE claim_id = ?";
        String insertPayment = "INSERT INTO reimbursements (claim_id, reimbursed_amount, payment_mode, reimbursement_date, processed_by, status) VALUES (?, ?, ?, ?, ?, 'COMPLETED')";
        String updateClaim = "UPDATE expense_claims SET status = 'REIMBURSED' WHERE claim_id = ?";
        Connection con = null;
        try {
            con = jdbcUtil.getConnection();
            con.setAutoCommit(false);

            double amount;
            try (PreparedStatement ps = con.prepareStatement(findClaim)) {
                ps.setInt(1, claimId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return false;
                    }
                    amount = rs.getDouble("claim_amount");
                }
            }

            try (PreparedStatement ps = con.prepareStatement(insertPayment)) {
                ps.setInt(1, claimId);
                ps.setDouble(2, amount);
                ps.setString(3, paymentMode);
                ps.setDate(4, Date.valueOf(LocalDate.now()));
                ps.setInt(5, financeExecutiveId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(updateClaim)) {
                ps.setInt(1, claimId);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public List<Reimbursement> getReimbursementHistory(int financeExecutiveId) {
        List<Reimbursement> history = new ArrayList<>();
        String sql = "SELECT * FROM reimbursements WHERE processed_by = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, financeExecutiveId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    history.add(mapReimbursement(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    private FinanceExecutive mapFinance(ResultSet rs) throws SQLException {
        return new FinanceExecutive(
                rs.getInt("employee_id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("department")
        );
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

    private Reimbursement mapReimbursement(ResultSet rs) throws SQLException {
        Date paidOn = rs.getDate("reimbursement_date");
        Reimbursement reimbursement = new Reimbursement(
                rs.getInt("claim_id"),
                rs.getDouble("reimbursed_amount"),
                rs.getString("payment_mode"),
                rs.getString("transaction_ref"),
                paidOn == null ? null : paidOn.toLocalDate(),
                rs.getInt("processed_by"),
                rs.getString("status")
        );
        reimbursement.setReimbursementId(rs.getInt("reimbursement_id"));
        return reimbursement;
    }
}
