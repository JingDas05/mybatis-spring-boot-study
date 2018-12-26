/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50721
Source Host           : localhost:3306
Source Database       : mybatis3

Target Server Type    : MYSQL
Target Server Version : 50721
File Encoding         : 65001

Date: 2018-12-11 16:09:39
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
INSERT INTO `t_city_1` VALUES ('5', '5', '城市5', '省份5', '中国');
INSERT INTO `t_city_1` VALUES ('11', '11', '城市11', '省份11', '中国');
INSERT INTO `t_city_1` VALUES ('17', '17', '城市17', '省份17', '中国');
INSERT INTO `t_city_1` VALUES ('23', '23', '城市23', '省份23', '中国');
INSERT INTO `t_city_1` VALUES ('29', '29', '城市29', '省份29', '中国');
INSERT INTO `t_city_1` VALUES ('35', '35', '城市35', '省份35', '中国');
INSERT INTO `t_city_1` VALUES ('41', '41', '城市41', '省份41', '中国');
INSERT INTO `t_city_1` VALUES ('47', '47', '城市47', '省份47', '中国');
