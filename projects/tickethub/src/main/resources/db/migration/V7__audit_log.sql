create sequence audit_log_seq increment by 50;
create table audit_log (
    id      bigint primary key,
    at      timestamp with time zone not null,
    action  varchar(60) not null,
    details varchar(500)
);
