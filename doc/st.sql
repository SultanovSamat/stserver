/*
Navicat MySQL Data Transfer

Source Server         : local
Source Server Version : 50525
Source Host           : localhost:3306
Source Database       : st

Target Server Type    : MYSQL
Target Server Version : 50525
File Encoding         : 65001

Date: 2014-07-18 16:30:33
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for tab_module
-- ----------------------------
DROP TABLE IF EXISTS `tab_module`;
CREATE TABLE `tab_module` (
  `id` smallint(5) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tab_module
-- ----------------------------
INSERT INTO `tab_module` VALUES ('1', 'a');
INSERT INTO `tab_module` VALUES ('2', 'b');

-- ----------------------------
-- Table structure for tab_terminal
-- ----------------------------
DROP TABLE IF EXISTS `tab_terminal`;
CREATE TABLE `tab_terminal` (
  `id` int(10) NOT NULL,
  `typeId` smallint(6) DEFAULT NULL COMMENT '终端类型，默认0',
  `position` varchar(255) DEFAULT NULL COMMENT '终端投放地址',
  `enabled` tinyint(2) DEFAULT '0' COMMENT '启用状态 0：启用 1：停用',
  `memo` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tab_terminal
-- ----------------------------
INSERT INTO `tab_terminal` VALUES ('12345678', '1', 'abcd', '0', 'aaa');

-- ----------------------------
-- Table structure for tab_terminal_module_status
-- ----------------------------
DROP TABLE IF EXISTS `tab_terminal_module_status`;
CREATE TABLE `tab_terminal_module_status` (
  `TERMINALID` int(10) DEFAULT NULL,
  `MODULEID` smallint(5) DEFAULT NULL,
  `STATUS` tinyint(2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tab_terminal_module_status
-- ----------------------------
INSERT INTO `tab_terminal_module_status` VALUES ('12345678', '1', '1');
INSERT INTO `tab_terminal_module_status` VALUES ('12345678', '2', '1');

-- ----------------------------
-- Table structure for tab_terminal_status
-- ----------------------------
DROP TABLE IF EXISTS `tab_terminal_status`;
CREATE TABLE `tab_terminal_status` (
  `TERMINALID` int(255) NOT NULL COMMENT '终端ID  主键',
  `OnlineStatus` tinyint(2) DEFAULT NULL COMMENT '设备在线状态  0：未知  1：在线  2：离线',
  `LastOnlineTime` datetime DEFAULT NULL COMMENT '最近一次的在线时间',
  `M1STATUS` tinyint(2) DEFAULT NULL,
  `M2STATUS` tinyint(2) DEFAULT NULL,
  `M3STATUS` tinyint(2) DEFAULT NULL,
  `M4STATUS` tinyint(2) DEFAULT NULL,
  `M5STATUS` tinyint(2) DEFAULT NULL,
  `M6STATUS` tinyint(2) DEFAULT NULL,
  `M7STATUS` tinyint(2) DEFAULT NULL,
  `M8STATUS` tinyint(2) DEFAULT NULL,
  PRIMARY KEY (`TERMINALID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tab_terminal_status
-- ----------------------------
INSERT INTO `tab_terminal_status` VALUES ('12345678', '1', '2014-07-18 15:27:32', '1', '2', '3', '3', '1', '2', '1', '0');

-- ----------------------------
-- Table structure for tab_terminal_type
-- ----------------------------
DROP TABLE IF EXISTS `tab_terminal_type`;
CREATE TABLE `tab_terminal_type` (
  `id` int(11) NOT NULL COMMENT '终端类型ID',
  `name` varchar(255) DEFAULT NULL COMMENT '类型名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='终端类型';

-- ----------------------------
-- Records of tab_terminal_type
-- ----------------------------

-- ----------------------------
-- Table structure for tab_users
-- ----------------------------
DROP TABLE IF EXISTS `tab_users`;
CREATE TABLE `tab_users` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `name` varchar(50) NOT NULL COMMENT '用户名',
  `pass` varchar(50) DEFAULT NULL COMMENT '用户密码',
  `memo` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tab_users
-- ----------------------------
INSERT INTO `tab_users` VALUES ('1', 'admin', 'admin', 'admin');
