CREATE DATABASE IF NOT EXISTS skillswap;
USE skillswap;

SET FOREIGN_KEY_CHECKS = 0;
CREATE TABLE IF NOT EXISTS `assessment_questions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `answer` varchar(2000) DEFAULT NULL,
  `awarded_points` int DEFAULT NULL,
  `feedback` varchar(500) DEFAULT NULL,
  `max_points` int NOT NULL,
  `position` int NOT NULL,
  `text` varchar(500) NOT NULL,
  `assessment_id` bigint NOT NULL,
  `correct_option` int DEFAULT NULL,
  `options_json` varchar(2000) DEFAULT NULL,
  `selected_option` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKljemcscn19ij7kysiqewaqp88` (`assessment_id`),
  CONSTRAINT `FKljemcscn19ij7kysiqewaqp88` FOREIGN KEY (`assessment_id`) REFERENCES `assessments` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS `assessments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `attempt` int NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `passed` bit(1) DEFAULT NULL,
  `score_percent` int DEFAULT NULL,
  `status` enum('GRADED','OPEN','SUBMITTED') NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `learner_id` bigint NOT NULL,
  `skill_id` bigint NOT NULL,
  `swap_request_id` bigint NOT NULL,
  `teacher_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKe7vq89w4xk0n4xnacl1np9gcl` (`swap_request_id`,`teacher_id`),
  KEY `FKid88d73lia10rll0iaxntl3ui` (`learner_id`),
  KEY `FK2eb58l2mtpavfd2svtrhk3so5` (`skill_id`),
  KEY `FKdjiwcc28ntgda377a882y5qsb` (`teacher_id`),
  CONSTRAINT `FK2eb58l2mtpavfd2svtrhk3so5` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`),
  CONSTRAINT `FKdjiwcc28ntgda377a882y5qsb` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKefaqs6w03jwwaknm4sh1awet` FOREIGN KEY (`swap_request_id`) REFERENCES `swap_requests` (`id`),
  CONSTRAINT `FKid88d73lia10rll0iaxntl3ui` FOREIGN KEY (`learner_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS `reviews` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comment` varchar(500) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `rating` int NOT NULL,
  `reviewee_id` bigint NOT NULL,
  `reviewer_id` bigint NOT NULL,
  `swap_request_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKh9dfh1o9ijtkyqvjgcbgmfobm` (`swap_request_id`,`reviewer_id`),
  KEY `FK1sjw2c8j34ew366vfqxergp0b` (`reviewee_id`),
  KEY `FKd1isgfajhtdl8mgg29up6mofi` (`reviewer_id`),
  CONSTRAINT `FK1sjw2c8j34ew366vfqxergp0b` FOREIGN KEY (`reviewee_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FK6a73jbhur09ag8y1av28sgg7v` FOREIGN KEY (`swap_request_id`) REFERENCES `swap_requests` (`id`),
  CONSTRAINT `FKd1isgfajhtdl8mgg29up6mofi` FOREIGN KEY (`reviewer_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS `skills` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` varchar(255) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK85woe63nu9klkk9fa73vf0jd0` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS `swap_messages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `text` varchar(1000) NOT NULL,
  `sender_id` bigint NOT NULL,
  `swap_request_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX3g0x6tswaclqinqereep7x0o6` (`swap_request_id`,`id`),
  KEY `FK6vg74xxc3gcbk819lul27s05w` (`sender_id`),
  CONSTRAINT `FK4g8ertsov7dd32p4eg8arnikp` FOREIGN KEY (`swap_request_id`) REFERENCES `swap_requests` (`id`),
  CONSTRAINT `FK6vg74xxc3gcbk819lul27s05w` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS `swap_requests` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `message` varchar(500) DEFAULT NULL,
  `status` enum('ACCEPTED','CANCELLED','COMPLETED','PENDING','REJECTED') NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `receiver_id` bigint NOT NULL,
  `requester_id` bigint NOT NULL,
  `skill_offered_id` bigint NOT NULL,
  `skill_wanted_id` bigint NOT NULL,
  `duration_days` int NOT NULL DEFAULT '14',
  `end_date` date DEFAULT NULL,
  `meeting_url` varchar(500) DEFAULT NULL,
  `receiver_finished_early` bit(1) NOT NULL DEFAULT b'0',
  `requester_finished_early` bit(1) NOT NULL DEFAULT b'0',
  `start_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKaydr0x5sn0g0sg24tfneu8ma7` (`receiver_id`),
  KEY `FKhw8nk86dcyqihpjybxphelcfk` (`requester_id`),
  KEY `FKehusvejkq9physb3m2lnn8k3b` (`skill_offered_id`),
  KEY `FKaollorb63ao8pd4amgh32649x` (`skill_wanted_id`),
  CONSTRAINT `FKaollorb63ao8pd4amgh32649x` FOREIGN KEY (`skill_wanted_id`) REFERENCES `skills` (`id`),
  CONSTRAINT `FKaydr0x5sn0g0sg24tfneu8ma7` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKehusvejkq9physb3m2lnn8k3b` FOREIGN KEY (`skill_offered_id`) REFERENCES `skills` (`id`),
  CONSTRAINT `FKhw8nk86dcyqihpjybxphelcfk` FOREIGN KEY (`requester_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS `user_skills` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `level` enum('ADVANCED','BEGINNER','INTERMEDIATE') NOT NULL,
  `notes` varchar(300) DEFAULT NULL,
  `type` enum('OFFERED','WANTED') NOT NULL,
  `skill_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK7w8a2o115am6kfgahhdwm36va` (`user_id`,`skill_id`,`type`),
  KEY `FKh223y61gwijpgqt6nlsuti07g` (`skill_id`),
  CONSTRAINT `FKh223y61gwijpgqt6nlsuti07g` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`),
  CONSTRAINT `FKro13if9r7fwkr5115715127ai` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `avatar_url` varchar(255) DEFAULT NULL,
  `bio` varchar(500) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phone` varchar(15) DEFAULT NULL,
  `role` enum('ADMIN','USER') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;

