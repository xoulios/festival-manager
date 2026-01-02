INSERT INTO roles (id, name, created_at, updated_at) VALUES
(1, 'PROGRAMMER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'STAFF',      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'SUBMITTER',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (id, username, email, password, enabled, created_at, updated_at) VALUES
(1, 'programmer1', 'programmer1@example.com', 
'$2a$10$7EqJtq98hPqEX7fNZaFWoOHi4xWQW5E9OQj1YxDCEV4VpWw2X2gai', TRUE, 
CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'staff1',      'staff1@example.com',      
'$2a$10$7EqJtq98hPqEX7fNZaFWoOHi4xWQW5E9OQj1YxDCEV4VpWw2X2gai', TRUE, 
CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'artist1',     'artist1@example.com',     
'$2a$10$7EqJtq98hPqEX7fNZaFWoOHi4xWQW5E9OQj1YxDCEV4VpWw2X2gai', TRUE, 
CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO festivals (id, title, description, start_date, end_date, state) VALUES
(1, 'Demo Festival', NULL, CURRENT_DATE, DATEADD('DAY', 1, CURRENT_DATE), 'CREATED');

INSERT INTO user_festival_roles (user_id, festival_id, role_id) VALUES
(1, 1, 1), 
(2, 1, 2);
