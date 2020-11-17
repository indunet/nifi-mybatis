create database vehicle;
use vehicle;

create table vehicle_model
(
    id bigserial not null
            primary key,
    manufacturer_id bigint,
    name_cn varchar(128) not null,
    name_en varchar(128)
);

create table vehicle
(
    id bigserial not null
            primary key,
    vehicle_model_id bigint,
    vehicle_code varchar(64) not null
);

insert into
    vehicle_model(id, manufacturer_id, name_cn, name_en)
values
    (1, 1, 'A型车', 'Model-A'),
    (2, 2, 'B型车', 'Model-B'),
    (3, 3, 'C型车', 'Model-C');

insert into
    vehicle(id, vehicle_model_id, vehicle_code)
values
    (1, 1, 'T001'),
    (2, 2, 'T002'),
    (3, 3, 'T003');

