-- MySQL dump 10.16  Distrib 10.2.13-MariaDB, for osx10.13 (x86_64)
--
-- Host: localhost    Database: joker
-- ------------------------------------------------------
-- Server version	10.2.13-MariaDB-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `jk_gateway_app`
--

DROP TABLE IF EXISTS `jk_gateway_app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `jk_gateway_app` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) DEFAULT NULL COMMENT '网关应用名字',
  `timestamp` bigint(20) DEFAULT NULL COMMENT '创建时间戳',
  `u_timestamp` bigint(20) DEFAULT NULL COMMENT '更新的时间戳',
  `metas` text DEFAULT NULL COMMENT '项目配置信息 json 格式',
  `host` varchar(64) DEFAULT NULL,
  `port` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `jk_gateway_project_id_uindex` (`id`),
  UNIQUE KEY `jk_gateway_app_name_uindex` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COMMENT='网关中创建的应用';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `jk_gateway_app`
--

LOCK TABLES `jk_gateway_app` WRITE;
/*!40000 ALTER TABLE `jk_gateway_app` DISABLE KEYS */;
INSERT INTO `jk_gateway_app` VALUES (5,'测定',1536471551444,1536471758949,'{\"apis\":[{\"apiOption\":{\"apiOptionUrls\":[{\"url\":\"http://www.baidu.com\"}]},\"apiType\":\"REDIRECT\",\"name\":\"测试api\",\"on\":true,\"path\":\"/baidu\",\"remark\":\"跳转到百度首页\",\"supportContentType\":[\"\"],\"supportMethods\":[]}],\"createTime\":0,\"host\":\"localhost\",\"name\":\"测定\",\"on\":true,\"port\":8889,\"remark\":\"测试项目\",\"updateTime\":1536471758949}','localhost',8889);
/*!40000 ALTER TABLE `jk_gateway_app` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-09-09 13:52:15
