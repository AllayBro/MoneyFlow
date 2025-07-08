
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(64) UNIQUE NOT NULL,
    password VARCHAR(64) NOT NULL
    );

CREATE TABLE IF NOT EXISTS accounts (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    balance DOUBLE PRECISION NOT NULL,
    currency VARCHAR(3) NOT NULL,
    account_number VARCHAR(32) UNIQUE NOT NULL
    );

CREATE TABLE IF NOT EXISTS transfers (

    id SERIAL PRIMARY KEY,
    from_account_number VARCHAR(255) NOT NULL,
    to_account_number VARCHAR(255) NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    user_id INT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    currency VARCHAR(3) NOT NULL
);

