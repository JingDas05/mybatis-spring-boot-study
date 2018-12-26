/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50721
Source Host           : localhost:3306
Source Database       : mybatis3

Target Server Type    : MYSQL
Target Server Version : 50721
File Encoding         : 65001

Date: 2018-12-11 16:09:34
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for t_city_0
-- ----------------------------
DROP TABLE IF EXISTS `t_city_0`;
CREATE TABLE `t_city_0` (
  `id` int(10) unsigned NOT NULL COMMENT '主键Id',
  `sub_id` int(10) unsigned NOT NULL COMMENT '子Id',
  `name` varchar(128) DEFAULT NULL COMMENT '名字',
  `state` varchar(128) DEFAULT NULL COMMENT '州',
  `country` varchar(128) DEFAULT NULL COMMENT '城市',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='城市表';

-- ----------------------------
-- Records of t_city_0
-- ----------------------------
INSERT INTO `t_city_0` VALUES ('2', '2', '城市2', '省份2', '中国');
INSERT INTO `t_city_0` VALUES ('8', '8', '城市8', '省份8', '中国');
INSERT INTO `t_city_0` VALUES ('14', '14', '城市14', '省份14', '中国');
INSERT INTO `t_city_0` VALUES ('20', '20', '城市20', '省份20', '中国');
INSERT INTO `t_city_0` VALUES ('26', '26', '城市26', '省份26', '中国');
INSERT INTO `t_city_0` VALUES ('32', '32', '城市32', '省份32', '中国');
INSERT INTO `t_city_0` VALUES ('38', '38', '城市38', '省份38', '中国');
INSERT INTO `t_city_0` VALUES ('44', '44', '城市44', '省份44', '中国');
