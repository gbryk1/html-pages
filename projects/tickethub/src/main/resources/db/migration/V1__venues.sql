create sequence venue_seq increment by 50;
create sequence sector_seq increment by 50;
create sequence seat_seq increment by 50;

create table venue (
    id          bigint primary key,
    name        varchar(120) not null,
    street      varchar(120) not null,
    city        varchar(80)  not null,
    postal_code varchar(10)  not null
);

create table sector (
    id       bigint primary key,
    venue_id bigint not null references venue (id),
    name     varchar(60) not null,
    kind     varchar(20) not null check (kind in ('SEATED', 'STANDING')),
    capacity integer not null,
    unique (venue_id, name)
);

create table seat (
    id        bigint primary key,
    sector_id bigint not null references sector (id),
    row_label varchar(5) not null,
    number    integer not null,
    unique (sector_id, row_label, number)
);
