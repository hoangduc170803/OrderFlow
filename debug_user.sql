-- Check user data
SELECT id, username, password, first_name, last_name, created_at 
FROM users 
WHERE username = 'customer1';

-- Check if user has roles
SELECT u.username, r.name as role_name 
FROM users u 
LEFT JOIN user_role ur ON u.id = ur.user_id 
LEFT JOIN roles r ON ur.role_id = r.id 
WHERE u.username = 'customer1';
