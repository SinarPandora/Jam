create table source_observer
(
    id              bigserial not null primary key,
    source_identity text      not null,
    source_type     text      not null,
    create_time     timestamp not null default current_timestamp,
    is_active       boolean   not null default true
);

create table source_subscriber
(
    id               bigserial not null primary key,
    chat_id          bigint    not null,
    chat_type        text      not null,
    source_id        bigint    not null references source_observer,
    is_paused        boolean   not null default false,
    last_key         text      not null default 'INIT',
    last_update_time timestamp not null default current_timestamp,
    create_time      timestamp not null default current_timestamp,
    is_active        boolean   not null default true
);

create unique index source_subscriber_source_chat
    on source_subscriber (chat_id, chat_type, source_id);

create table source_push_history
(
    id            bigserial not null primary key,
    subscriber_id bigint    not null references source_subscriber on delete cascade,
    push_time     timestamp not null default current_timestamp,
    message_key   text      not null
);

create unique index source_push_history_sid_key
    on source_push_history (subscriber_id, message_key);
