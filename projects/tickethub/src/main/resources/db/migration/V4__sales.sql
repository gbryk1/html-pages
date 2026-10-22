create sequence orders_seq increment by 50;
create sequence ticket_seq increment by 50;
create sequence price_tier_seq increment by 50;
create sequence event_seat_seq increment by 50;

create table price_tier (
    id             bigint primary key,
    event_id       bigint not null references event (id),
    sector_id      bigint not null references sector (id),
    price_amount   numeric(12, 2) not null,
    price_currency varchar(3) not null,
    unique (event_id, sector_id)
);

create table event_seat (
    id          bigint primary key,
    event_id    bigint not null references event (id),
    seat_id     bigint not null references seat (id),
    status      varchar(20) not null check (status in ('AVAILABLE', 'HELD', 'SOLD')),
    held_until  timestamp with time zone,
    version     bigint not null,
    unique (event_id, seat_id)
);
create index event_seat_status_idx on event_seat (event_id, status);

create table orders (
    id             bigint primary key,
    customer_id    bigint not null references customer (id),
    status         varchar(20) not null check (status in ('NEW', 'PAID', 'CANCELLED')),
    total_amount   numeric(12, 2) not null,
    total_currency varchar(3) not null,
    version        bigint not null
);
create index orders_customer_idx on orders (customer_id);

create table ticket (
    id             bigint primary key,
    order_id       bigint not null references orders (id),
    event_seat_id  bigint not null references event_seat (id),
    holder_name    varchar(160) not null,
    price_amount   numeric(12, 2) not null,
    price_currency varchar(3) not null
);
create index ticket_order_idx on ticket (order_id);
