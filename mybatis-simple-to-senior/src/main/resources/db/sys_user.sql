CREATE TABLE sys_user (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户id',
  user_name VARCHAR(50) COMMENT '用户名',
  user_password VARCHAR(50) COMMENT '密码',
  user_email VARCHAR(50) COMMENT '邮箱',
  user_info TEXT COMMENT '简介',
  head_img BLOB COMMENT '头像',
  create_time DATETIME COMMENT '创建时间',
  PRIMARY KEY (id)
);
ALTER TABLE sys_user COMMENT '用户表';
INSERT INTO `sys_user` VALUES ('1','admin','123456','admin@me','管理员','null','2017-07-09 09:21:00');
INSERT INTO `sys_user` VALUES ('1001','test','123456','test@me','测试用户','null','2017-07-08 09:21:00');
CREATE TABLE sys_role(
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  role_name VARCHAR(50) COMMENT '角色名字',
  enabled INT COMMENT '有效标志',
  create_by BIGINT COMMENT '创建人',
  create_time TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (id)
);
INSERT INTO `sys_role` VALUES ('1','管理员','1','1', '2017-07-08 09:21:00');
INSERT INTO `sys_role` VALUES ('2','管理员','1','1', '2017-07-01 09:21:00');
ALTER TABLE sys_role COMMENT '角色表';
CREATE TABLE sys_privilege(
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  privilege_name VARCHAR(50) COMMENT '权限名称',
  privilege_url VARCHAR(200) COMMENT '权限URL',
  PRIMARY KEY (id)
);
INSERT INTO sys_privilege VALUES ('1', '用户管理', '/users');
INSERT INTO sys_privilege VALUES ('2', '角色管理', '/roles');
INSERT INTO sys_privilege VALUES ('3', '系统日志', '/logs');
INSERT INTO sys_privilege VALUES ('4', '人员维护', '/persons');
INSERT INTO sys_privilege VALUES ('5', '单位维护', '/companies');
ALTER TABLE sys_privilege COMMENT '';
CREATE TABLE sys_user_role (
  user_id BIGINT COMMENT '用户ID',
  role_id BIGINT COMMENT '角色ID'
);
INSERT INTO `sys_user_role` VALUES ('1','1');
INSERT INTO `sys_user_role` VALUES ('1','2');
INSERT INTO `sys_user_role` VALUES ('1001','1');
ALTER TABLE sys_user_role COMMENT '';
CREATE TABLE sys_role_privilege (
  role_id BIGINT COMMENT '角色ID',
  privilege_id BIGINT COMMENT '权限ID'
);
INSERT INTO `sys_role_privilege` VALUES ('1', '1');
INSERT INTO `sys_role_privilege` VALUES ('1', '2');
INSERT INTO `sys_role_privilege` VALUES ('1', '3');
INSERT INTO `sys_role_privilege` VALUES ('2', '4');
INSERT INTO `sys_role_privilege` VALUES ('2', '5');
ALTER TABLE sys_role_privilege COMMENT '角色权限关系表';




