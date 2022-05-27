create table LinkedGuilds (
    id UUID not null primary key,
    name varchar(100) not null,
    discord_id varchar(100) not null unique
);

create table Players (
    id UUID not null primary key,
    player_name varchar(100) null,

    discord_avatar varchar(255) null,
    discord_name varchar(100) null,
    discord_id varchar(100) null unique,
    google_id varchar(100) null unique
);

create table PlayerCharacters (
    id UUID not null primary key
);

create table Events (
    id UUID not null primary key,
    event_date DATE not null,
    guild_id UUID not null,

    foreign key (guild_id) references LinkedGuilds(id)
);

create table EventRegistrations (
    id UUID not null primary key,
    event_id UUID not null,
    player_id UUID not null,
    player_character_id UUID null,
    table_id UUID null,

    foreign key (event_id) references Events(id),
    foreign key (player_id) references Players(id),
    unique (event_id, player_id)
);

create table HostedTables (
    id UUID not null primary key,
    event_id UUID null,
    dungeon_master_id UUID not null,

    foreign key (event_id) references Events(id),
    foreign key (dungeon_master_id) references Players(id),
    unique (event_id, dungeon_master_id)
);
