-- Créé le 26/02/2025 pour  stocker les enum sous forme de string
ALTER TABLE memories 
MODIFY visibility VARCHAR(50),
MODIFY state VARCHAR(50);