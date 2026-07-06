create table time_slot
(
    slot_id             bigserial primary key,
    user_id             integer,
    begin_time          timestamptz,
    duration_in_minutes integer,
    free                boolean
);

create table meeting
(
    meeting_id   bigserial primary key,
    slot_id      bigserial references time_slot,
    title        text,
    description  text,
    participants jsonb
);