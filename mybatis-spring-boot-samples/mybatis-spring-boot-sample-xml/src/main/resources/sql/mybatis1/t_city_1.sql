/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50721
Source Host           : localhost:3306
Source Database       : mybatis1

Target Server Type    : MYSQL
Target Server Version : 50721
File Encoding         : 65001

Date: 2018-12-11 16:06:55
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for t_city_1
-- ----------------------------
DROP TABLE IF EXISTS `t_city_1`;
CREATE TABLE `t_city_1` (
  `id` int(10) unsigned NOT NULL COMMENT '主键Id',
  `sub_id` int(10) unsigned NOT NULL COMMENT '子Id',
  `name` varchar(128) DEFAULT NULL COMMENT '名字',
  `state` varchar(128) DEFAULT NULL COMMENT '州',
  `country` varchar(128) DEFAULT NULL COMMENT '城市',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='城市表';

-- ----------------------------
-- Records of t_city_1
-- ----------------------------
INSERT INTO `t_city_1` VALUES ('3', '3', '城市3', '省份3', '中国');
INSERT INTO `t_city_1` VALUES ('9', '9', '城市9', '省份9', '中国');
INSERT INTO `t_city_1` VALUES ('15', '15', '城市15', '省份15', '中国');
INSERT INTO `t_city_1` VALUES ('21', '21', '城市21', '省份21', '中国');
INSERT INTO `t_city_1` VALUES ('27', '27', '城市27', '省份27', '中国');
INSERT INTO `t_city_1` VALUES ('33', '33', '城市33', '省份33', '中国');
INSERT INTO `t_city_1` VALUES ('39', '39', '城市39', '省份39', '中国');
INSERT INTO `t_city_1` VALUES ('45', '45', '城市45', '省份45', '中国');
