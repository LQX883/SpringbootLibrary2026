-- MySQL dump 10.13  Distrib 8.4.7, for Win64 (x86_64)
--
-- Host: localhost    Database: springboot_library_2026
-- ------------------------------------------------------
-- Server version	8.4.7

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `springboot_library_2026`
--

/*!40000 DROP DATABASE IF EXISTS `springboot_library_2026`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `springboot_library_2026` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `springboot_library_2026`;

--
-- Table structure for table `book`
--

DROP TABLE IF EXISTS `book`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `book` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `author` varchar(60) NOT NULL,
  `available_copies` int NOT NULL,
  `cover_url` varchar(500) DEFAULT NULL,
  `loan_days` int DEFAULT NULL,
  `introduction` varchar(2000) DEFAULT NULL,
  `isbn` varchar(30) DEFAULT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `publisher` varchar(60) DEFAULT NULL,
  `shelf_no` varchar(30) DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  `title` varchar(100) NOT NULL,
  `total_copies` int NOT NULL,
  `type` varchar(30) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKehpdfjpu1jm3hijhj4mm0hx9h` (`isbn`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `book`
--

LOCK TABLES `book` WRITE;
/*!40000 ALTER TABLE `book` DISABLE KEYS */;
INSERT INTO `book` VALUES (1,'2026-11-13 09:09:52.689757','2026-11-13 09:09:52.745449','刘慈欣',0,NULL,30,'地球文明与三体文明的信息交流、生死搏杀及两个文明在宇宙中的兴衰历程。','9787115000001',32.00,'重庆出版社','A-01','ACTIVE','三体',1,'文学'),(2,'2026-11-13 09:09:52.689757','2026-11-13 09:09:52.756132','余华',5,NULL,30,'讲述一个人历尽世间沧桑和磨难的一生，亦将中国大半个世纪的社会变迁凝缩其间。','9787115000002',28.00,'作家出版社','A-02','ACTIVE','活着',6,'文学'),(3,'2026-11-13 09:09:52.689757','2026-11-13 09:09:52.756132','加西亚·马尔克斯',0,NULL,30,'布恩迪亚家族七代人的传奇故事，以及加勒比海沿岸小镇马孔多的百年兴衰。','9787115000003',39.50,'南海出版公司','A-03','ACTIVE','百年孤独',1,'文学'),(4,'2026-11-13 09:09:52.689757','2026-11-13 09:09:52.756132','曹雪芹',7,NULL,30,'中国古典四大名著之一，以贾、史、王、薛四大家族的兴衰为背景。','9787115000004',59.70,'人民文学出版社','A-04','ACTIVE','红楼梦',8,'文学'),(5,'2026-11-13 09:09:52.699908','2026-11-13 09:09:52.699908','路遥',6,NULL,30,'全景式地表现中国当代城乡社会生活的长篇小说。','9787115000005',49.00,'北京十月文艺出版社','A-05','ACTIVE','平凡的世界',6,'文学'),(6,'2026-11-13 09:09:52.699908','2026-11-13 09:09:52.699908','钱钟书',4,NULL,30,'以幽默而辛辣的笔触描绘抗战初期知识分子的群像。','9787115000006',36.00,'人民文学出版社','A-06','ACTIVE','围城',4,'文学'),(7,'2026-11-13 09:09:52.699908','2026-11-13 09:09:52.756132','东野圭吾',4,NULL,30,'一桩十九年前的悬案，牵出两个人长达十九年的命运纠葛。','9787115000007',39.50,'南海出版公司','A-07','ACTIVE','白夜行',5,'文学'),(8,'2026-11-13 09:09:52.699908','2026-11-13 09:09:52.699908','东野圭吾',5,NULL,30,'一家能够咨询烦恼的杂货店，连接起跨越时空的温暖故事。','9787115000008',39.50,'南海出版公司','A-08','ACTIVE','解忧杂货店',5,'文学'),(9,'2026-11-13 09:09:52.699908','2026-11-13 09:09:52.756132','周志明',5,NULL,30,'系统讲解 Java 虚拟机的内存模型、垃圾收集、类加载与性能调优。','9787115000009',129.00,'机械工业出版社','B-01','ACTIVE','深入理解Java虚拟机',6,'计算机'),(10,'2026-11-13 09:09:52.699908','2026-11-13 09:09:52.699908','克雷格·沃斯',5,NULL,30,'全面介绍 Spring Boot、Spring MVC、Spring Data 等核心技术的实践指南。','9787115000010',99.00,'人民邮电出版社','B-02','ACTIVE','Spring实战',5,'计算机'),(11,'2026-11-13 09:09:52.699908','2026-11-13 09:09:52.699908','托马斯·科尔曼',4,NULL,30,'计算机算法领域的经典教材，覆盖排序、图算法、动态规划等主题。','9787115000011',128.00,'机械工业出版社','B-03','ACTIVE','算法导论',4,'计算机'),(12,'2026-11-13 09:09:52.699908','2026-11-13 09:09:52.756132','詹姆斯·库罗斯',4,NULL,30,'从应用层出发，自顶向下讲解计算机网络的基本原理与协议。','9787115000012',89.00,'机械工业出版社','B-04','ACTIVE','计算机网络：自顶向下方法',5,'计算机'),(13,'2026-11-13 09:09:52.699908','2026-11-13 09:09:52.699908','王珊',7,NULL,30,'系统介绍数据库系统的基本概念、关系数据库、SQL 与事务处理。','9787115000013',42.00,'高等教育出版社','B-05','ACTIVE','数据库系统概论',7,'计算机'),(14,'2026-11-13 09:09:52.699908','2026-11-13 09:09:52.699908','罗伯特·马丁',5,NULL,30,'通过大量实例讲解如何编写可读、可维护的高质量代码。','9787115000014',59.00,'人民邮电出版社','B-06','ACTIVE','代码整洁之道',5,'计算机'),(15,'2026-11-13 09:09:52.699908','2026-11-13 09:09:52.699908','马丁·福勒',4,NULL,30,'系统整理重构手法，帮助开发者在保持行为不变的前提下改善代码结构。','9787115000015',99.00,'人民邮电出版社','B-07','ACTIVE','重构：改善既有代码的设计',4,'计算机'),(16,'2026-11-13 09:09:52.699908','2026-11-13 09:09:52.699908','布鲁斯·埃克尔',4,NULL,30,'深入讲解 Java 语言特性与面向对象设计思想的经典著作。','9787115000016',108.00,'机械工业出版社','B-08','ACTIVE','Java编程思想',4,'计算机'),(17,'2026-11-13 09:09:52.699908','2026-11-13 09:09:52.756132','黄仁宇',5,NULL,30,'以万历十五年为中心，剖析明代政治、经济与社会的深层结构。','9787115000017',32.00,'中华书局','C-01','ACTIVE','万历十五年',6,'历史'),(18,'2026-11-13 09:09:52.710438','2026-11-13 09:09:52.756132','尤瓦尔·赫拉利',6,NULL,30,'从认知革命、农业革命到科学革命，重新审视人类文明的演进。','9787115000018',68.00,'中信出版社','C-02','ACTIVE','人类简史',7,'历史'),(19,'2026-11-13 09:09:52.710438','2026-11-13 09:09:52.710438','当年明月',8,NULL,30,'以轻松幽默的笔法讲述明朝三百年历史。','9787115000019',358.00,'北京联合出版公司','C-03','ACTIVE','明朝那些事儿',8,'历史'),(20,'2026-11-13 09:09:52.710438','2026-11-13 09:09:52.710438','贾雷德·戴蒙德',4,NULL,30,'从地理与环境角度解释不同大陆人类社会发展差异。','9787115000020',69.00,'中信出版社','C-04','ACTIVE','枪炮、病菌与钢铁',4,'历史'),(21,'2026-11-13 09:09:52.710438','2026-11-13 09:09:52.710438','曼昆',5,NULL,30,'以十大经济学原理为主线，介绍微观与宏观经济学基础。','9787115000021',88.00,'北京大学出版社','D-01','ACTIVE','经济学原理',5,'经济'),(22,'2026-11-13 09:09:52.710438','2026-11-13 09:09:52.710438','亚当·斯密',3,NULL,30,'古典经济学奠基之作，系统论述分工、市场与国民财富。','9787115000022',58.00,'商务印书馆','D-02','ACTIVE','国富论',3,'经济'),(23,'2026-11-13 09:09:52.710438','2026-11-13 09:09:52.710438','瑞·达利欧',4,NULL,30,'桥水基金创始人总结的生活与工作原则。','9787115000023',98.00,'中信出版社','D-03','ACTIVE','原则',4,'经济'),(24,'2026-11-13 09:09:52.710438','2026-11-13 09:09:52.756132','史蒂芬·霍金',4,NULL,30,'从宇宙大爆炸到黑洞，带领读者探索宇宙的起源与命运。','9787115000024',45.00,'湖南科学技术出版社','E-01','ACTIVE','时间简史',5,'科学'),(25,'2026-11-13 09:09:52.710438','2026-11-13 09:09:52.710438','查尔斯·达尔文',3,NULL,30,'生物进化论奠基之作，阐述自然选择与物种演化。','9787115000025',48.00,'商务印书馆','E-02','ACTIVE','物种起源',3,'科学'),(26,'2026-11-13 09:09:52.710438','2026-11-13 09:09:52.710438','贡布里希',4,NULL,30,'以通俗流畅的语言讲述从史前到现代的艺术发展史。','9787115000026',280.00,'广西美术出版社','F-01','ACTIVE','艺术的故事',4,'艺术'),(27,'2026-11-13 09:09:52.710438','2026-11-13 09:09:52.710438','李泽厚',4,NULL,30,'从远古图腾到明清文艺，梳理中国美学的发展脉络。','9787115000027',42.00,'生活·读书·新知三联书店','F-02','ACTIVE','美的历程',4,'艺术'),(28,'2026-11-13 09:09:52.710438','2026-11-13 09:09:52.710438','乔斯坦·贾德',5,NULL,30,'以小说形式讲述西方哲学史，是哲学入门的经典读物。','9787115000028',42.00,'作家出版社','G-01','ACTIVE','苏菲的世界',5,'哲学'),(29,'2026-11-13 09:09:52.710438','2026-11-13 09:09:52.710438','陈琦',4,NULL,30,'介绍学习理论、学生心理发展与教学设计的心理学基础。','9787115000029',52.00,'高等教育出版社','H-01','ACTIVE','教育心理学',4,'教育'),(30,'2026-11-13 09:09:52.710438','2026-11-13 09:09:52.710438','毛姆',5,NULL,30,'以画家高更为原型，探讨理想与现实、艺术与生活的冲突。','9787115000030',39.00,'上海译文出版社','A-09','ACTIVE','月亮与六便士',5,'文学');
/*!40000 ALTER TABLE `book` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `book_attachment`
--

DROP TABLE IF EXISTS `book_attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `book_attachment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `book_id` bigint NOT NULL,
  `content_type` varchar(100) DEFAULT NULL,
  `file_name` varchar(200) NOT NULL,
  `file_size` bigint DEFAULT NULL,
  `image_type` varchar(20) NOT NULL,
  `original_name` varchar(255) DEFAULT NULL,
  `sort_order` int DEFAULT NULL,
  `url` varchar(500) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_attachment_book` (`book_id`),
  KEY `idx_attachment_type` (`image_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `book_attachment`
--

LOCK TABLES `book_attachment` WRITE;
/*!40000 ALTER TABLE `book_attachment` DISABLE KEYS */;
/*!40000 ALTER TABLE `book_attachment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `loan`
--

DROP TABLE IF EXISTS `loan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `loan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `book_id` bigint NOT NULL,
  `borrow_date` datetime(6) NOT NULL,
  `due_date` datetime(6) NOT NULL,
  `operator_id` bigint DEFAULT NULL,
  `remark` varchar(200) DEFAULT NULL,
  `return_date` datetime(6) DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_loan_user` (`user_id`),
  KEY `idx_loan_book` (`book_id`),
  KEY `idx_loan_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loan`
--

LOCK TABLES `loan` WRITE;
/*!40000 ALTER TABLE `loan` DISABLE KEYS */;
INSERT INTO `loan` VALUES (1,'2026-11-13 09:09:52.738777','2026-11-13 09:09:52.738777',1,'2026-10-26 09:09:52.738777','2026-11-25 09:09:52.738777',1,NULL,NULL,'BORROWED',5),(2,'2026-11-13 09:09:52.738777','2026-11-13 09:09:52.738777',3,'2026-09-28 09:09:52.738777','2026-10-28 09:09:52.738777',1,NULL,NULL,'BORROWED',4),(3,'2026-11-13 09:09:52.738777','2026-11-13 09:09:52.738777',2,'2026-11-05 09:09:52.738777','2026-12-05 09:09:52.738777',1,NULL,NULL,'BORROWED',3),(4,'2026-11-13 09:09:52.738777','2026-11-13 09:09:52.738777',9,'2026-10-02 09:09:52.738777','2026-11-01 09:09:52.738777',1,NULL,NULL,'BORROWED',3),(5,'2026-11-13 09:09:52.738777','2026-11-13 09:09:52.738777',4,'2026-11-01 09:09:52.738777','2026-12-01 09:09:52.738777',1,NULL,NULL,'BORROWED',7),(6,'2026-11-13 09:09:52.745449','2026-11-13 09:09:52.745449',5,'2026-10-24 09:09:52.745449','2026-11-23 09:09:52.745449',1,NULL,'2026-11-03 09:09:52.745449','RETURNED',8),(7,'2026-11-13 09:09:52.745449','2026-11-13 09:09:52.745449',17,'2026-10-19 09:09:52.745449','2026-11-18 09:09:52.745449',1,NULL,NULL,'BORROWED',9),(8,'2026-11-13 09:09:52.745449','2026-11-13 09:09:52.745449',18,'2026-10-28 09:09:52.745449','2026-11-27 09:09:52.745449',1,NULL,NULL,'BORROWED',14),(9,'2026-11-13 09:09:52.745449','2026-11-13 09:09:52.745449',6,'2026-10-09 09:09:52.745449','2026-11-08 09:09:52.745449',1,NULL,'2026-11-08 09:09:52.745449','RETURNED',10),(10,'2026-11-13 09:09:52.745449','2026-11-13 09:09:52.745449',12,'2026-11-04 09:09:52.745449','2026-12-04 09:09:52.745449',1,NULL,NULL,'BORROWED',11),(11,'2026-11-13 09:09:52.745449','2026-11-13 09:09:52.745449',24,'2026-10-22 09:09:52.745449','2026-11-21 09:09:52.745449',1,NULL,NULL,'BORROWED',12),(12,'2026-11-13 09:09:52.745449','2026-11-13 09:09:52.745449',25,'2026-10-14 09:09:52.745449','2026-11-13 09:09:52.745449',1,NULL,'2026-11-11 09:09:52.745449','RETURNED',13),(13,'2026-11-13 09:09:52.745449','2026-11-13 09:09:52.745449',7,'2026-10-30 09:09:52.745449','2026-11-29 09:09:52.745449',1,NULL,NULL,'BORROWED',6);
/*!40000 ALTER TABLE `loan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `content` varchar(500) NOT NULL,
  `link` varchar(200) DEFAULT NULL,
  `read_flag` bit(1) NOT NULL,
  `title` varchar(100) NOT NULL,
  `type` varchar(20) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_notification_user` (`user_id`),
  KEY `idx_notification_read` (`read_flag`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
INSERT INTO `notification` VALUES (1,'2026-11-13 09:09:52.745449','2026-11-13 09:09:52.745449','欢迎回来！您可以在这里查询馆藏、借阅图书、预约缺藏书目，并实时接收到书通知。','/books',_binary '\0','欢迎使用智慧图书馆','SYSTEM',3),(2,'2026-11-13 09:09:52.745449','2026-11-13 09:09:52.745449','当预约的图书出现可借副本时，系统会通过右上角消息中心实时提醒您，请留意查收。','/my-reservations',_binary '\0','预约到书提醒','SYSTEM',3);
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `operation_log`
--

DROP TABLE IF EXISTS `operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `action` varchar(50) DEFAULT NULL,
  `description` varchar(200) DEFAULT NULL,
  `duration_ms` bigint DEFAULT NULL,
  `error_msg` varchar(500) DEFAULT NULL,
  `ip` varchar(60) DEFAULT NULL,
  `method` varchar(10) DEFAULT NULL,
  `module` varchar(30) DEFAULT NULL,
  `params` varchar(2000) DEFAULT NULL,
  `success` bit(1) NOT NULL,
  `uri` varchar(255) DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  `user_name` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_log_user` (`user_id`),
  KEY `idx_log_module` (`module`),
  KEY `idx_log_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `operation_log`
--

LOCK TABLES `operation_log` WRITE;
/*!40000 ALTER TABLE `operation_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `operation_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reservation`
--

DROP TABLE IF EXISTS `reservation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `book_id` bigint NOT NULL,
  `expires_at` datetime(6) DEFAULT NULL,
  `notified_at` datetime(6) DEFAULT NULL,
  `remark` varchar(200) DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_reservation_user` (`user_id`),
  KEY `idx_reservation_book` (`book_id`),
  KEY `idx_reservation_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservation`
--

LOCK TABLES `reservation` WRITE;
/*!40000 ALTER TABLE `reservation` DISABLE KEYS */;
INSERT INTO `reservation` VALUES (1,'2026-11-13 09:09:52.745449','2026-11-13 09:09:52.745449',3,NULL,NULL,NULL,'WAITING',6);
/*!40000 ALTER TABLE `reservation` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `area` varchar(60) DEFAULT NULL,
  `college` varchar(60) DEFAULT NULL,
  `duty` varchar(40) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `entry_time` date DEFAULT NULL,
  `grade` varchar(20) DEFAULT NULL,
  `login_time` datetime(6) DEFAULT NULL,
  `major` varchar(60) DEFAULT NULL,
  `name` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `permission_level` int DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `role` varchar(20) NOT NULL,
  `status` varchar(20) NOT NULL,
  `title` varchar(40) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKiic0kskryiymn15fg9bqpmw22` (`name`),
  UNIQUE KEY `UKpulp17fvich5aby4m0kc820h6` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'2026-11-13 09:09:51.968139','2026-11-13 09:09:51.968139',NULL,NULL,'系统管理','admin@library.edu.cn','2020-09-01',NULL,'2026-11-12 09:09:51.965135','系统管理员','admin','$2a$10$AHV4aqHMf2linbOIhFo89ei0F9VsRUAtkS8cKDJu67wJmTv2O0CpK',9,'13800000001','ADMIN','ACTIVE',NULL),(2,'2026-11-13 09:09:52.033468','2026-11-13 09:09:52.033468','一楼借阅区',NULL,'借阅服务',NULL,'2021-03-01',NULL,'2026-11-12 09:09:52.033468','图书馆学','operator','$2a$10$E1wSpH.3F1QWDem6uDgq7eeE0dFXRc9RjYIu54pzVUsjbXsWzfN86',5,'13800000002','OPERATOR','ACTIVE',NULL),(3,'2026-11-13 09:09:52.080490','2026-11-13 09:09:52.080490',NULL,'信息工程学院',NULL,'student1@library.edu.cn',NULL,'2024级','2026-11-12 09:09:52.080490','计算机科学与技术','student1','$2a$10$reaqgA.92Ar3VWfYeNIqNegKSz/Exhu7xU8Zrlrke/bJR2Fnxzg1q',NULL,'13800000003','STUDENT','ACTIVE',NULL),(4,'2026-11-13 09:09:52.126208','2026-11-13 09:09:52.126208',NULL,'信息工程学院',NULL,NULL,NULL,NULL,'2026-11-12 09:09:52.126208','软件工程','teacher','$2a$10$7exzaxJ/xTmZWDN4Y9fxj.yx8nahaK6oRfe0nGUfIRi4Wq77Y.N8e',NULL,'13800000004','TEACHER','ACTIVE','副教授'),(5,'2026-11-13 09:09:52.172583','2026-11-13 09:09:52.172583',NULL,'信息工程学院',NULL,NULL,NULL,'2023级','2026-11-12 09:09:52.172583','软件工程','student2','$2a$10$73axH9y3OO8kNVnuCouJAO5MFHUkJGWVIbVlFTzvTqr/ZWoGczO0q',NULL,'13800000005','STUDENT','ACTIVE',NULL),(6,'2026-11-13 09:09:52.220687','2026-11-13 09:09:52.220687',NULL,'文学院',NULL,NULL,NULL,'2024级','2026-11-12 09:09:52.220687','汉语言文学','student3','$2a$10$2Gn.6obTPSk4tmVRnDk3iuusxjXaYtGz185QvOoiwZXOxuahy/Wye',NULL,'13800000006','STUDENT','ACTIVE',NULL),(7,'2026-11-13 09:09:52.268264','2026-11-13 09:09:52.268264',NULL,'经济管理学院',NULL,NULL,NULL,'2022级','2026-11-12 09:09:52.268264','会计学','student4','$2a$10$9YaHVZFag2wyAYz.0aQ9qeCS60.gvWYN3q/tVNAje3uicKbpfONHK',NULL,'13800000007','STUDENT','ACTIVE',NULL),(8,'2026-11-13 09:09:52.311665','2026-11-13 09:09:52.311665',NULL,'信息工程学院',NULL,NULL,NULL,'2023级','2026-11-12 09:09:52.311665','人工智能','student5','$2a$10$7sqLbyc6yTayNS5lZTbsn.U3UwbaD.GlZnfd3sd2EA0zcflHTRGeO',NULL,'13800000008','STUDENT','ACTIVE',NULL),(9,'2026-11-13 09:09:52.362673','2026-11-13 09:09:52.362673',NULL,'艺术设计学院',NULL,NULL,NULL,'2024级','2026-11-12 09:09:52.362673','视觉传达设计','student6','$2a$10$NwtCBq9f9FAq5Qy1.bmRu.Cs5yuRR3stFqD7zddVHlAd5WlaW8MZq',NULL,'13800000009','STUDENT','ACTIVE',NULL),(10,'2026-11-13 09:09:52.410141','2026-11-13 09:09:52.410141',NULL,'人文学院',NULL,NULL,NULL,'2022级','2026-11-12 09:09:52.410141','历史学','student7','$2a$10$g0PlmbvOegqIgJsMcTvt0OuvAHxKT3iDnvJNpDywNFnEtImCnZGRy',NULL,'13800000010','STUDENT','ACTIVE',NULL),(11,'2026-11-13 09:09:52.449478','2026-11-13 09:09:52.449478',NULL,'信息工程学院',NULL,NULL,NULL,'2023级','2026-11-12 09:09:52.449478','数据科学与大数据技术','student8','$2a$10$Ayu2Qh.KqGXWQizyFoA1V.R1MyZDguK5DXIuEsN2xELKtLlDpcDYS',NULL,'13800000011','STUDENT','ACTIVE',NULL),(12,'2026-11-13 09:09:52.503677','2026-11-13 09:09:52.503677',NULL,'外国语学院',NULL,NULL,NULL,'2024级','2026-11-12 09:09:52.503677','英语','student9','$2a$10$xb/OQpaaDC9w/w.wsAbLq.j.m31pxW13eikbRsBc39ZL.bwHryJei',NULL,'13800000012','STUDENT','ACTIVE',NULL),(13,'2026-11-13 09:09:52.544518','2026-11-13 09:09:52.544518',NULL,'经济管理学院',NULL,NULL,NULL,'2022级','2026-11-12 09:09:52.544518','工商管理','student10','$2a$10$zpk4cFFpM80eyunJSF8C2uLdfn1N8Uvfy.2od3lH7CrjswzOt9P9G',NULL,'13800000013','STUDENT','ACTIVE',NULL),(14,'2026-11-13 09:09:52.591675','2026-11-13 09:09:52.591675',NULL,'文学院',NULL,NULL,NULL,NULL,'2026-11-12 09:09:52.591675','中国古代文学','teacher2','$2a$10$RRDqmapmyb62Ojnl/id.FOVbsxS8rms0lHz7KzZn7H8zKvGTUSfvi',NULL,'13800000014','TEACHER','ACTIVE','讲师'),(15,'2026-11-13 09:09:52.641601','2026-11-13 09:09:52.641601',NULL,'信息工程学院',NULL,NULL,NULL,NULL,'2026-11-12 09:09:52.641601','计算机应用技术','teacher3','$2a$10$6tkMfoRep3eN7TD547cwLu76CIzdvunS9PdgvGLUgbXv8O6IVnOZO',NULL,'13800000015','TEACHER','ACTIVE','教授'),(16,'2026-11-13 09:09:52.685193','2026-11-13 09:09:52.685193','二楼书库',NULL,'图书采编',NULL,NULL,NULL,'2026-11-12 09:09:52.685193','信息管理与信息系统','operator2','$2a$10$2EOGkrpVDKiUFYF5oYBF1O.efI.5cjrUipPcL44V9e5EpR406NQrK',4,'13800000016','OPERATOR','ACTIVE',NULL);
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-13  9:10:33
