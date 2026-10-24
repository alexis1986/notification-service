-- API Keys de demo, guardadas como SHA-256. Valores en claro (solo para la demo):
-- client-a -> key-for-client-a, client-b -> super-secret-key-for-b
INSERT INTO api_keys (client_id, key_hash) VALUES
    ('client-a', '94562fa3e0c6846a1f7bfe7b351e89eda94f3b5bd384d442e31d8de68ae4a6ff'),
    ('client-b', 'e2d2ad39751421f648a4093ceb2d73e4b05a704a19843180e8a1bc6bde606306')
ON CONFLICT (key_hash) DO NOTHING;
