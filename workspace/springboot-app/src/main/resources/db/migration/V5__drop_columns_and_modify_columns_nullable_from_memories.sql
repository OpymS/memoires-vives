-- Créé le 06/03/2025 suite modification de l'entity
ALTER TABLE memories 
DROP COLUMN memory_year,
DROP COLUMN memory_month,
DROP COLUMN memory_day,
DROP COLUMN memory_hour,
DROP COLUMN memory_minute;

ALTER TABLE memories
MODIFY COLUMN creation_date DATETIME(6) NOT NULL,
MODIFY COLUMN state VARCHAR(50) NOT NULL,
MODIFY COLUMN visibility VARCHAR(50) NOT NULL;