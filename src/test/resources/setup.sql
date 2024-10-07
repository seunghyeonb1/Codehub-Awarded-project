create table authors
(
    author_id  serial
        primary key,
    avatar_url varchar(255),
    biography  varchar(255),
    name       varchar(255)
);

create table categories
(
    category_id serial primary key,
    description varchar(255),
    name varchar(255)
);

create table books
(
    book_id          serial
        primary key,
    avatar_url       varchar(255),
    description      varchar(255),
    isbn             varchar(255),
    price            double precision,
    publication_date date,
    stock_quantity   integer,
    title            varchar(255),
    category_id      bigint
        constraint fkleqa3hhc0uhfvurq6mil47xk0
            references categories
);

create table book_authors
(
    book_id   integer not null
        constraint fkbhqtkv2cndf10uhtknaqbyo0a
            references books,
    author_id integer not null
        constraint fko86065vktj3hy1m7syr9cn7va
            references authors,
    primary key (book_id, author_id)
);