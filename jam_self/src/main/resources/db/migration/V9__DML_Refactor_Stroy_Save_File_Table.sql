drop table if exists story_instance;
create table story_instance
(
    id          bigserial                           not null
        constraint story_instance_pk
            primary key,
    story_id    bigint                              not null
        constraint story_instance_story_id_fk
            references story,
    data        varchar                             not null,
    config      varchar                             not null,
    chat_type   varchar(15)                         not null,
    chat_id     bigint                              not null,
    last_update timestamp default current_timestamp not null
);

create
index story_instance_story_id_index
    on story_instance (story_id);

drop table if exists story_save_file;
create table story_save_file
(
    id            bigserial                           not null
        constraint story_save_file_pk
            primary key,
    story_id      bigint                              not null
        constraint story_save_file_story_id_fk
            references story,
    data          varchar                             not null,
    config        varchar                             not null,
    chat_type     varchar(15)                         not null,
    chat_id       bigint                              not null,
    record_time   timestamp default current_timestamp not null,
    is_auto_saved boolean   default false             not null
);

create
index story_save_file_chat_id_chat_type_story_id_uindex
    on story_save_file (chat_id, chat_type, story_id);

create table story_runner_config
(
    id          bigserial                           not null
        constraint story_save_file_pk
            primary key,
    story_id    bigint                              not null,
    constraint story_save_file_story_id_fk references story,
    chat_type   varchar(15)                         not null,
    chat_id     bigint                              not null,
    config      varchar                             not null,
    last_update timestamp default current_timestamp not null
);

create unique index story_runner_config_chat_id_chat_type_story_id_uindex
    on story_runner_config (chat_id, chat_type, story_id);
