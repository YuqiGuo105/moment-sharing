CREATE TABLE IF NOT EXISTS "Record" (
  id uuid not null default gen_random_uuid(),
  created_at timestamp with time zone not null default now(),
  url text null,
  owner text null,
  constraint Record_pkey primary key (id)
);

TRUNCATE TABLE "Record";

INSERT INTO "Record"(id, created_at, url, owner) VALUES
  ('11111111-1111-1111-1111-111111111111', NOW(), 'http://example.com/1', 'alice'),
  ('22222222-2222-2222-2222-222222222222', NOW(), 'http://example.com/2', 'bob');
