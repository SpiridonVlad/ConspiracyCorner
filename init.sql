
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS theory_evidence_urls;
DROP TABLE IF EXISTS theories;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    secret_code VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    is_anonymous BOOLEAN DEFAULT FALSE,
    reputation INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE theories (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    posted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    is_anonymous_post BOOLEAN DEFAULT FALSE,
    comment_count INTEGER DEFAULT 0,
    score INTEGER DEFAULT 0,
    author_id BIGINT NOT NULL,
    CONSTRAINT fk_theory_author FOREIGN KEY (author_id) REFERENCES users(id)
);

CREATE TABLE theory_evidence_urls (
    theory_id BIGINT NOT NULL,
    url VARCHAR(255),
    CONSTRAINT fk_evidence_theory FOREIGN KEY (theory_id) REFERENCES theories(id)
);

CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    posted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    is_anonymous_post BOOLEAN DEFAULT FALSE,
    author_id BIGINT NOT NULL,
    theory_id BIGINT NOT NULL,
    score INTEGER DEFAULT 0,
    CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES users(id),
    CONSTRAINT fk_comment_theory FOREIGN KEY (theory_id) REFERENCES theories(id)
);

CREATE TABLE votes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    theory_id BIGINT,
    comment_id BIGINT,
    value INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vote_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_vote_theory FOREIGN KEY (theory_id) REFERENCES theories(id),
    CONSTRAINT fk_vote_comment FOREIGN KEY (comment_id) REFERENCES comments(id),
    CONSTRAINT uq_vote_user_theory UNIQUE (user_id, theory_id),
    CONSTRAINT uq_vote_user_comment UNIQUE (user_id, comment_id),
    CONSTRAINT chk_vote_target CHECK (
        (theory_id IS NOT NULL AND comment_id IS NULL) OR 
        (theory_id IS NULL AND comment_id IS NOT NULL)
    )
);


INSERT INTO users (username, password, email, role, created_at, is_anonymous, secret_code) VALUES
('admin', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlNBxBFveB.qLG', 'admin@conspiracy.com', 'ADMIN', NOW(), false, 'admin_secret'),
('truther_99', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlNBxBFveB.qLG', 'truther@conspiracy.com', 'USER', NOW(), false, NULL),
('skeptic_dave', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlNBxBFveB.qLG', 'dave@skeptic.com', 'USER', NOW(), false, NULL),
('alien_hunter', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlNBxBFveB.qLG', 'fox@mulder.com', 'USER', NOW(), false, 'i_want_to_believe');


INSERT INTO theories (title, content, status, posted_at, updated_at, is_anonymous_post, author_id, comment_count) VALUES

('The Moon is Hollow', 'Think about it. Why does it ring like a bell when objects hit it? NASA knows the truth!', 'UNVERIFIED', NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days', false, (SELECT id FROM users WHERE username = 'truther_99'), 2),

('Birds arent Real', 'They are all surveillance drones created by the government. Have you ever seen a baby pigeon?', 'DEBUNKED', NOW() - INTERVAL '5 days', NOW() - INTERVAL '1 day', false, (SELECT id FROM users WHERE username = 'alien_hunter'), 1),

('The Pyramids were Power Plants', 'Ancient Egyptians had wireless electricity. The pyramids are just giant tesla coils.', 'CONFIRMED', NOW() - INTERVAL '1 week', NOW() - INTERVAL '1 week', false, (SELECT id FROM users WHERE username = 'truther_99'), 0),

('Area 52 is the real deal', 'Area 51 is a distraction. The real stuff happens at Area 52.', 'UNVERIFIED', NOW(), NOW(), true, (SELECT id FROM users WHERE username = 'alien_hunter'), 0);


INSERT INTO theory_evidence_urls (theory_id, url) VALUES
((SELECT id FROM theories WHERE title = 'The Moon is Hollow'), 'https://example.com/moon-hollow-proof'),
((SELECT id FROM theories WHERE title = 'The Moon is Hollow'), 'https://nasa-leaks.com/moon'),
((SELECT id FROM theories WHERE title = 'Birds arent Real'), 'https://birdsarentreal.com'),
((SELECT id FROM theories WHERE title = 'The Pyramids were Power Plants'), 'https://ancient-power.org');


INSERT INTO comments (content, posted_at, updated_at, is_anonymous_post, author_id, theory_id) VALUES

('This explains so much!', NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day', false, (SELECT id FROM users WHERE username = 'alien_hunter'), (SELECT id FROM theories WHERE title = 'The Moon is Hollow')),
('Complete nonsense. Basic physics disproves this.', NOW() - INTERVAL '12 hours', NOW() - INTERVAL '12 hours', false, (SELECT id FROM users WHERE username = 'skeptic_dave'), (SELECT id FROM theories WHERE title = 'The Moon is Hollow')),

('I saw a bird recharging on a power line yesterday.', NOW() - INTERVAL '4 days', NOW() - INTERVAL '4 days', true, (SELECT id FROM users WHERE username = 'truther_99'), (SELECT id FROM theories WHERE title = 'Birds arent Real'));
