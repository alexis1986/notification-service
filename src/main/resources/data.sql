MERGE INTO api_keys (client_id, api_key) KEY(api_key) VALUES ('client-a', 'key-for-client-a');
MERGE INTO api_keys (client_id, api_key) KEY(api_key) VALUES ('client-b', 'super-secret-key-for-b');
