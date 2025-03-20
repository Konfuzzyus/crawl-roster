DROP TABLE PlayerCharacters;
DROP TYPE CharacterClass;

EXECUTE IMMEDIATE
    select 'ALTER TABLE EventRegistrations DROP CONSTRAINT '
        || QUOTE_IDENT(CONSTRAINT_NAME)
    from INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    where TABLE_NAME ILIKE 'EventRegistrations'
    AND CONSTRAINT_TYPE = 'FOREIGN KEY'
    FETCH FIRST ROW ONLY;
EXECUTE IMMEDIATE
    select 'ALTER TABLE EventRegistrations DROP CONSTRAINT '
        || QUOTE_IDENT(CONSTRAINT_NAME)
    from INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    where TABLE_NAME ILIKE 'EventRegistrations'
    AND CONSTRAINT_TYPE = 'FOREIGN KEY'
    FETCH FIRST ROW ONLY;
EXECUTE IMMEDIATE
    select 'ALTER TABLE EventRegistrations DROP CONSTRAINT '
        || QUOTE_IDENT(CONSTRAINT_NAME)
    from INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    where TABLE_NAME ILIKE 'EventRegistrations'
    AND CONSTRAINT_TYPE = 'FOREIGN KEY'
    FETCH FIRST ROW ONLY;
EXECUTE IMMEDIATE
    select 'ALTER TABLE HostedTables DROP CONSTRAINT '
        || QUOTE_IDENT(CONSTRAINT_NAME)
    from INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    where TABLE_NAME ILIKE 'HostedTables'
    AND CONSTRAINT_TYPE = 'FOREIGN KEY'
    FETCH FIRST ROW ONLY;
EXECUTE IMMEDIATE
    select 'ALTER TABLE HostedTables DROP CONSTRAINT '
        || QUOTE_IDENT(CONSTRAINT_NAME)
    from INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    where TABLE_NAME ILIKE 'HostedTables'
    AND CONSTRAINT_TYPE = 'FOREIGN KEY'
    FETCH FIRST ROW ONLY;
EXECUTE IMMEDIATE
    select 'ALTER TABLE GuildRoles DROP CONSTRAINT '
        || QUOTE_IDENT(CONSTRAINT_NAME)
    from INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    where TABLE_NAME ILIKE 'GuildRoles'
    AND CONSTRAINT_TYPE = 'FOREIGN KEY'
    FETCH FIRST ROW ONLY;
EXECUTE IMMEDIATE
    select 'ALTER TABLE GuildRoles DROP CONSTRAINT '
        || QUOTE_IDENT(CONSTRAINT_NAME)
    from INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    where TABLE_NAME ILIKE 'GuildRoles'
    AND CONSTRAINT_TYPE = 'FOREIGN KEY'
    FETCH FIRST ROW ONLY;
EXECUTE IMMEDIATE
    select 'ALTER TABLE Events DROP CONSTRAINT '
        || QUOTE_IDENT(CONSTRAINT_NAME)
    from INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    where TABLE_NAME ILIKE 'Events'
    AND CONSTRAINT_TYPE = 'FOREIGN KEY'
    FETCH FIRST ROW ONLY;

EXECUTE IMMEDIATE
    select 'ALTER TABLE Guilds DROP CONSTRAINT '
        || QUOTE_IDENT(CONSTRAINT_NAME)
    from INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    where TABLE_NAME ILIKE 'Guilds'
    AND CONSTRAINT_TYPE = 'UNIQUE'
    FETCH FIRST ROW ONLY;
EXECUTE IMMEDIATE
    select 'ALTER TABLE Players DROP CONSTRAINT '
        || QUOTE_IDENT(CONSTRAINT_NAME)
    from INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    where TABLE_NAME ILIKE 'Players'
    AND CONSTRAINT_TYPE = 'UNIQUE'
    FETCH FIRST ROW ONLY;
EXECUTE IMMEDIATE
    select 'ALTER TABLE Events DROP CONSTRAINT '
        || QUOTE_IDENT(CONSTRAINT_NAME)
    from INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    where TABLE_NAME ILIKE 'Events'
    AND CONSTRAINT_TYPE = 'UNIQUE'
    FETCH FIRST ROW ONLY;
EXECUTE IMMEDIATE
    select 'ALTER TABLE EventRegistrations DROP CONSTRAINT '
        || QUOTE_IDENT(CONSTRAINT_NAME)
    from INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    where TABLE_NAME ILIKE 'EventRegistrations'
    AND CONSTRAINT_TYPE = 'UNIQUE'
    FETCH FIRST ROW ONLY;
EXECUTE IMMEDIATE
    select 'ALTER TABLE HostedTables DROP CONSTRAINT '
        || QUOTE_IDENT(CONSTRAINT_NAME)
    from INFORMATION_SCHEMA.TABLE_CONSTRAINTS
    where TABLE_NAME ILIKE 'HostedTables'
    AND CONSTRAINT_TYPE = 'UNIQUE'
    FETCH FIRST ROW ONLY;

alter table Guilds drop primary key;
alter table Players drop primary key;
alter table GuildRoles drop primary key;
alter table Hostedtables drop primary key;
alter table Events drop primary key;
alter table EventRegistrations drop primary key;

alter table Events add CONSTRAINT Events_PK PRIMARY KEY (id);
alter table Guilds add CONSTRAINT Guilds_PK PRIMARY KEY (id);
alter table Players add CONSTRAINT Players_PK PRIMARY KEY (id);
alter table Hostedtables add CONSTRAINT Hostedtables_PK PRIMARY KEY (event_id, dungeon_master_id);
alter table EventRegistrations add CONSTRAINT EventRegistrations_PK PRIMARY KEY (event_id, player_id);

alter table GuildRoles add CONSTRAINT GuildRoles_PK PRIMARY KEY (player_id, guild_id);
alter table Events add CONSTRAINT Events_FK_guild_id FOREIGN KEY (guild_id) references Guilds(id);
alter table GuildRoles add CONSTRAINT GuildRoles_FK_player_id FOREIGN KEY (player_id) references Players(id);
alter table GuildRoles add CONSTRAINT GuildRoles_FK_guild_id FOREIGN KEY (guild_id) references Guilds(id);
alter table Hostedtables add CONSTRAINT Hostedtables_FK_event_id FOREIGN KEY (event_id) references Events(id);
alter table Hostedtables add CONSTRAINT Hostedtables_FK_dungeon_master_id FOREIGN KEY (dungeon_master_id) references Players(id);
alter table EventRegistrations add CONSTRAINT EventRegistrations_FK_dungeon_master_id FOREIGN KEY (dungeon_master_id) references Players(id);
alter table EventRegistrations add CONSTRAINT EventRegistrations_FK_event_id FOREIGN KEY (event_id) references Events(id);

alter table Guilds add CONSTRAINT Guilds_UNIQUE_discord_id UNIQUE (discord_id);
alter table Players add CONSTRAINT Players_UNIQUE_discord_id UNIQUE (discord_id);
alter table Events add CONSTRAINT Events_UNIQUE_guild_id_event_date UNIQUE (guild_id, event_date);
alter table EventRegistrations add CONSTRAINT EventRegistrations_UNIQUE_event_id_player_id unique (event_id, player_id);
alter table HostedTables add CONSTRAINT HostedTables_UNIQUE_event_id_dungeon_master_id unique (event_id, dungeon_master_id);