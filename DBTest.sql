
CREATE TABLE "books" (
	"book_id" SERIAL,
	"title" varchar(255),
	"description" varchar(255),
	CONSTRAINT books_pk PRIMARY KEY ("book_id")
) WITH (
  OIDS=FALSE
);
INSERT INTO public.books (book_id, title, description)
VALUES (1, 'T1', 'D1'), (2, 'T2', 'D2'), ('3', 'T3', 'D3');

CREATE TABLE "authors" (
  "author_id" SERIAL,
  "last_name" varchar(255),
  CONSTRAINT author_pk PRIMARY KEY ("author_id")
) WITH (
OIDS=FALSE
);
INSERT INTO public.authors (author_id, last_name)
VALUES (1, 'L1'),(2, 'L2'), (3, 'L3');

CREATE TABLE "jnct_books_authors" (
  "book_id" INTEGER,
  "author_id" INTEGER,
  "is_main_author" BOOLEAN,
  PRIMARY KEY (book_id, author_id),
  FOREIGN KEY (book_id) REFERENCES books(book_id)
                        ON UPDATE CASCADE
                        ON DELETE CASCADE,
  FOREIGN KEY (author_id) REFERENCES authors(author_id)
                        ON UPDATE CASCADE
                        ON DELETE CASCADE
) WITH (
OIDS=FALSE
);

INSERT INTO public.jnct_books_authors (book_id, author_id, is_main_author)
VALUES (1,1, TRUE ), (1,2, FALSE ), (1,3, FALSE),
  (2,1, FALSE ), (2,3, TRUE ),
  (3,2, FALSE), (3, 3, TRUE );


SELECT *  FROM books JOIN jnct_books_authors USING(book_id);