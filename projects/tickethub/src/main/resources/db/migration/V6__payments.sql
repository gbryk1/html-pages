create sequence payment_seq increment by 50;

create table payment (
    id              bigint primary key,
    order_id        bigint not null references orders (id),
    method          varchar(20) not null,
    amount          numeric(12, 2) not null,
    currency        varchar(3) not null,
    card_last4      varchar(4),
    blik_ref        varchar(40),
    voucher_code    varchar(40),
    check (method in ('CARD', 'BLIK', 'VOUCHER')),
    check (method <> 'CARD' or card_last4 is not null),
    check (method <> 'BLIK' or blik_ref is not null),
    check (method <> 'VOUCHER' or voucher_code is not null)
);
create index payment_order_idx on payment (order_id);
