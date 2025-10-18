-- Test role assignment
-- Check if user has roles
SELECT u.id, u.username, r.name as role_name 
FROM users u 
LEFT JOIN user_role ur ON u.id = ur.user_id 
LEFT JOIN roles r ON ur.role_id = r.id 
WHERE u.username = 'customer1';

-- Check all users and their roles
SELECT u.id, u.username, u.first_name, u.last_name, r.name as role_name, r.description
FROM users u 
LEFT JOIN user_role ur ON u.id = ur.user_id 
LEFT JOIN roles r ON ur.role_id = r.id;

-- Check if user_role table exists and has data
SELECT * FROM user_role;
