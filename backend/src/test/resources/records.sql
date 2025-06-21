-- src/test/resources/records.sql
TRUNCATE TABLE "Record";
INSERT INTO "Record" (id, created_at, url, owner) VALUES
                                                      ('11111111-1111-1111-1111-111111111111', CURRENT_TIMESTAMP, 'http://example.com', 'alice'),
                                                      ('22222222-2222-2222-2222-222222222222', CURRENT_TIMESTAMP, 'http://example.org', 'bob');
