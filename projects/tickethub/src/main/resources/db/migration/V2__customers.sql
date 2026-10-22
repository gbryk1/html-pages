create sequence customer_seq increment by 50;

create table customer (
    id        bigint primary key,
    email     varchar(160) not null unique,
    full_name varchar(160) not null
);
