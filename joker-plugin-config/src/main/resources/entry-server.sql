

# mysql 
DROP TABLE IF EXISTS `vertx_project_server`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vertx_project_server` (
  `version` varchar(64) DEFAULT NULL,
  `artifact_id` varchar(128) DEFAULT NULL,
  `group_id` varchar(128) DEFAULT NULL,
  `data` longtext DEFAULT NULL,
  `uuid` varchar(128) DEFAULT NULL,
  `upload_time` datetime DEFAULT NULL,
  `name` varchar(128) DEFAULT 'NULL',
  `enter` varchar(128) DEFAULT NULL COMMENT 'className'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;



# hsqldb
DROP TABLE IF EXISTS joker_plugin_config
CREATE TABLE joker_plugin_config (uuid varchar(128),comment varchar(128),endpoint varchar(128),data LONGVARCHAR,update_time datetime,create_time datetime)