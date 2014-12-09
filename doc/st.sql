/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50611
Source Host           : localhost:3306
Source Database       : st

Target Server Type    : MYSQL
Target Server Version : 50611
File Encoding         : 65001

Date: 2014-12-09 16:30:39
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `tab_charge_detail`
-- ----------------------------
DROP TABLE IF EXISTS `tab_charge_detail`;
CREATE TABLE `tab_charge_detail` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `Status` tinyint(4) DEFAULT '1' COMMENT '交易状态：1:成功  3:失败',
  `CityCardNo` varchar(30) DEFAULT '' COMMENT '市民卡号',
  `CardType` tinyint(255) DEFAULT NULL COMMENT '卡片类型',
  `BankCardNo` varchar(30) DEFAULT '' COMMENT '充值时采用的银行卡卡号或者充值卡卡号',
  `ChargeTime` datetime DEFAULT NULL COMMENT '充值时间',
  `ChargeType` tinyint(255) DEFAULT '0' COMMENT '充值类型  0：现金  1：银联卡 2：充值卡  3：账户宝',
  `ChargeAmount` int(255) DEFAULT NULL COMMENT '充值金额 单位：分',
  `BalanceBeforeCharge` int(255) DEFAULT NULL COMMENT '充值前余额',
  `TAC` varchar(20) DEFAULT '' COMMENT '充值过程中生存的tac',
  `ASN` varchar(20) DEFAULT '' COMMENT 'IC卡应用序列号',
  `TSN` varchar(20) DEFAULT '' COMMENT 'IC卡交易序列号',
  `ChargeSNo` bigint(12) DEFAULT NULL COMMENT '充值交易流水号',
  `TerminalID` int(20) DEFAULT NULL COMMENT '充值所在终端',
  `POSID` varchar(20) DEFAULT '' COMMENT 'posid',
  `SAMID` varchar(20) DEFAULT '' COMMENT 'samid',
  `AgencyNo` varchar(20) DEFAULT '' COMMENT '充值代理机构代码',
  `SysTime` datetime DEFAULT NULL COMMENT '数据插入时间',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=108 DEFAULT CHARSET=utf8 COMMENT='充值明细';


-- ----------------------------
-- Table structure for `tab_refund`
-- ----------------------------
DROP TABLE IF EXISTS `tab_refund`;
CREATE TABLE `tab_refund` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `CardNo` varchar(30) DEFAULT NULL COMMENT '卡号',
  `Amount` int(255) DEFAULT NULL COMMENT '退款金额',
  `RefundTime` datetime DEFAULT NULL COMMENT '退款时间，由客户端提供',
  `Status` tinyint(255) DEFAULT NULL COMMENT '状态  0：新建未退回  1：退回',
  `InsertTime` datetime DEFAULT NULL,
  `TerminalID` int(11) DEFAULT NULL COMMENT '终端编号',
  `ChargeType` tinyint(4) DEFAULT NULL COMMENT '充值类型',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8 COMMENT='退款明细';

-- ----------------------------
-- Table structure for `tab_terminal`
-- ----------------------------
DROP TABLE IF EXISTS `tab_terminal`;
CREATE TABLE `tab_terminal` (
  `id` int(19) NOT NULL COMMENT '终端ID',
  `typeId` smallint(6) DEFAULT '0' COMMENT '终端类型，默认0',
  `position` varchar(255) DEFAULT NULL COMMENT '终端投放地址',
  `enabled` tinyint(2) DEFAULT '0' COMMENT '启用状态 0：启用 1：停用',
  `memo` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tab_terminal
-- ----------------------------
INSERT INTO `tab_terminal` VALUES ('123', '1', 'a', '0', 'sfd');

-- ----------------------------
-- Table structure for `tab_terminal_status`
-- ----------------------------
DROP TABLE IF EXISTS `tab_terminal_status`;
CREATE TABLE `tab_terminal_status` (
  `TERMINALID` int(19) NOT NULL COMMENT '终端ID  主键',
  `OnlineStatus` tinyint(2) DEFAULT NULL COMMENT '设备在线状态  0：未知  1：在线  2：离线',
  `LastOnlineTime` datetime DEFAULT NULL COMMENT '最近一次的在线时间',
  `M1STATUS` tinyint(2) DEFAULT '0' COMMENT '市民卡读写模块 (0:未知 1:正常 2:故障   下同)',
  `M2STATUS` tinyint(2) DEFAULT '0' COMMENT '现金模块',
  `M3STATUS` tinyint(2) DEFAULT '0' COMMENT '银联模块',
  `M4STATUS` tinyint(2) DEFAULT '0' COMMENT '打印模块',
  `M5STATUS` tinyint(2) DEFAULT '0' COMMENT '身份证读取模块',
  `M6STATUS` tinyint(2) DEFAULT '0' COMMENT '密码键盘模块',
  `M7STATUS` tinyint(2) DEFAULT '0',
  `M8STATUS` tinyint(2) DEFAULT '0',
  `CashBoxAmount` int(11) DEFAULT '0' COMMENT '钱箱现金',
  PRIMARY KEY (`TERMINALID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for `tab_terminal_type`
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
-- Table structure for `tab_users`
-- ----------------------------
DROP TABLE IF EXISTS `tab_users`;
CREATE TABLE `tab_users` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `name` varchar(50) NOT NULL COMMENT '用户名',
  `pass` varchar(50) DEFAULT NULL COMMENT '用户密码  ',
  `memo` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='用户';

-- ----------------------------
-- Records of tab_users
-- ----------------------------
INSERT INTO `tab_users` VALUES ('1', 'admin', 'admin', 'admin');

-- ----------------------------
-- Table structure for `tab_withdraw_detail`
-- ----------------------------
DROP TABLE IF EXISTS `tab_withdraw_detail`;
CREATE TABLE `tab_withdraw_detail` (
  `TerminalId` int(11) DEFAULT '0' COMMENT '终端ID',
  `WithdrawAmount` int(255) DEFAULT '0' COMMENT '提款金额',
  `OperTime` datetime DEFAULT NULL COMMENT '提款操作时间',
  `OperUserId` int(11) DEFAULT '0' COMMENT '操作人员编号',
  `InsertTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '数据插入时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='提款操作明细记录';

-- ----------------------------
-- Records of tab_withdraw_detail
-- ----------------------------
