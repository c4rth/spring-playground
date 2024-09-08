create table TEST_TABLE
(
    ID            bigint identity primary key,
    EXTENDED_NAME varchar(255)
);

insert into TEST_TABLE (EXTENDED_NAME)
values ('ABC');

insert into TEST_TABLE (EXTENDED_NAME)
values ('XYZ');