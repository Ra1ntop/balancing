create table player (
    id           uuid        primary key,
    nickname     varchar(32) not null,
    display_name varchar(64),
    created_at   timestamptz not null,
    updated_at   timestamptz not null
);

-- Nicknames are compared case-insensitively, so uniqueness has to be enforced the same way
create unique index ux_player_nickname_lower on player (lower(nickname));

create table player_statistics (
    player_id           uuid    primary key references player (id) on delete cascade,
    wins                integer not null default 0,
    losses              integer not null default 0,
    draws               integer not null default 0,
    total_damage        bigint  not null default 0,
    current_win_streak  integer not null default 0,
    current_loss_streak integer not null default 0,
    constraint ck_player_statistics_non_negative check (
        wins >= 0 and losses >= 0 and draws >= 0 and total_damage >= 0
            and current_win_streak >= 0 and current_loss_streak >= 0
    )
);
