package com.ers.dao;

import com.ers.model.Reimbursement;
import com.ers.util.JDBCUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ReimbursementDaoImpl implements IReimbursementDao {
    private final JDBCUtil jdbcUtil;

    public ReimbursementDaoImpl(JDBCUtil jdbcUtil) {
        this.jdbcUtil = jdbcUtil;
    }

    @Override
    public Reimbursement addReimbursement(Reimbursement reimbursement) {
        String sql = "INSERT INTO reimbursements (claim_id, reimbursed_amount, payment_mode, transaction_ref, reimbursement_date, processed_by, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillReimbursement(ps, reimbursement);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    reimbursement.setReimbursementId(keys.getInt(1));
                }
            }
            return reimbursement;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean updateReimbursement(Reimbursement reimbursement) {
        String sql = "UPDATE reimbursements SET claim_id = ?, reimbursed_amount = ?, payment_mode = ?, transaction_ref = ?, reimbursement_date = ?, processed_by = ?, status = ? WHERE reimbursement_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            fillReimbursement(ps, reimbursement);
            ps.setInt(8, reimbursement.getReimbursementId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Reimbursement getReimbursementById(int reimbursementId) {
        String sql = "SELECT * FROM reimbursements WHERE reimbursement_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reimbursementId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapReimbursement(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Reimbursement> getAllReimbursements() {
        List<Reimbursement> reimbursements = new ArrayList<>();
        String sql = "SELECT * FROM reimbursements";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                reimbursements.add(mapReimbursement(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reimbursements;
    }

    @Override
    public boolean deleteReimbursementById(int reimbursementId) {
        String sql = "DELETE FROM reimbursements WHERE reimbursement_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reimbursementId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Reimbursement getReimbursementByClaimId(int claimId) {
        String sql = "SELECT * FROM reimbursements WHERE claim_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, claimId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapReimbursement(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Reimbursement> getReimbursementsByEmployeeId(int employeeId) {
        List<Reimbursement> reimbursements = new ArrayList<>();
        String sql = "SELECT r.* FROM reimbursements r JOIN expense_claims c ON r.claim_id = c.claim_id WHERE c.employee_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reimbursements.add(mapReimbursement(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reimbursements;
    }

    @Override
    public List<Reimbursement> getReimbursementsByStatus(String status) {
        List<Reimbursement> reimbursements = new ArrayList<>();
        String sql = "SELECT * FROM reimbursements WHERE status = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reimbursements.add(mapReimbursement(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reimbursements;
    }

    private void fillReimbursement(PreparedStatement ps, Reimbursement reimbursement) throws SQLException {
        ps.setInt(1, reimbursement.getClaimId());
        ps.setDouble(2, reimbursement.getReimbursedAmount());
        ps.setString(3, reimbursement.getPaymentMode());
        ps.setString(4, reimbursement.getTransactionRef());
        if (reimbursement.getReimbursementDate() == null) {
            ps.setNull(5, java.sql.Types.DATE);
        } else {
            ps.setDate(5, Date.valueOf(reimbursement.getReimbursementDate()));
        }
        ps.setInt(6, reimbursement.getProcessedBy());
        ps.setString(7, reimbursement.getStatus());
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
