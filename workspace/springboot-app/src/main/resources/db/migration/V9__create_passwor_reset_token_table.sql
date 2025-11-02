-- Créé le 02/11/2025 pour gérer la réinitialisation de mot de passe
CREATE TABLE `password_reset_token` (
    `token_id` bigint NOT NULL AUTO_INCREMENT,
    `token` VARCHAR(255) NOT NULL UNIQUE,
    `user_id` BIGINT NOT NULL,
    `expiration` TIMESTAMP NOT NULL,
    PRIMARY KEY (`token_id`),
    CONSTRAINT `fk_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
);