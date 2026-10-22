create sequence category_seq increment by 50;
create sequence performer_seq increment by 50;
create sequence event_seq increment by 50;
create sequence event_performer_seq increment by 50;

create table category (
    id   bigint primary key,
    code varchar(30) not null unique,
    name varchar(80) not null
);

create table performer (
    id    bigint primary key,
    name  varchar(120) not null,
    genre varchar(40)
);

create table event (
    id          bigint primary key,
    venue_id    bigint not null references venue (id),
    category_id bigint not null references category (id),
    title       varchar(200) not null,
    starts_at   timestamp with time zone not null,
    status      varchar(20) not null check (status in ('DRAFT', 'ON_SALE', 'SOLD_OUT', 'CANCELLED')),
    version     bigint not null
);
create index event_status_starts_idx on event (status, starts_at, id);

create table event_performer (
    id            bigint primary key,
    event_id      bigint not null references event (id),
    performer_id  bigint not null references performer (id),
    billing_order integer not null,
    unique (event_id, performer_id)
);
