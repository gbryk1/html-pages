create table note (
    id         bigserial    primary key,
    owner      varchar(50)  not null references users (username),
    title      varchar(120) not null,
    body       text         not null,
    created_at timestamptz  not null default now()
);
create index ix_note_owner on note (owner);
