package com.ers.dao;

import com.ers.model.Department;
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

public class DepartmentDaoImpl implements IDepartmentDao {
    private final JDBCUtil jdbcUtil;

    public DepartmentDaoImpl(JDBCUtil jdbcUtil) {
        this.jdbcUtil = jdbcUtil;
    }

    @Override
    public Department addDepartment(Department department) {
        String sql = "INSERT INTO departments (department_name, manager_id) VALUES (?, ?)";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, department.getDepartmentName());
            setNullableInt(ps, 2, department.getManagerId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    department.setDepartmentId(keys.getInt(1));
                }
            }
            return department;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean updateDepartment(Department department) {
        String sql = "UPDATE departments SET department_name = ?, manager_id = ? WHERE department_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, department.getDepartmentName());
            setNullableInt(ps, 2, department.getManagerId());
            ps.setInt(3, department.getDepartmentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Department getDepartmentById(int departmentId) {
        String sql = "SELECT * FROM departments WHERE department_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapDepartment(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Department> getAllDepartments() {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT * FROM departments";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                departments.add(mapDepartment(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return departments;
    }

    @Override
    public boolean deleteDepartmentById(int departmentId) {
        String sql = "DELETE FROM departments WHERE department_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Employee> getEmployeesByDepartmentId(int departmentId) {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT * FROM employees WHERE department_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, departmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    employees.add(mapEmployee(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }

    @Override
    public Department getDepartmentByManagerId(int managerId) {
        String sql = "SELECT * FROM departments WHERE manager_id = ?";
        try (Connection con = jdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapDepartment(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Department mapDepartment(ResultSet rs) throws SQLException {
        int managerId = rs.getInt("manager_id");
        if (rs.wasNull()) {
            managerId = 0;
        }
        Department department = new Department(rs.getString("department_name"), managerId);
        department.setDepartmentId(rs.getInt("department_id"));
        return department;
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
