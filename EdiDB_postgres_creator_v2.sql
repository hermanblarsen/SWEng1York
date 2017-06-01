CREATE TABLE "users" (
  "user_id" SERIAL,
  "user_type" varchar(255) NOT NULL,
  "username" varchar(255) NOT NULL,
  "first_name" varchar(255) NOT NULL,
  "last_name" varchar(255) NOT NULL,
  "email_address" varchar(255),
  "password_hash" varchar(255) NOT NULL,
  "password_salt" varchar(255) NOT NULL,
  "active_presentation_id" integer,
  CONSTRAINT users_pk PRIMARY KEY ("user_id")
) WITH (
OIDS=FALSE
);
INSERT INTO public.users (user_id, user_type, username, first_name, last_name, email_address, password_hash, password_salt)
VALUES (1, 'teacher', 'Teacher', 'Joe', 'Bloggs', 'em@il', 'hashedPasswordxxx', 'passwordsalt');


CREATE TABLE "modules" (
  "module_id" SERIAL,
  "moduleName" varchar(255) NOT NULL,
  "subject" varchar(255) NOT NULL,
  "description" TEXT NOT NULL,
  "time_last_updated" TIME NOT NULL,
  "time_created" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT modules_pk PRIMARY KEY ("module_id")
) WITH (
OIDS=FALSE
);
INSERT INTO public.modules (module_id, moduleName, description, subject, time_last_updated)
VALUES (1, 'TestName', 'TestDescription', 'TestSubject', '12:00:00');


CREATE TABLE "presentations" (
  "presentation_id" SERIAL,
  "module_id" integer NOT NULL,
  "current_slide_number" integer,
  "xml_url" varchar(255) NOT NULL,
  "live" BOOLEAN NOT NULL,
  CONSTRAINT presentations_pk PRIMARY KEY ("presentation_id")
) WITH (
OIDS=FALSE
);
INSERT INTO public.presentations (presentation_id, module_id, xml_url, live)
VALUES (1, 1, 'httpTest', FALSE);


CREATE TABLE "questions" (
  "question_id" SERIAL,
  "user_id" integer NOT NULL,
  "presentation_id" integer NOT NULL,
  "time_created" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "time_answered" TIME,
  "question_data" varchar(255) NOT NULL,
  "slide_number" integer NOT NULL,
  CONSTRAINT questions_pk PRIMARY KEY ("question_id")
) WITH (
OIDS=FALSE
);
INSERT INTO public.questions (question_id, user_id, presentation_id, time_answered, question_data, slide_number)
VALUES (1, 1, 1, '12:01:00', 'Test Question', 1);


CREATE TABLE "interactive_elements" (
  "interactive_element_id" SERIAL,
  "presentation_id" integer NOT NULL,
  "interactive_element_data" varchar(255) NOT NULL,
  "type" varchar(255) NOT NULL,
  "live" BOOLEAN NOT NULL,
  "response_interval" interval NOT NULL DEFAULT INTERVAL '30 secs',
  "slide_number" integer NOT NULL,
  CONSTRAINT interactive_elements_pk PRIMARY KEY ("interactive_element_id")
) WITH (
OIDS=FALSE
);
INSERT INTO public.interactive_elements (interactive_element_id, presentation_id, interactive_element_data, type, live, slide_number)
VALUES (1, 1, 'Test Questions', 'poll', FALSE , 1);


CREATE TABLE "interactions" (
  "interaction_id" SERIAL,
  "user_id" integer NOT NULL,
  "interactive_element_id" integer NOT NULL,
  "interaction_data" varchar(255),
  "time_created" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT interactions_pk PRIMARY KEY ("interaction_id")
) WITH (
OIDS=FALSE
);
INSERT INTO public.interactions (interaction_id, user_id, interactive_element_id, interaction_data)
VALUES (1, 1, 1, 'Test Data');


CREATE TABLE "jnct_users_modules" (
  "user_id" integer NOT NULL,
  "module_id" integer NOT NULL,
  "teacher" BOOLEAN,
  PRIMARY KEY (user_id, module_id)
) WITH (
OIDS=FALSE
);
INSERT INTO public.jnct_users_modules (user_id, module_id, teacher)
VALUES (1, 1, true);



CREATE TABLE "thumbnails" (
  "thumbnail_id" SERIAL,
  "presentation_id" integer NOT NULL,
  "thumbnail_url" varchar(255) NOT NULL,
  "slide_number" integer NOT NULL,
  CONSTRAINT thumbnails_pk PRIMARY KEY ("thumbnail_id")
) WITH (
OIDS=FALSE
);
INSERT INTO public.thumbnails (thumbnail_id, presentation_id, thumbnail_url, slide_number)
VALUES (1, 1,'httpsTest', 1);



ALTER TABLE "users" ADD CONSTRAINT "users_fk0" FOREIGN KEY ("active_presentation_id") REFERENCES "presentations"("presentation_id");


ALTER TABLE "presentations" ADD CONSTRAINT "presentations_fk0" FOREIGN KEY ("module_id") REFERENCES "modules"("module_id");

ALTER TABLE "questions" ADD CONSTRAINT "questions_fk0" FOREIGN KEY ("user_id") REFERENCES "users"("user_id");
ALTER TABLE "questions" ADD CONSTRAINT "questions_fk1" FOREIGN KEY ("presentation_id") REFERENCES "presentations"("presentation_id");

ALTER TABLE "interactive_elements" ADD CONSTRAINT "interactive_elements_fk0" FOREIGN KEY ("presentation_id") REFERENCES "presentations"("presentation_id");

ALTER TABLE "interactions" ADD CONSTRAINT "interactions_fk0" FOREIGN KEY ("user_id") REFERENCES "users"("user_id");
ALTER TABLE "interactions" ADD CONSTRAINT "interactions_fk1" FOREIGN KEY ("interactive_element_id") REFERENCES "interactive_elements"("interactive_element_id");

ALTER TABLE "jnct_users_modules" ADD CONSTRAINT "jnct_users_modules_fk0" FOREIGN KEY ("user_id") REFERENCES "users"("user_id") ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE "jnct_users_modules" ADD CONSTRAINT "jnct_users_modules_fk1" FOREIGN KEY ("module_id") REFERENCES "modules"("module_id") ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE "thumbnails" ADD CONSTRAINT "thumbnails_fk0" FOREIGN KEY ("presentation_id") REFERENCES "presentations"("presentation_id");


--Example SQL statement for junction table
SELECT *  FROM users JOIN jnct_users_modules USING(user_id);