/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50721
Source Host           : localhost:3306
Source Database       : mybatis1

Target Server Type    : MYSQL
Target Server Version : 50721
File Encoding         : 65001

Date: 2018-12-11 16:06:01
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for t_city
-- ----------------------------
DROP TABLE IF EXISTS `t_city`;
CREATE TABLE `t_city` (
  `id` int(10) unsigned NOT NULL COMMENT '主键Id',
  `sub_id` int(10) unsigned NOT NULL COMMENT '子Id',
  `name` varchar(128) DEFAULT NULL COMMENT '名字',
  `state` varchar(128) DEFAULT NULL COMMENT '州',
  `country` varchar(128) DEFAULT NULL COMMENT '城市',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='城市表';

-- ----------------------------
-- Records of t_city
-- ----------------------------
INSERT INTO `t_city` VALUES ('0', '0', '城市0', '省份0', '中国');
INSERT INTO `t_city` VALUES ('3', '3', '城市3', '省份3', '中国');
INSERT INTO `t_city` VALUES ('6', '6', '城市6', '省份6', '中国');
INSERT INTO `t_city` VALUES ('9', '9', '城市9', '省份9', '中国');
INSERT INTO `t_city` VALUES ('12', '12', '城市12', '省份12', '中国');
INSERT INTO `t_city` VALUES ('15', '15', '城市15', '省份15', '中国');
INSERT INTO `t_city` VALUES ('18', '18', '城市18', '省份18', '中国');
INSERT INTO `t_city` VALUES ('21', '21', '城市21', '省份21', '中国');
INSERT INTO `t_city` VALUES ('24', '24', '城市24', '省份24', '中国');
INSERT INTO `t_city` VALUES ('27', '27', '城市27', '省份27', '中国');
INSERT INTO `t_city` VALUES ('30', '30', '城市30', '省份30', '中国');
INSERT INTO `t_city` VALUES ('33', '33', '城市33', '省份33', '中国');
INSERT INTO `t_city` VALUES ('36', '36', '城市36', '省份36', '中国');
INSERT INTO `t_city` VALUES ('39', '39', '城市39', '省份39', '中国');
INSERT INTO `t_city` VALUES ('42', '42', '城市42', '省份42', '中国');
INSERT INTO `t_city` VALUES ('45', '45', '城市45', '省份45', '中国');
INSERT INTO `t_city` VALUES ('48', '48', '城市48', '省份48', '中国');
