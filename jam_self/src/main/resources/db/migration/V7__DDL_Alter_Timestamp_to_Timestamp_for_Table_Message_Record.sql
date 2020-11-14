alter table message_records
    alter column timestamp drop default;

alter table message_records
    alter column timestamp set data type timestamp without time zone
        using to_timestamp(
            case timestamp > 9999999999
                when true then "left"(timestamp::text, 10)::bigint
                else timestamp end
        );

alter table message_records
    alter column timestamp set default current_timestamp;
