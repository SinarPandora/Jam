-- auto-generated definition
create table message_records
(
    id               bigserial                             not null
        constraint message_records_pkey
            primary key,
    message          varchar default ''::character varying not null,
    message_id       bigint  default 0                     not null,
    message_type     varchar default ''::character varying not null,
    message_sub_type varchar default ''::character varying not null,
    post_type        varchar default ''::character varying not null,
    raw_message      varchar default ''::character varying not null,
    self_id          bigint  default 0                     not null,
    sender_id        bigint  default 0                     not null,
    group_id         bigint  default '-1'::integer         not null,
    font             bigint  default 0                     not null,
    timestamp        bigint  default '-1'::integer         not null
);

create unique index message_records_id_uindex
    on message_records (id);
