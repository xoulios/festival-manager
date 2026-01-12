INSERT INTO roles (id, name, created_at, updated_at) VALUES
(1, 'PROGRAMMER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'STAFF',      CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'SUBMITTER',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'ORGANIZER',  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'ARTIST',     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (id, username, email, password, enabled, created_at, updated_at) VALUES
(1, 'programmer1', 'programmer1@example.com',
'$2b$10$hrhlad0XEOrLOrPUhvfsOutI/cYKV5DYEeg8MwZadrjYoV0GEv.DC', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'staff1', 'staff1@example.com',
'$2b$10$hrhlad0XEOrLOrPUhvfsOutI/cYKV5DYEeg8MwZadrjYoV0GEv.DC', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'artist1', 'artist1@example.com',
'$2b$10$hrhlad0XEOrLOrPUhvfsOutI/cYKV5DYEeg8MwZadrjYoV0GEv.DC', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'organizer1', 'organizer1@example.com',
'$2b$10$hrhlad0XEOrLOrPUhvfsOutI/cYKV5DYEeg8MwZadrjYoV0GEv.DC', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO festivals (id, title, description, start_date, end_date, state, created_at, updated_at) VALUES
(1, 'Demo Festival', NULL, CURRENT_DATE, DATEADD('DAY', 1, CURRENT_DATE), 'CREATED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_festival_roles (user_id, festival_id, role_id) VALUES
(1, 1, 1),
(2, 1, 2),
(3, 1, 3),
(4, 1, 4);
