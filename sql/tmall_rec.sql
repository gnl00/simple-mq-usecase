-- 天猫推荐数据

drop database if exists `tmall_rec`;
create database
    if not exists `tmall_rec`
    default character set utf8mb4;

use `tmall_rec`;

drop table if exists product;
create table if not exists product (
    id int auto_increment primary key not null,
    item_id int not null,
    title text not null,
    pict_url text not null,
    category varchar(30) not null,
    brand_id varchar(30) not null,
    -- seller_id varchar(30) not null,
    store int not null
) engine innodb;

drop table if exists customer;
create table if not exists customer (
    id int auto_increment primary key not null,
    account decimal not null
) engine innodb;

drop table if exists review;
create table if not exists review (
    id int auto_increment primary key not null,
    item_id int not null,
    rater_uid varchar(30) not null,
    feedback text not null,
    gmt_create timestamp not null,
    rate_pic_url varchar(30)
) engine innodb;

drop table if exists log;
create table if not exists log (
    id int auto_increment primary key not null,
    item_id int not null,
    user_id varchar(30) not null,
    action text not null,
    vtime timestamp
) engine innodb;