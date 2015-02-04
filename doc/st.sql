/*
Navicat MySQL Data Transfer

Source Server         : local
Source Server Version : 50525
Source Host           : localhost:3306
Source Database       : st

Target Server Type    : MYSQL
Target Server Version : 50525
File Encoding         : 65001

Date: 2015-02-04 17:08:47
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for tab_charge_detail
-- ----------------------------
DROP TABLE IF EXISTS `tab_charge_detail`;
CREATE TABLE `tab_charge_detail` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `Status` tinyint(4) DEFAULT '1' COMMENT 'äº¤æ˜“çŠ¶æ€ 1ï¼šæˆåŠŸã€2ï¼šä¸­é—´çŠ¶æ€ 3ï¼šå¤±è´¥',
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
  `POSID` varchar(20) DEFAULT '' COMMENT 'posÃ§Â¼â€“Ã¥ÂÂ·',
  `SAMID` varchar(20) DEFAULT '' COMMENT 'samÃ§Â¼â€“Ã¥ÂÂ·',
  `AgencyNo` varchar(20) DEFAULT '' COMMENT 'Ã¥â€¦â€¦Ã¥â‚¬Â¼Ã§â€šÂ¹Ã§Â¼â€“Ã¥ÂÂ·',
  `SysTime` datetime DEFAULT NULL COMMENT '数据插入时间',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=312 DEFAULT CHARSET=utf8 COMMENT='Ã¥â€¦â€¦Ã¥â‚¬Â¼Ã¤ÂºÂ¤Ã¦Ëœâ€œÃ¦ËœÅ½Ã§Â»â€ ';

-- ----------------------------
-- Table structure for tab_log_type
-- ----------------------------
DROP TABLE IF EXISTS `tab_log_type`;
CREATE TABLE `tab_log_type` (
  `log_type_id` int(11) NOT NULL COMMENT '日志类型ID',
  `log_type_name` varchar(255) DEFAULT NULL COMMENT '日志类型名',
  PRIMARY KEY (`log_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for tab_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `tab_oper_log`;
CREATE TABLE `tab_oper_log` (
  `id` int(255) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `terminalId` int(255) DEFAULT '0' COMMENT '终端编号',
  `time` datetime DEFAULT NULL COMMENT '日志时间',
  `logtype` smallint(255) DEFAULT '0' COMMENT '日志类型',
  `logMemo` varchar(255) DEFAULT '' COMMENT '日志内容',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8 COMMENT='系统操作日志表';

-- ----------------------------
-- Table structure for tab_refund
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
  `RefundReason` varchar(255) DEFAULT '' COMMENT '退款原因',
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=75 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for tab_terminal
-- ----------------------------
DROP TABLE IF EXISTS `tab_terminal`;
CREATE TABLE `tab_terminal` (
  `id` int(19) NOT NULL COMMENT 'Ã§Â»Ë†Ã§Â«Â¯ID  12Ã¤Â½Â',
  `typeId` smallint(6) DEFAULT '1' COMMENT '终端类型，默认1',
  `position` varchar(255) DEFAULT NULL COMMENT '终端投放地址',
  `enabled` tinyint(2) DEFAULT '0' COMMENT '启用状态 0：启用 1：停用',
  `memo` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for tab_terminal_status
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
-- Table structure for tab_terminal_type
-- ----------------------------
DROP TABLE IF EXISTS `tab_terminal_type`;
CREATE TABLE `tab_terminal_type` (
  `id` int(11) NOT NULL COMMENT '终端类型ID',
  `name` varchar(255) DEFAULT NULL COMMENT '类型名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Ã§Â»Ë†Ã§Â«Â¯Ã§Â±Â»Ã¥Å¾â€¹';

-- ----------------------------
-- Table structure for tab_users
-- ----------------------------
DROP TABLE IF EXISTS `tab_users`;
CREATE TABLE `tab_users` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `name` varchar(50) NOT NULL COMMENT '用户名',
  `pass` varchar(50) DEFAULT NULL COMMENT '用户密码  ',
  `memo` varchar(255) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT=' Ã¥ÂÅ½Ã¥ÂÂ°Ã§Â®Â¡Ã§Ââ€ Ã§Â³Â»Ã§Â»Å¸Ã§â€Â¨Ã¦Ë†Â·';

-- ----------------------------
-- Table structure for tab_withdraw_detail
-- ----------------------------
DROP TABLE IF EXISTS `tab_withdraw_detail`;
CREATE TABLE `tab_withdraw_detail` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `TerminalId` int(11) DEFAULT '0' COMMENT '终端ID',
  `WithdrawAmount` int(255) DEFAULT '0' COMMENT '提款金额',
  `OperTime` datetime DEFAULT NULL COMMENT '提款操作时间',
  `LastOperTime` datetime DEFAULT NULL COMMENT '上次操作时间',
  `OperUserId` int(11) DEFAULT '0' COMMENT '操作人员编号',
  `InsertTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '数据插入时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT COMMENT='提款操作明细记录';

-- ----------------------------
-- View structure for view_cash_detail
-- ----------------------------
DROP VIEW IF EXISTS `view_cash_detail`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER  VIEW `view_cash_detail` AS select 0 AS `flag`,`a`.`TerminalID` AS `TerminalID`,`a`.`CityCardNo` AS `CityCardNo`,`a`.`ChargeAmount` AS `amount`,date_format(`a`.`ChargeTime`,'%Y-%m-%d %H:%i:%S') AS `operTime`,0 AS `status` from `tab_charge_detail` `a` where (`a`.`ChargeType` = 0) union all select 1 AS `flag`,`b`.`TerminalID` AS `TerminalID`,`b`.`CardNo` AS `CityCardNo`,`b`.`Amount` AS `Amount`,date_format(`b`.`RefundTime`,'%Y-%m-%d %H:%i:%S') AS `operTime`,`b`.`Status` AS `status` from `tab_refund` `b` where (`b`.`ChargeType` = 0) ;
