-- Créé le 27/01/2026 pour gérer le reverse geocoding et améliorer la précision du positionnement
ALTER TABLE locations
ADD COLUMN country VARCHAR(100),
ADD COLUMN city VARCHAR(100),
ADD COLUMN country_code VARCHAR(5),
MODIFY latitude DECIMAL(9,6) NOT NULL,
MODIFY longitude DECIMAL(9,6) NOT NULL;