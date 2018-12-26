/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50721
Source Host           : localhost:3306
Source Database       : mybatis1

Target Server Type    : MYSQL
Target Server Version : 50721
File Encoding         : 65001

Date: 2018-12-11 16:06:50
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
INSERT INTO `t_city_0` VALUES ('0', '0', '城市0', '省份0', '中国');
INSERT INTO `t_city_0` VALUES ('6', '6', '城市6', '省份6', '中国');
INSERT INTO `t_city_0` VALUES ('12', '12', '城市12', '省份12', '中国');
INSERT INTO `t_city_0` VALUES ('18', '18', '城市18', '省份18', '中国');
INSERT INTO `t_city_0` VALUES ('24', '24', '城市24', '省份24', '中国');
INSERT INTO `t_city_0` VALUES ('30', '30', '城市30', '省份30', '中国');
INSERT INTO `t_city_0` VALUES ('36', '36', '城市36', '省份36', '中国');
INSERT INTO `t_city_0` VALUES ('42', '42', '城市42', '省份42', '中国');
INSERT INTO `t_city_0` VALUES ('48', '48', '城市48', '省份48', '中国');
