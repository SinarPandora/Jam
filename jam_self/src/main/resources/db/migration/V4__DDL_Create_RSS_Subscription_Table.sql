-- auto-generated definition
create table rss_subscription
(
    source          varchar                             not null
        constraint rss_subscription_pk
            primary key,
    source_category varchar                                not null,
    subscribers     varchar                                not null,
    channel         varchar                                not null,
    last_key        varchar      default 'IS_NOT_A_KEY'    not null,
    last_update timestamp default CURRENT_TIMESTAMP not null
);

create unique index rss_subscription_url_uindex
    on rss_subscription (source);

