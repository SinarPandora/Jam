create table plugins
(
    id bigserial not null,
    name varchar not null,
    keywords varchar default '' not null,
    author varchar not null,
    package varchar not null,
    install_date timestamp not null,
    is_enabled bool default false not null
);

create unique index plugins_id_uindex
    on plugins (id);

create unique index plugins_package_uindex
    on plugins (package);

alter table plugins
    add constraint plugins_pk
        primary key (id);

