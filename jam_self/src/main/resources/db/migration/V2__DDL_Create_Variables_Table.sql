-- auto-generated definition
create table variables
(
    id               bigserial                                 not null
        constraint variables_pkey
            primary key,
    name             varchar default ''::character varying     not null,
    chat_type        varchar default ''::character varying     not null,
    chat_id          bigint  default '-1'::integer             not null,
    value            varchar default ''::character varying     not null,
    type             varchar default 'TEXT'::character varying not null,
    last_update_date timestamp                                 not null
);

create unique index variables_id_uindex
    on variables (id);
