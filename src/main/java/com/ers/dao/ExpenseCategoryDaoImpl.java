package com.ers.dao;

import com.ers.model.ExpenseCategory;
import com.ers.util.JDBCUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ExpenseCategoryDaoImpl implements IExpenseCategoryDao {
    private final JDBCUtil jdbcUtil;

    public ExpenseCategoryDaoImpl(JDBCUtil jdbcUtil) {
        this.jdbcUtil = jdbcUtil;
    }

    @Override
    public ExpenseCategory addExpenseCategory(ExpenseCategory expenseCategory) {
        String sql = "INSERT INTO expense_categories (category_name, description) VALUES (?, ?)";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, expenseCategory.getCategory_name());
            ps.setString(2, expenseCategory.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    expenseCategory.setCategory_id(keys.getInt(1));
                }
            }
            return expenseCategory;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean updateExpenseCategory(ExpenseCategory expenseCategory) {
        String sql = "UPDATE expense_categories SET category_name = ?, description = ? WHERE category_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, expenseCategory.getCategory_name());
            ps.setString(2, expenseCategory.getDescription());
            ps.setInt(3, expenseCategory.getCategory_id());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public ExpenseCategory getExpenseCategoryById(int categoryId) {
        String sql = "SELECT * FROM expense_categories WHERE category_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCategory(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ExpenseCategory> getAllExpenseCategories() {
        List<ExpenseCategory> categories = new ArrayList<>();
        String sql = "SELECT * FROM expense_categories";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categories.add(mapCategory(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    @Override
    public boolean deleteExpenseCategoryById(int categoryId) {
        String sql = "DELETE FROM expense_categories WHERE category_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ExpenseCategory mapCategory(ResultSet rs) throws SQLException {
        ExpenseCategory category = new ExpenseCategory(
                rs.getString("category_name"),
                rs.getString("description")
        );
        category.setCategory_id(rs.getInt("category_id"));
        return category;
    }
}
