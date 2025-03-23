CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL REFERENCES users(id),
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

-- Inserir um usuário administrador padrão
INSERT INTO users (
    id, 
    username, 
    password, 
    full_name, 
    email, 
    phone, 
    account_non_expired, 
    account_non_locked, 
    credentials_non_expired, 
    enabled, 
    active, 
    version, 
    created_at, 
    updated_at
) VALUES (
    gen_random_uuid(),
    'admin',
    '$2a$10$5JoaWJRLLAHuDlTwCbSLMeFrcKTySO.6X3KPIBNjMSaqJMtZf3X42', -- senha: admin123
    'Administrador',
    'admin@rgbnet.com',
    NULL,
    TRUE,
    TRUE,
    TRUE,
    TRUE,
    TRUE,
    0,
    NOW(),
    NOW()
);

-- Atribuir papel de administrador
INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE username = 'admin'; 