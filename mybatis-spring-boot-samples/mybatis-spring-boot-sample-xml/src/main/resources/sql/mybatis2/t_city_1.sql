/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50721
Source Host           : localhost:3306
Source Database       : mybatis2

Target Server Type    : MYSQL
Target Server Version : 50721
File Encoding         : 65001

Date: 2018-12-11 16:08:48
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
INSERT INTO `t_city_1` VALUES ('1', '1', '城市1', '省份1', '中国');
INSERT INTO `t_city_1` VALUES ('7', '7', '城市7', '省份7', '中国');
INSERT INTO `t_city_1` VALUES ('13', '13', '城市13', '省份13', '中国');
INSERT INTO `t_city_1` VALUES ('19', '19', '城市19', '省份19', '中国');
INSERT INTO `t_city_1` VALUES ('25', '25', '城市25', '省份25', '中国');
INSERT INTO `t_city_1` VALUES ('31', '31', '城市31', '省份31', '中国');
INSERT INTO `t_city_1` VALUES ('37', '37', '城市37', '省份37', '中国');
INSERT INTO `t_city_1` VALUES ('43', '43', '城市43', '省份43', '中国');
INSERT INTO `t_city_1` VALUES ('49', '49', '城市49', '省份49', '中国');
