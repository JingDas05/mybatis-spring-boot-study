/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50721
Source Host           : localhost:3306
Source Database       : mybatis2

Target Server Type    : MYSQL
Target Server Version : 50721
File Encoding         : 65001

Date: 2018-12-11 16:08:39
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
INSERT INTO `t_city_0` VALUES ('4', '4', '城市4', '省份4', '中国');
INSERT INTO `t_city_0` VALUES ('10', '10', '城市10', '省份10', '中国');
INSERT INTO `t_city_0` VALUES ('16', '16', '城市16', '省份16', '中国');
INSERT INTO `t_city_0` VALUES ('22', '22', '城市22', '省份22', '中国');
INSERT INTO `t_city_0` VALUES ('28', '28', '城市28', '省份28', '中国');
INSERT INTO `t_city_0` VALUES ('34', '34', '城市34', '省份34', '中国');
INSERT INTO `t_city_0` VALUES ('40', '40', '城市40', '省份40', '中国');
INSERT INTO `t_city_0` VALUES ('46', '46', '城市46', '省份46', '中国');
