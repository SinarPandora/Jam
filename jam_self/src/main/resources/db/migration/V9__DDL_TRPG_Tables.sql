create table trpg_actor
(
    id             bigserial not null
        constraint trpg_actor_pk
            primary key,
    name           text      not null,
    qid            bigint    not null,
    attr           text      not null,
    info           text      not null,
    default_config text      not null default '{}',
    is_active      boolean   not null default false
);

create table trpg_actor_snapshot
(
    id          bigserial not null
        constraint trpg_actor_history_pk
            primary key,
    actor_id    bigint    not null references trpg_actor,
    name        text      not null,
    qid         bigint    not null,
    attr        text      not null,
    info        text      not null,
    create_time timestamp not null default current_timestamp
);

create table trpg_game
(
    id        bigserial not null
        constraint trpg_game_pk
            primary key,
    name      text      not null,
    rule_name text      not null,
    kp_list   text      not null,
    last_chat text
);

create table trpg_status
(
    id             bigserial not null
        constraint trpg_status_pk
            primary key,
    snapshot_id    bigint    not null references trpg_actor_snapshot,
    game_id        bigint    not null references trpg_game,
    attr_overrides text      not null default '{}',
    tags           text      not null default '{}',
    config         text      not null default '{}',
    update_time    timestamp not null default current_timestamp
);

create table trpg_status_change_history
(
    id           bigserial not null
        constraint trpg_status_change_history_pk
            primary key,
    status_id    bigint    not null references trpg_status,
    name         text      not null,
    adjust_expr  text      not null,
    origin_value int       not null,
    after_value  int       not null,
    create_time  timestamp not null default current_timestamp
);

create table trpg_roll_history
(
    id          bigserial not null
        constraint trpg_roll_history_pk
            primary key,
    status_id   bigint    not null references trpg_status,
    result      text      not null,
    point       int       not null,
    pass        boolean   not null,
    create_time timestamp not null default current_timestamp
);
