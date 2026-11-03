USE driverse_db;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS class_enrollments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    module_name VARCHAR(100) NOT NULL,
    enrolled_on DATE
);

CREATE TABLE IF NOT EXISTS learner_modules (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    module_number INT NOT NULL,
    module_title VARCHAR(100) NOT NULL,
    note VARCHAR(150),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
);

CREATE TABLE IF NOT EXISTS mock_tests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    test_name VARCHAR(100) NOT NULL,
    score DECIMAL(5,2) NOT NULL DEFAULT 0,
    attempts INT NOT NULL DEFAULT 1,
    taken_date DATE NOT NULL,
    completed TINYINT(1) NOT NULL DEFAULT 1,
    completed_at DATE NULL
);

CREATE TABLE IF NOT EXISTS payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    paid_on DATE,
    description VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS progress (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    theory_pct INT NOT NULL DEFAULT 0,
    practical_pct INT NOT NULL DEFAULT 0,
    mock_test_pct INT NOT NULL DEFAULT 0,
    assignment_pct INT NOT NULL DEFAULT 0
);

CREATE OR REPLACE VIEW upcoming_classes AS
SELECT 1 AS user_id, id, class_date, class_time,
       class_type AS title,
       CASE WHEN vehicle LIKE 'N/A%' THEN 'Indoor Theory Hall' ELSE vehicle END AS location,
       instructor, status
FROM class_schedule
WHERE status = 'Scheduled';

INSERT INTO users (full_name, email) VALUES ('Sneha Nair', 'sneha@gmail.com');

INSERT INTO class_enrollments (user_id, module_name, enrolled_on) VALUES
(1, 'Road Signs and Rules',   CURDATE() - INTERVAL 20 DAY),
(1, 'Vehicle Controls',       CURDATE() - INTERVAL 18 DAY),
(1, 'Parking and Reversing',  CURDATE() - INTERVAL 10 DAY),
(1, 'City Driving',           CURDATE() - INTERVAL 3 DAY);

INSERT INTO learner_modules (user_id, module_number, module_title, note, status) VALUES
(1, 1, 'Road Signs and Rules',  'Completed',          'COMPLETED'),
(1, 2, 'Vehicle Controls',      'Completed',          'COMPLETED'),
(1, 3, 'Parking and Reversing', 'Practical - 1 hour',  'PENDING'),
(1, 4, 'City Driving',          'Practical - 1 hour',  'PENDING'),
(1, 5, 'Highway Driving',       'Practical - 2 hours', 'PENDING'),
(1, 6, 'Night Driving',         'Practical - 1 hour',  'PENDING');

INSERT INTO mock_tests (user_id, test_name, score, attempts, taken_date, completed, completed_at) VALUES
(1, 'Road Signs Test',         82.50, 1, CURDATE() - INTERVAL 12 DAY, 1, CURDATE() - INTERVAL 12 DAY),
(1, 'Traffic Rules Test',      74.00, 2, CURDATE() - INTERVAL 8 DAY,  1, CURDATE() - INTERVAL 8 DAY),
(1, 'Learner Licence Mock 1',  88.00, 1, CURDATE() - INTERVAL 4 DAY,  1, CURDATE() - INTERVAL 4 DAY);

INSERT INTO payments (user_id, amount, paid_on, description) VALUES
(1, 5000.00, CURDATE() - INTERVAL 25 DAY, 'Course fee - first instalment'),
(1, 3000.00, CURDATE() - INTERVAL 5 DAY,  'Course fee - second instalment');

INSERT INTO progress (user_id, theory_pct, practical_pct, mock_test_pct, assignment_pct)
VALUES (1, 70, 45, 60, 80);

SELECT * FROM upcoming_classes;