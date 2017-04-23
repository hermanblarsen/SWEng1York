CREATE TABLE "users" (
	"user_id" SERIAL,
	"user_type" varchar(255),
	"password" varchar(255),
	"first_name" varchar(255),
	"last_name" varchar(255),
	"username" varchar(255),
	"module_ids" integer,
	CONSTRAINT users_pk PRIMARY KEY ("user_id")
) WITH (
  OIDS=FALSE
);
INSERT INTO public.users (user_id)
VALUES (1);

CREATE TABLE "modules" (
	"module_id" SERIAL,
	"user_ids" integer,
	"teacher_ids" integer,
	"presentation_ids" integer,
	"description" varchar(255),
	"subject" varchar(255),
	"time_last_updated"  TIME,
	"time_created"  TIME DEFAULT CURRENT_TIME,
	CONSTRAINT modules_pk PRIMARY KEY ("module_id")
) WITH (
  OIDS=FALSE
);
INSERT INTO public.modules (module_id)
VALUES (1);

CREATE TABLE "presentations" (
	"presentation_id" SERIAL,
	"active_users" integer,
	"current_slide" integer,
	"xml_url" varchar(255),
	"live" BOOLEAN,
	"thumbnail_urls" varchar(255),
	"interactive_element_ids" integer,
	"question_ids" integer,
	CONSTRAINT presentations_pk PRIMARY KEY ("presentation_id")
) WITH (
  OIDS=FALSE
);
INSERT INTO public.presentations (presentation_id)
VALUES (1);

CREATE TABLE "questions" (
	"question_id" SERIAL,
	"user_id" integer,
	"time_created"  TIME DEFAULT CURRENT_TIME,
	"time_answered"  TIME,
	"question_data" varchar(255),
	"slide_number" integer,
	CONSTRAINT questions_pk PRIMARY KEY ("question_id")
) WITH (
  OIDS=FALSE
);
INSERT INTO public.questions (question_id)
VALUES (1);

CREATE TABLE "interactive_elements" (
	"interactive_element_id" SERIAL,
	"interactive_element_data" varchar(255),
	"type" varchar(255),
	"response_interval" INTERVAL,
	"live" BOOLEAN,
	"slide_number" integer,
	"interaction_ids" integer,
	CONSTRAINT interactive_elements_pk PRIMARY KEY ("interactive_element_id")
) WITH (
  OIDS=FALSE
);
INSERT INTO public.interactive_elements (interactive_element_id)
VALUES (1);

CREATE TABLE "interactions" (
	"interaction_id" SERIAL,
	"user_id" integer,
	"interaction_data" varchar(255),
	"time_created"  TIME DEFAULT CURRENT_TIME,
	CONSTRAINT interactions_pk PRIMARY KEY ("interaction_id")
) WITH (
  OIDS=FALSE
);
INSERT INTO public.interactions (interaction_id)
VALUES (1);

ALTER TABLE "users" ADD CONSTRAINT "users_fk0" FOREIGN KEY ("module_ids") REFERENCES "modules"("module_id");

ALTER TABLE "modules" ADD CONSTRAINT "modules_fk0" FOREIGN KEY ("user_ids") REFERENCES "users"("user_id");
ALTER TABLE "modules" ADD CONSTRAINT "modules_fk1" FOREIGN KEY ("teacher_ids") REFERENCES "users"("user_id");
ALTER TABLE "modules" ADD CONSTRAINT "modules_fk2" FOREIGN KEY ("presentation_ids") REFERENCES "presentations"("presentation_id");

ALTER TABLE "presentations" ADD CONSTRAINT "presentations_fk0" FOREIGN KEY ("active_users") REFERENCES "users"("user_id");
ALTER TABLE "presentations" ADD CONSTRAINT "presentations_fk1" FOREIGN KEY ("interactive_element_ids") REFERENCES "interactive_elements"("interactive_element_id");
ALTER TABLE "presentations" ADD CONSTRAINT "presentations_fk2" FOREIGN KEY ("interactive_element_ids") REFERENCES "interactive_elements"("interactive_element_id");
ALTER TABLE "presentations" ADD CONSTRAINT "presentations_fk3" FOREIGN KEY ("question_ids") REFERENCES "questions"("question_id");

ALTER TABLE "questions" ADD CONSTRAINT "questions_fk0" FOREIGN KEY ("user_id") REFERENCES "users"("user_id");

ALTER TABLE "interactive_elements" ADD CONSTRAINT "interactive_elements_fk0" FOREIGN KEY ("interaction_ids") REFERENCES "interactions"("interaction_id");

ALTER TABLE "interactions" ADD CONSTRAINT "interactions_fk0" FOREIGN KEY ("user_id") REFERENCES "users"("user_id");

