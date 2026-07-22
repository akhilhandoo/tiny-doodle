
CREATE EXTENSION IF NOT EXISTS btree_gist;

create table time_slot
(
    slot_id             bigserial primary key,
    user_id             integer,
    begin_time          timestamp,
    duration_in_minutes integer,
    free                boolean,
    exclude using gist (user_id with =, tsrange(begin_time, begin_time + duration_in_minutes * interval '1 minute') with &&)
);

create table meeting
(
    meeting_id   bigserial primary key,
    slot_id      bigserial references time_slot,
    title        text,
    description  text,
    participants jsonb
);