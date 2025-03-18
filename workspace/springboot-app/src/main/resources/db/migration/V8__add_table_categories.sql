-- Créé le 18/03/2025 pour ajouter des catégories aux souvenirs
CREATE TABLE `categories` (
  `category_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`category_id`)
);

ALTER TABLE memories 
ADD COLUMN category_id bigint;

INSERT INTO categories (name)
VALUES ('Default category');

UPDATE memories SET category_id = (SELECT category_id FROM categories WHERE name = 'Default category');

ALTER TABLE memories MODIFY category_id bigint NOT NULL;

ALTER TABLE memories ADD CONSTRAINT `FKk7u2dgokaxf1ve2q9wtwxl1le` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`);