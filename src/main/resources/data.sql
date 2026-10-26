
INSERT INTO skills (
    skill_name, normalized_name, category, description,
    is_active, created_at, updated_at
)
SELECT
    'java', 'Java', 'PROGRAMMING_LANGUAGE', 'Core Java',
    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM skills WHERE normalized_name = 'java'
);

INSERT INTO skills (
    skill_name, normalized_name, category, description,
    is_active, created_at, updated_at
)
SELECT
    'spring-boot', 'Spring Boot', 'FRAMEWORK', 'Spring Boot framework',
    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM skills WHERE normalized_name = 'spring boot'
);

INSERT INTO skills (
    skill_name, normalized_name, category, description,
    is_active, created_at, updated_at
)
SELECT
    'angular', 'Angular', 'FRAMEWORK', 'Angular frontend framework',
    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM skills WHERE normalized_name = 'angular'
);

INSERT INTO skills (
    skill_name, normalized_name, category, description,
    is_active, created_at, updated_at
)
SELECT
    'react', 'React', 'FRAMEWORK', 'React frontend framework',
    TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM skills WHERE normalized_name = 'react'
);
