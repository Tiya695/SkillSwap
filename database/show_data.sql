-- SkillSwap Verification Queries

USE skillswap;

SHOW TABLES;

-- Users table
SELECT id, name, email, date_of_birth, phone, avatar_url, role, LEFT(password, 20) AS password_hash_start
FROM users;

-- The skill catalog
SELECT id, name, category FROM skills ORDER BY category, name;

-- Who teaches (OFFERED) and who wants to learn (WANTED) what
SELECT u.name AS person, s.name AS skill, us.type, us.level
FROM user_skills us
JOIN users u ON u.id = us.user_id
JOIN skills s ON s.id = us.skill_id
ORDER BY u.name, us.type;

-- Swap requests
SELECT sr.id, a.name AS requester, b.name AS receiver,
       t.name AS requester_teaches, w.name AS requester_learns, sr.status, sr.created_at
FROM swap_requests sr
JOIN users a ON a.id = sr.requester_id
JOIN users b ON b.id = sr.receiver_id
JOIN skills t ON t.id = sr.skill_offered_id
JOIN skills w ON w.id = sr.skill_wanted_id
ORDER BY sr.id;

-- Reviews
SELECT r.id, rv.name AS reviewer, re.name AS reviewee, r.rating, r.comment
FROM reviews r
JOIN users rv ON rv.id = r.reviewer_id
JOIN users re ON re.id = r.reviewee_id;
