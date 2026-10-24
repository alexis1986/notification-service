-- Usuarios de demo: user1 existe en los dos clientes con preferencias distintas; user2 solo en client-a
INSERT INTO users (client_id, user_id, preferred_channel) VALUES
    ('client-a', 'user1', 'EMAIL'),
    ('client-b', 'user1', 'PUSH'),
    ('client-a', 'user2', 'WHATSAPP')
ON CONFLICT DO NOTHING;

INSERT INTO user_addresses (client_id, user_id, channel, address) VALUES
    ('client-a', 'user1', 'EMAIL',    'user1@client-a.com'),
    ('client-a', 'user1', 'WHATSAPP', '+1111111111'),
    ('client-a', 'user1', 'PUSH',     'push-a-1'),
    ('client-b', 'user1', 'EMAIL',    'user1@client-b.com'),
    ('client-b', 'user1', 'WHATSAPP', '+2222222222'),
    ('client-b', 'user1', 'PUSH',     'push-b-1'),
    ('client-a', 'user2', 'EMAIL',    'user2@client-a.com'),
    ('client-a', 'user2', 'WHATSAPP', '+3333333333'),
    ('client-a', 'user2', 'PUSH',     'push-a-2')
ON CONFLICT DO NOTHING;
