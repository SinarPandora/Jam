drop table if exists story_instance;

comment on column story_runner_instance.data is '自动存档';

drop table if exists story_save_file;
create table story_save_file
(
    id            bigserial                           not null
        constraint story_save_file_pk
            primary key,
    story_id      bigint                              not null
        constraint story_save_file_story_id_fk
            references story,
    name          varchar,
    data          varchar                             not null,
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
        constraint story_runner_config_pk
            primary key,
    story_id    bigint                              not null
        constraint story_runner_config_story_id_fk
            references story,
    chat_type   varchar(15)                         not null,
    chat_id     bigint                              not null,
    config      varchar                             not null,
    last_update timestamp default current_timestamp not null
);

create unique index story_runner_config_chat_id_chat_type_story_id_uindex
    on story_runner_config (chat_id, chat_type, story_id);
