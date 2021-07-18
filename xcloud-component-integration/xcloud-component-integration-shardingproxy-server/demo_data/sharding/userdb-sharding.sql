drop schema if exists userdb_g0db0;
drop schema if exists userdb_g0db1;
drop schema if exists userdb_g1db0;
drop schema if exists userdb_g1db1;
drop schema if exists userdb_g1db2;

-- Sharding Group0(db0)
drop schema if exists userdb_g0db0;
create schema if not exists userdb_g0db0;
use userdb_g0db0;
drop table if exists t_user_0;
drop table if exists t_user_1;
create table if not exists t_user_0(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group0，所属物理数据库：db0，存储数据范围(ID取值)：[0 ~ 1000w)，分片算法：id%6=0,1,2';
create table if not exists t_user_1(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group0，所属物理数据库：db0，存储数据范围(ID取值)：[1000w ~ 2000w)，分片算法：id%6=3,4,5';
insert into `userdb_g0db0`.`t_user_0` (`id`, `name`) values ('1', 'jack1');
insert into `userdb_g0db0`.`t_user_1` (`id`, `name`) values ('10000005', 'jack10000005');

-- Sharding Group0(db1)
drop schema if exists userdb_g0db1;
create schema if not exists userdb_g0db1;
use userdb_g0db1;
drop table if exists t_user_0;
drop table if exists t_user_1;
create table if not exists t_user_0(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group0，所属物理数据库：db1，存储数据范围(ID取值)：[0 ~ 1000w)，分片算法：id%6=0,1,2';
create table if not exists t_user_1(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group0，所属物理数据库：db1，存储数据范围(ID取值)：[1000w ~ 2000w)，分片算法：id%6=3,4,5';
insert into `userdb_g0db1`.`t_user_0` (`id`, `name`) values ('104', 'jack104');
insert into `userdb_g0db1`.`t_user_1` (`id`, `name`) values ('10000005', 'jack10000005');

-- Sharding Group1(db0)
drop schema if exists userdb_g1db0;
create schema if not exists userdb_g1db0;
use userdb_g1db0;
drop table if exists t_user_0;
drop table if exists t_user_1;
drop table if exists t_user_2;
create table if not exists t_user_0(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group0，所属物理数据库：db0，存储数据范围(ID取值)：[0 ~ 1000w)，分片算法：id%6=0,1,2';
create table if not exists t_user_1(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group0，所属物理数据库：db0，存储数据范围(ID取值)：[1000w ~ 2000w)，分片算法：id%6=3,4,5';
insert into `userdb_g1db0`.`t_user_0` (`id`, `name`) values ('109', 'jack109');
insert into `userdb_g1db0`.`t_user_1` (`id`, `name`) values ('10000004', 'jack10000004');

-- Sharding Group1(db1)
drop schema if exists userdb_g1db1;
create schema if not exists userdb_g1db1;
use userdb_g1db1;
drop table if exists t_user_0;
drop table if exists t_user_1;
create table if not exists t_user_0(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group1，所属物理数据库：db1，存储数据范围(ID取值)：[0 ~ 1000w)，分片算法：id%6=0,1,2';
create table if not exists t_user_1(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group1，所属物理数据库：db1，存储数据范围(ID取值)：[1000w ~ 2000w)，分片算法：id%6=3,4,5';
insert into `userdb_g1db1`.`t_user_0` (`id`, `name`) values ('201', 'jack201');
insert into `userdb_g1db1`.`t_user_1` (`id`, `name`) values ('10000021', 'jack10000021');

-- Sharding Group1(db2)
drop schema if exists userdb_g1db2;
create schema if not exists userdb_g1db2;
use userdb_g1db2;
drop table if exists t_user_0;
drop table if exists t_user_1;
create table if not exists t_user_0(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group1，所属物理数据库：db2，存储数据范围(ID取值)：[0 ~ 1000w)，分片算法：id%6=0,1,2';
create table if not exists t_user_1(id bigint not null, `name` varchar(32) character set utf8 collate utf8_bin, primary key(id))engine=innodb comment='对外虚拟库名：userdb，所属分片分组：group1，所属物理数据库：db2，存储数据范围(ID取值)：[1000w ~ 2000w)，分片算法：id%6=3,4,5';
insert into `userdb_g1db2`.`t_user_0` (`id`, `name`) values ('201', 'jack201');
insert into `userdb_g1db2`.`t_user_1` (`id`, `name`) values ('10000021', 'jack10000021');
