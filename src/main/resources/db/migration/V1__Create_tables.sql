create table news (
    id bigint auto_increment,
    title text,
    content text,
    url varchar(1000),
    createdAt timestamp default now(),
    modifiedAt timestamp default now(),
    primary key  (`id`)
);

create table links_to_be_processed (
    id bigint auto_increment,
    link varchar(1000),
    primary key (`id`)
);

create table links_already_processed (
    id bigint auto_increment,
    link varchar(1000),
    primary key (`id`)
);
