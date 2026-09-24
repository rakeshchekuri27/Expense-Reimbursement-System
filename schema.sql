DROP DATABASE IF EXISTS expense_reimbursement_system;
CREATE DATABASE expense_reimbursement_system;

USE expense_reimbursement_system;


CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'EMPLOYEE', 'MANAGER', 'FINANCE_EXECUTIVE') NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE departments (
    department_id INT PRIMARY KEY AUTO_INCREMENT,
    department_name VARCHAR(100) NOT NULL,
    manager_id INT
);


CREATE TABLE employees (
    employee_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    department_id INT,

    CONSTRAINT fk_employee_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id),

    CONSTRAINT fk_employee_department
        FOREIGN KEY (department_id)
        REFERENCES departments(department_id)
);


ALTER TABLE departments
ADD CONSTRAINT fk_department_manager
FOREIGN KEY (manager_id)
REFERENCES employees(employee_id);

CREATE TABLE expense_categories (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE expense_claims (
    claim_id INT PRIMARY KEY AUTO_INCREMENT,
    employee_id INT NOT NULL,
    claim_description TEXT,
    claim_date DATE NOT NULL,
    claim_amount DECIMAL(10,2) NOT NULL,
    status ENUM('DRAFT', 'SUBMITTED', 'APPROVED', 'REJECTED', 'REIMBURSED')
        NOT NULL DEFAULT 'DRAFT',
    document_path VARCHAR(255),
    reason VARCHAR(200),

    CONSTRAINT fk_claim_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(employee_id)
);

CREATE TABLE claim_items (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    claim_id INT NOT NULL,
    category_id INT NOT NULL,
    description VARCHAR(255),
    amount DECIMAL(10,2) NOT NULL,
    expense_date DATE NOT NULL,

    CONSTRAINT fk_item_claim
        FOREIGN KEY (claim_id)
        REFERENCES expense_claims(claim_id),

    CONSTRAINT fk_item_category
        FOREIGN KEY (category_id)
        REFERENCES expense_categories(category_id)
);


CREATE TABLE finance_executives (
    employee_id INT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    department VARCHAR(100),

    CONSTRAINT fk_finance_employee
        FOREIGN KEY (employee_id)
        REFERENCES employees(employee_id)
);

CREATE TABLE reimbursements (
    reimbursement_id INT PRIMARY KEY AUTO_INCREMENT,
    claim_id INT NOT NULL UNIQUE,
    reimbursed_amount DECIMAL(10,2) NOT NULL,
    payment_mode ENUM('BANK_TRANSFER', 'CASH', 'UPI', 'CHEQUE') NOT NULL,
    transaction_ref VARCHAR(100),
    reimbursement_date DATE,
    processed_by INT NOT NULL,
    status ENUM('PENDING', 'COMPLETED', 'FAILED')
        NOT NULL DEFAULT 'PENDING',

    CONSTRAINT fk_reimbursement_claim
        FOREIGN KEY (claim_id)
        REFERENCES expense_claims(claim_id),

    CONSTRAINT fk_reimbursement_finance
        FOREIGN KEY (processed_by)
        REFERENCES finance_executives(employee_id)
);
