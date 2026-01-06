-- Créé le 06/01/2026 pour rendre la colonne slug non nullable
ALTER TABLE memories
MODIFY COLUMN slug VARCHAR(270) NOT NULL;