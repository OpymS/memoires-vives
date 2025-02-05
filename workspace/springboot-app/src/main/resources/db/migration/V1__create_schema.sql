-- MySQL dump 10.13  Distrib 8.0.40, for Linux (x86_64)
--
-- Host: localhost    Database: mv_db
-- ------------------------------------------------------
-- Server version	8.0.40
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `group_user`
--

DROP TABLE IF EXISTS `group_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_user` (
  `user_id` bigint NOT NULL,
  `group_id` bigint NOT NULL,
  KEY `FK94doopu6vtv06uhv97j20iwk8` (`group_id`),
  KEY `FKrqeo92wyuy7jcc54mfbln3wme` (`user_id`),
  CONSTRAINT `FK94doopu6vtv06uhv97j20iwk8` FOREIGN KEY (`group_id`) REFERENCES `ugroups` (`group_id`),
  CONSTRAINT `FKrqeo92wyuy7jcc54mfbln3wme` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `locations`
--

DROP TABLE IF EXISTS `locations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `locations` (
  `location_id` bigint NOT NULL AUTO_INCREMENT,
  `latitude` float NOT NULL,
  `longitude` float NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `memories`
--

DROP TABLE IF EXISTS `memories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `memories` (
  `memory_id` bigint NOT NULL AUTO_INCREMENT,
  `creation_date` datetime(6) DEFAULT NULL,
  `description` varchar(255) NOT NULL,
  `mediauuid` varchar(255) DEFAULT NULL,
  `memory_date` date NOT NULL,
  `memory_day` int NOT NULL,
  `memory_hour` int NOT NULL,
  `memory_minute` int NOT NULL,
  `memory_month` int NOT NULL,
  `memory_year` int NOT NULL,
  `modification_date` datetime(6) DEFAULT NULL,
  `state` tinyint NOT NULL,
  `title` varchar(255) NOT NULL,
  `visibility` tinyint NOT NULL,
  `location_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`memory_id`),
  KEY `FK8bd4oyff7tqseydypk05vgb1x` (`location_id`),
  KEY `FKia77spbkhm2x332hxi2bfj1md` (`user_id`),
  CONSTRAINT `FK8bd4oyff7tqseydypk05vgb1x` FOREIGN KEY (`location_id`) REFERENCES `locations` (`location_id`),
  CONSTRAINT `FKia77spbkhm2x332hxi2bfj1md` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `memories_chk_1` CHECK ((`state` between 0 and 2)),
  CONSTRAINT `memories_chk_2` CHECK ((`visibility` between 0 and 2))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `memory_group`
--

DROP TABLE IF EXISTS `memory_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `memory_group` (
  `memory_id` bigint NOT NULL,
  `group_id` bigint NOT NULL,
  KEY `FKm9i762e1g7qccialu4nbs61xn` (`memory_id`),
  KEY `FK97iwnob9pyfq2l734cfvl0tuf` (`group_id`),
  CONSTRAINT `FK97iwnob9pyfq2l734cfvl0tuf` FOREIGN KEY (`group_id`) REFERENCES `ugroups` (`group_id`),
  CONSTRAINT `FKm9i762e1g7qccialu4nbs61xn` FOREIGN KEY (`memory_id`) REFERENCES `memories` (`memory_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ugroups`
--

DROP TABLE IF EXISTS `ugroups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ugroups` (
  `group_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`group_id`),
  KEY `FK83kcx6a1nrajmcnjyrccxpbgr` (`user_id`),
  CONSTRAINT `FK83kcx6a1nrajmcnjyrccxpbgr` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_friends`
--

DROP TABLE IF EXISTS `user_friends`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_friends` (
  `user_id` bigint NOT NULL,
  `friend_id` bigint NOT NULL,
  KEY `FK11y5boh1e7gh60rdqixyetv3x` (`friend_id`),
  KEY `FKk08ugelrh9cea1oew3hgxryw2` (`user_id`),
  CONSTRAINT `FK11y5boh1e7gh60rdqixyetv3x` FOREIGN KEY (`friend_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FKk08ugelrh9cea1oew3hgxryw2` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `pseudo` varchar(255) NOT NULL,
  `role` varchar(255) NOT NULL,
  `is_activated` bit(1) NOT NULL,
  `is_admin` bit(1) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UKr9i2upm423j62a0neosbc8ucq` (`pseudo`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-02-05 20:35:01
