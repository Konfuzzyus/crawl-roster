create type TableLanguage as enum ('SwissGerman', 'English', 'German', 'Italian', 'French', 'Romansh');
create type CharacterClass as enum ('Artificer', 'Barbarian', 'Bard', 'Cleric', 'Druid', 'Fighter', 'Monk', 'Paladin', 'Ranger', 'Rogue', 'Sorcerer', 'Warlock', 'Wizard');

create table Guilds (
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

create table GuildRoles (
    player_id UUID not null,
    guild_id UUID not null,
    is_admin bit not null,
    is_dungeon_master bit not null,

    primary key (player_id, guild_id),
    foreign key (player_id) references Players(id),
    foreign key (guild_id) references Guilds(id)
);

create table PlayerCharacters (
    id UUID not null primary key,
    player_id UUID not null,
    character_level integer not null,
    character_class CharacterClass not null,
    dnd_beyond_id integer null,

    foreign key (player_id) references Players(id)
);

create table Events (
    id UUID not null primary key,
    event_date DATE not null,
    event_time TIME WITH TIME ZONE null,
    guild_id UUID not null,
    name varchar(100) null,
    description varchar(3000) null,
    location varchar(100) null,

    foreign key (guild_id) references Guilds(id),
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

