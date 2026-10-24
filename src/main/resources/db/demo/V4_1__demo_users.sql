-- Usuarios de demo: user1 existe en los dos clientes con preferencias distintas; user2 solo en client-a
INSERT INTO users (client_id, user_id, preferred_channel) VALUES
    ('client-a', 'user1', 'EMAIL'),
    ('client-b', 'user1', 'PUSH'),
    ('client-a', 'user2', 'WHATSAPP')
ON CONFLICT DO NOTHING;

-- Emails en el dominio de prueba de Resend: simulan entregas sin rebotes que dañen la reputación del remitente
INSERT INTO user_addresses (client_id, user_id, channel, address) VALUES
    ('client-a', 'user1', 'EMAIL',    'delivered+user1-client-a@resend.dev'),
    ('client-a', 'user1', 'WHATSAPP', '+1111111111'),
    ('client-a', 'user1', 'PUSH',     'push-a-1'),
    ('client-b', 'user1', 'EMAIL',    'delivered+user1-client-b@resend.dev'),
    ('client-b', 'user1', 'WHATSAPP', '+2222222222'),
    ('client-b', 'user1', 'PUSH',     'push-b-1'),
    ('client-a', 'user2', 'EMAIL',    'delivered+user2-client-a@resend.dev'),
    ('client-a', 'user2', 'WHATSAPP', '+3333333333'),
    ('client-a', 'user2', 'PUSH',     'push-a-2')
ON CONFLICT DO NOTHING;
