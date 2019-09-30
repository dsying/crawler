drop table if exists news;
create table news (
    id bigint primary key auto_increment,
    title text,
    content text,
    url varchar(1000),
    createdAt timestamp,
    modifiedAt timestamp
);

drop table if exists links_to_be_processed;
create table links_to_be_processed (
    link varchar(1000)
);

drop table if exists links_already_processed;
create table links_already_processed (
    link varchar(1000)
)
