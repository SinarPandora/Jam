create table bwy_project
(
    id          bigserial not null
        constraint bwy_project_pkey
            primary key,
    name        text      not null,
    detail      text      not null default '',
    due_date    date      not null,
    create_qid  bigint    not null,
    create_time timestamp not null default current_timestamp,
    is_active   boolean   not null default true
);

create table bwy_group
(
    id          bigserial not null
        constraint bwy_group_pkey
            primary key,
    name        text      not null,
    detail      text      not null default '',
    create_time timestamp not null default current_timestamp,
    creator_qid bigint    not null,
    is_active   boolean   not null default true
);

create table bwy_group_member
(
    id          bigserial not null
        constraint bwy_group_member_pkey
            primary key,
    group_id    bigint    not null references bwy_group (id),
    member_qid  bigint    not null,
    create_time timestamp not null default current_timestamp
);

create table bwy_notification
(
    id          bigserial not null
        constraint bwy_notification_pkey
            primary key,
    content     text      not null,
    cron        text      not null,
    scope       text      not null,
    scope_type  text      not null,
    create_time timestamp not null default current_timestamp,
    creator_qid bigint    not null,
    is_active   boolean   not null default true
);

