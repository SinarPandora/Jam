-- auto-generated definition
create table instance
(
    id             bigserial                                  not null
        constraint instance_pkey
            primary key,
    name           varchar(255)                               not null,
    version        varchar(255)                               not null,
    deploy_path    varchar(255)                               not null,
    is_running     boolean      default false                 not null,
    pid            integer,
    manager_ids    integer[]    default ARRAY []::integer[]   not null,
    backend_type   varchar(255)                               not null,
    last_boot_time timestamp(0),
    args           varchar(255) default ''::character varying not null,
    inserted_at    timestamp(0) default current_timestamp     not null,
    updated_at     timestamp(0) default current_timestamp     not null
);

create unique index instance_name_index
    on instance (name);

create unique index instance_pid_index
    on instance (pid);

