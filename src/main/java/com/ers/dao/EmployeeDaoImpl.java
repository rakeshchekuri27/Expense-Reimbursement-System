package com.ers.dao;

import com.ers.model.Employee;
import com.ers.util.JDBCUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDaoImpl implements IEmployeeDao {
    private final JDBCUtil jdbcUtil;

    public EmployeeDaoImpl(JDBCUtil jdbcUtil) {
        this.jdbcUtil = jdbcUtil;
    }

    @Override
    public Employee addEmployee(Employee employee) {
        String sql = "INSERT INTO employees (user_id, full_name, email, department_id) VALUES (?, ?, ?, ?)";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, employee.getUserId());
            ps.setString(2, employee.getFullName());
            ps.setString(3, employee.getEmail());
            setNullableInt(ps, 4, employee.getDepartmentId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    employee.setEmployeeId(keys.getInt(1));
                }
            }
            return employee;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean updateEmployee(Employee employee) {
        String sql = "UPDATE employees SET user_id = ?, full_name = ?, email = ?, department_id = ? WHERE employee_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employee.getUserId());
            ps.setString(2, employee.getFullName());
            ps.setString(3, employee.getEmail());
            setNullableInt(ps, 4, employee.getDepartmentId());
            ps.setInt(5, employee.getEmployeeId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Employee getEmployeeById(int employeeId) {
        String sql = "SELECT * FROM employees WHERE employee_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapEmployee(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                employees.add(mapEmployee(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    @Override
    public boolean deleteEmployeeById(int employeeId) {
        String sql = "DELETE FROM employees WHERE employee_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Employee mapEmployee(ResultSet rs) throws SQLException {
        int departmentId = rs.getInt("department_id");
        if (rs.wasNull()) {
            departmentId = 0;
        }
        Employee employee = new Employee(
                rs.getInt("user_id"),
                rs.getString("full_name"),
                rs.getString("email"),
                departmentId
        );
        employee.setEmployeeId(rs.getInt("employee_id"));
        return employee;
    }

    private void setNullableInt(PreparedStatement ps, int index, int value) throws SQLException {
        if (value <= 0) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }
}
