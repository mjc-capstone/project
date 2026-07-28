UPDATE regions
SET source_system = 'SEOUL_OPEN_DATA'
WHERE source_system IS NULL;
