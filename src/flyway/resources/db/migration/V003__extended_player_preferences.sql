-- Add extra preference attributes to player settings
alter table Players add tier_preference integer not null default 0;
alter table Players add character_preference UUID null;

-- Fix data model for player character tracking
alter table PlayerCharacters drop column character_class;
alter table PlayerCharacters drop column character_level;
alter table PlayerCharacters add character_name varchar(100) not null default 'Unnamed Hero';
alter table PlayerCharacters add unique(player_id, dnd_beyond_id);

create table PlayerCharacterClasses (
    character_id UUID not null,
    level integer not null,
    name varchar(100) not null,

    foreign key (character_id) references PlayerCharacters(id)
);

-- Remove obsolete types
drop type CharacterClass;