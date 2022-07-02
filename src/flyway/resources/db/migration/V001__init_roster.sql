create type TableLanguage as enum ('SwissGerman', 'English', 'German', 'Italian', 'French', 'Romansh');

create table LinkedGuilds (
    id UUID not null primary key,
    name varchar(100) not null,
    discord_id varchar(100) not null unique
);

create table Players (
    id UUID not null primary key,
    player_name varchar(100) null,
    languages varchar(255) null,

    discord_id varchar(100) not null unique,
    discord_name varchar(100) null,
    discord_avatar varchar(255) null
);

create table PlayerCharacters (
    id UUID not null primary key
);

create table Events (
    id UUID not null primary key,
    event_date DATE not null,
    guild_id UUID not null,

    foreign key (guild_id) references LinkedGuilds(id),
    unique (guild_id, event_date)
);

create table EventRegistrations (
    id UUID not null primary key,
    event_id UUID not null,
    player_id UUID not null,
    player_character_id UUID null,
    table_id UUID null,
    registration_time timestamp(0) with time zone not null,

    foreign key (event_id) references Events(id),
    foreign key (player_id) references Players(id),
    unique (event_id, player_id)
);

create table HostedTables (
    id UUID not null primary key,
    event_id UUID not null,
    dungeon_master_id UUID not null,
    adventure_title varchar(100) null,
    adventure_description varchar(3000) null,
    module_designation varchar(32) null,
    table_language TableLanguage not null,
    min_players integer not null,
    max_players integer not null,
    min_character_level integer not null,
    max_character_level integer not null,

    foreign key (event_id) references Events(id),
    foreign key (dungeon_master_id) references Players(id),
    unique (event_id, dungeon_master_id)
);

