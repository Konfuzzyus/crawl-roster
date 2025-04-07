CREATE TYPE Tableaudience as enum ('Beginner', 'Regular');

ALTER TABLE HostedTables ADD COLUMN audience Tableaudience not null USING 'Regular';
ALTER TABLE HostedTables ADD COLUMN game_system varchar(128) null;
