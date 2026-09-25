package com.ers.dao;

import com.ers.model.ClaimItem;
import com.ers.util.JDBCUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClaimItemDaoImpl implements IClaimItemDao {
    private final JDBCUtil jdbcUtil;

    public ClaimItemDaoImpl(JDBCUtil jdbcUtil) {
        this.jdbcUtil = jdbcUtil;
    }

    @Override
    public ClaimItem addClaimItem(ClaimItem claimItem) {
        String sql = "INSERT INTO claim_items (claim_id, category_id, description, amount, expense_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, claimItem.getClaimId());
            ps.setInt(2, claimItem.getCategoryId());
            ps.setString(3, claimItem.getDescription());
            ps.setDouble(4, claimItem.getAmount());
            ps.setDate(5, Date.valueOf(claimItem.getExpenseDate()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    claimItem.setItemId(keys.getInt(1));
                }
            }
            return claimItem;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean updateClaimItem(ClaimItem claimItem) {
        String sql = "UPDATE claim_items SET claim_id = ?, category_id = ?, description = ?, amount = ?, expense_date = ? WHERE item_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, claimItem.getClaimId());
            ps.setInt(2, claimItem.getCategoryId());
            ps.setString(3, claimItem.getDescription());
            ps.setDouble(4, claimItem.getAmount());
            ps.setDate(5, Date.valueOf(claimItem.getExpenseDate()));
            ps.setInt(6, claimItem.getItemId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public ClaimItem getClaimItemById(int itemId) {
        String sql = "SELECT * FROM claim_items WHERE item_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapItem(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ClaimItem> getAllClaimItems() {
        List<ClaimItem> items = new ArrayList<>();
        String sql = "SELECT * FROM claim_items";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(mapItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    @Override
    public boolean deleteClaimItemById(int itemId) {
        String sql = "DELETE FROM claim_items WHERE item_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<ClaimItem> getClaimItemsByClaimId(int claimId) {
        List<ClaimItem> items = new ArrayList<>();
        String sql = "SELECT * FROM claim_items WHERE claim_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, claimId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapItem(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    private ClaimItem mapItem(ResultSet rs) throws SQLException {
        Date expenseDate = rs.getDate("expense_date");
        ClaimItem item = new ClaimItem(
                rs.getInt("claim_id"),
                rs.getInt("category_id"),
                rs.getString("description"),
                rs.getDouble("amount"),
                expenseDate == null ? null : expenseDate.toLocalDate()
        );
        item.setItemId(rs.getInt("item_id"));
        return item;
    }
}
