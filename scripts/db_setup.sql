-- Create our database and table
create database hello_urld;

create table shortlinks (
  id uuid primary key not null,
  created_dtm timestamptz not null,
  expiration_dtm timestamptz,
  token varchar not null,
  redirect_to_url varchar not null
);