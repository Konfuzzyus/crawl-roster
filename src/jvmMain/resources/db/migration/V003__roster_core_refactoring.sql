-- Change EventRegistrations primary key from a distinct id to the combination of (event_id, player_id)
ALTER TABLE EventRegistrations DROP PRIMARY KEY;
ALTER TABLE EventRegistrations DROP COLUMN id;
ALTER TABLE EventRegistrations ADD PRIMARY KEY (event_id, player_id);

-- Register by dungeon master id instead of table id
ALTER TABLE EventRegistrations ADD COLUMN dungeon_master_id UUID null;
ALTER TABLE EventRegistrations ADD FOREIGN KEY (dungeon_master_id) references Players(id);
UPDATE EventRegistrations r SET (r.dungeon_master_id) = (SELECT t.dungeon_master_id FROM HostedTables t WHERE r.table_id=t.id);
ALTER TABLE EventRegistrations DROP COLUMN table_id;

-- Change primary key from a distinct id to the combination of (event_id, dungeon_master_id)
ALTER TABLE HostedTables DROP PRIMARY KEY;
ALTER TABLE HostedTables DROP COLUMN id;
ALTER TABLE HostedTables ADD PRIMARY KEY (event_id, dungeon_master_id);