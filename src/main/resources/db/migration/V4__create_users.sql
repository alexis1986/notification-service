CREATE TABLE users (
    client_id         VARCHAR(255) NOT NULL,
    user_id           VARCHAR(255) NOT NULL,
    preferred_channel VARCHAR(20)  NOT NULL,
    PRIMARY KEY (client_id, user_id)
);

-- Una fila por canal: sumar un canal nuevo es un dato, no una migración
CREATE TABLE user_addresses (
    client_id VARCHAR(255) NOT NULL,
    user_id   VARCHAR(255) NOT NULL,
    channel   VARCHAR(20)  NOT NULL,
    address   VARCHAR(255) NOT NULL,
    PRIMARY KEY (client_id, user_id, channel),
    FOREIGN KEY (client_id, user_id) REFERENCES users (client_id, user_id) ON DELETE CASCADE
);
