-- Créé le 05/11/2025 pour gérer le remember me
CREATE TABLE `persistent_logins` (
    `username` VARCHAR(64) NOT NULL,
    `series` VARCHAR(64),
    `token` VARCHAR(64) NOT NULL,
    `last_used` TIMESTAMP NOT NULL,
    PRIMARY KEY (`series`)
);