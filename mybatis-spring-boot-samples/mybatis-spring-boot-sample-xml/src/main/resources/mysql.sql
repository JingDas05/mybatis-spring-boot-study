
CREATE TABLE `t_city_0` (
 `id` int unsigned NOT NULL COMMENT '主键Id',
 `sub_id` int unsigned NOT NULL COMMENT '子Id',
 `name` varchar(128) DEFAULT NULL COMMENT '名字',
 `state` varchar(128) DEFAULT NULL COMMENT '州',
 `country` varchar(128) DEFAULT NULL COMMENT '城市',
 primary key (`id`)
 )ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='城市表';

 CREATE TABLE `t_city_1` (
 `id` int unsigned NOT NULL COMMENT '主键Id',
 `sub_id` int unsigned NOT NULL COMMENT '子Id',
 `name` varchar(128) DEFAULT NULL COMMENT '名字',
 `state` varchar(128) DEFAULT NULL COMMENT '州',
 `country` varchar(128) DEFAULT NULL COMMENT '城市',
 primary key (`id`)
 )ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='城市表';


--  mbg生成所需表
CREATE TABLE `t_hotel` (
 `city` int DEFAULT NULL,
 `name` varchar(128) DEFAULT NULL,
 `address` varchar(128) DEFAULT NULL,
 `zip` varchar(128) DEFAULT NULL
 )ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='宾馆表';

--  mbg生成所需表
 CREATE TABLE `t_city` (
 `id` int unsigned NOT NULL COMMENT '主键Id',
 `sub_id` int unsigned NOT NULL COMMENT '子Id',
 `name` varchar(128) DEFAULT NULL COMMENT '名字',
 `state` varchar(128) DEFAULT NULL COMMENT '州',
 `country` varchar(128) DEFAULT NULL COMMENT '城市',
 primary key (`id`)
 )ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='城市表';
