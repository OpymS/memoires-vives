-- Créé le 07/03/2025 pour ajouter une photo de profil et permettre des descriptions plus longues
ALTER TABLE users
ADD mediauuid VARCHAR(255) DEFAULT NULL;

ALTER TABLE memories
MODIFY description TEXT;
