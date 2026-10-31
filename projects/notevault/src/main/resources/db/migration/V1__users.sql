-- Schemat, którego oczekuje JdbcUserDetailsManager (users + authorities)
create table users (
    username varchar(50)  primary key,
    password varchar(500) not null,
    enabled  boolean      not null
);

create table authorities (
    username  varchar(50) not null references users (username),
    authority varchar(50) not null
);
create unique index ix_auth_username on authorities (username, authority);
