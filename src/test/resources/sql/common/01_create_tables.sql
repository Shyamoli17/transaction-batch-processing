--Schema
CREATE SCHEMA IF NOt EXISTS transaction_processing;
SET search_path TO transaction_processing;

--sequences
CREATE SEQUENCE IF NOT EXISTS seq_tbl_statement_id START 1;
CREATE SEQUENCE IF NOT EXISTS seq_batch_error_tbl START 1 INCREMENT 1;

--Statement & Fee
CREATE TABLE IF NOT EXISTS tbl_statement (
    id BIGINT PRIMARY KEY DEFAULT nextval('seq_tbl_statement_id'),
    service_type VARCHAR(20),
    year_month VARCHAR(6),
    account_no VARCHAR(20),
    product_code VARCHAR(10),
    stmt_ind VARCHAR(2),
    stmt_type VARCHAR(2),
    stmt_date VARCHAR(8),
    fee_amt NUMERIC,
    status VARCHAR (10),
    created_by VARCHAR(50),
    created_ts TIMESTAMP,
    updated_by VARCHAR(50),
    updated_ts TIMESTAMP
);