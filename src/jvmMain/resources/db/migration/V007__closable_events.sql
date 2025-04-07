-- add closed time to events table
ALTER TABLE Events ADD COLUMN closed_on timestamp(0) with time zone;

-- remove leftovers from half-baked ideas
ALTER TABLE Events DROP COLUMN name;
ALTER TABLE Events DROP COLUMN description;
ALTER TABLE EventRegistrations DROP COLUMN player_character_id;