alter table orders
    add column created_at       timestamp with time zone,
    add column created_by       varchar(80),
    add column last_modified_at timestamp with time zone,
    add column last_modified_by varchar(80);

create sequence revinfo_seq increment by 50;
create table revinfo (
    rev      integer primary key,
    revtstmp bigint
);

create table orders_aud (
    id               bigint not null,
    rev              integer not null references revinfo (rev),
    revtype          smallint,
    customer_id      bigint,
    status           varchar(20),
    total_amount     numeric(12, 2),
    total_currency   varchar(3),
    created_at       timestamp with time zone,
    created_by       varchar(80),
    last_modified_at timestamp with time zone,
    last_modified_by varchar(80),
    primary key (id, rev)
);
