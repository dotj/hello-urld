# --- !Ups
create table "analytics" (
  "id" bigint generated by default as identity(start with 1) not null primary key,
  "token" varchar unique not null,
  "hit_count" bigint default 0
);

# --- !Downs
drop table "analytics" if exists;