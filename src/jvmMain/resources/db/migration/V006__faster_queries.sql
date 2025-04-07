create index Event_IDX_guild_id_event_date on Events(guild_id, event_date);
create index EventRegistrations_IDX_event_id on EventRegistrations(event_id);
create index HostedTables_IDX_event_id on HostedTables(event_id);