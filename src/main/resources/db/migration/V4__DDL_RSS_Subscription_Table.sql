-- auto-generated definition
create table rss_subscription
(
    source_url      text                                not null
        constraint rss_subscription_pk
            primary key,
    source_category text                                not null,
    subscribers     text                                not null,
    last_key        text                                not null,
    last_update     timestamp default CURRENT_TIMESTAMP not null
);

create unique index rss_subscription_url_uindex
    on rss_subscription (source_url);

