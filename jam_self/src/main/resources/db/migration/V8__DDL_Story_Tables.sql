create table story
(
    id             bigserial                             not null
        constraint story_pk
            primary key,
    path           varchar                               not null,
    name           varchar                               not null,
    checksum       varchar                               not null,
    keyword        varchar     default ''                not null,
    author         varchar     default '无名氏'             not null,
    script         varchar                               not null,
    status         varchar(10) default '使用中'             not null,
    load_date      timestamp   default current_timestamp not null,
    default_config varchar                               not null
);

create index story_author_name_index
    on story (author, name);

create table story_instance
(
    id          bigserial                           not null
        constraint story_instance_pk
            primary key,
    story_id    bigint                              not null
        constraint story_instance_story_id_fk
            references story,
    data        varchar,
    auto_save   varchar   default ''                not null,
    config      varchar                             not null,
    chat_type   varchar(15)                         not null,
    chat_id     bigint                              not null,
    last_update timestamp default current_timestamp not null
);

create index story_instance_story_id_index
    on story_instance (story_id);

create table story_save_file
(
    id                        bigserial          not null
        constraint story_save_file_pk
            primary key,
    story_id                  bigint             not null
        constraint story_save_file_story_id_fk
            references story,
    chat_id                   bigint             not null,
    save_list                 varchar default '' not null,
    chat_type                 varchar(15)        not null,
    chat_scope_default_config varchar            not null
);

create unique index story_save_file_chat_id_chat_type_story_id_uindex
    on story_save_file (chat_id, chat_type, story_id);

create table story_save_inherit
(
    id              bigserial not null
        constraint story_save_inherit_pk
            primary key,
    legacy_story_id bigint    not null
        constraint story_save_inherit_legacy_story_id_fk
            references story,
    in_use_story_id bigint    not null
        constraint story_save_inherit_in_use_story_id_fk
            references story
);
