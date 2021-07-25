create table trpg_actor
(
    id        bigserial not null
        constraint trpg_actor_pk
            primary key,
    name      text      not null,
    qid       bigint    not null,
    attr      text      not null,
    info      text      not null,
    is_active boolean   not null default false
);

create table trpg_actor_history
(
    id          bigserial not null
        constraint trpg_actor_history_pk
            primary key,
    actor_id    bigint    not null,
    name        text      not null,
    qid         bigint    not null,
    attr        text      not null,
    info        text      not null,
    is_active   boolean   not null default false,
    update_date timestamp not null default current_timestamp
);

-- trpg_actor history trigger
create or replace function trpg_actor_history_trigger() returns trigger as
$$
begin
    insert into trpg_actor_history (actor_id, name, qid, attr, info, is_active)
    values (new.id, new.name, new.qid, new.attr, new.info, new.is_active);
end;
$$ language plpgsql;

create trigger trpg_actor_change
    after update or insert
    on trpg_actor
    for each row
execute procedure trpg_actor_history_trigger();

create table trpg_game
(
    id        bigserial not null
        constraint trpg_game_pk
            primary key,
    name      text      not null,
    rule_name text      not null,
    kp_list   text      not null
);

create table trpg_status
(
    id          bigserial not null
        constraint trpg_status_pk
            primary key,
    actor_id    bigint    not null,
    game_id     bigint    not null references trpg_game,
    attr_adjust text      not null default '{}',
    status      text      not null default '{}',
    tags        text      not null default '{}',
    update_date timestamp not null default current_timestamp
);

create table trpg_status_history
(
    id          bigserial not null
        constraint trpg_status_history_pk
            primary key,
    status_id   bigint    not null,
    actor_id    bigint    not null,
    game_id     bigint    not null references trpg_game,
    attr_adjust text      not null default '{}',
    status      text      not null default '{}',
    tags        text      not null default '{}',
    update_date timestamp not null default current_timestamp
);

-- trpg_actor history trigger
create or replace function trpg_status_history_trigger() returns trigger as
$$
begin
    insert into trpg_status_history(status_id, actor_id, game_id, attr_adjust, status, tags, update_date)
    values (new.id, new.actor_id, new.game_id, new.attr_adjust, new.status, new.tags, new.update_date);
end;
$$ language plpgsql;

create trigger trpg_status_change
    after update or insert
    on trpg_status
    for each row
execute procedure trpg_status_history_trigger();
