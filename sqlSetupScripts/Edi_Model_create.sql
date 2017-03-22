-- tables
-- Table: classes
CREATE TABLE classes (
    class_id int  NOT NULL,
    subject int  NOT NULL,
    size int  NOT NULL,
    teacher int  NOT NULL,
    students int  NOT NULL,
    presentation_library_presentation_id int  NOT NULL,
    CONSTRAINT classes_pk PRIMARY KEY (class_id)
);

-- Table: presentation_library
CREATE TABLE presentation_library (
    presentation_id int  NOT NULL,
    xml_location int  NOT NULL,
    classes int  NOT NULL,
    CONSTRAINT presentation_id PRIMARY KEY (presentation_id)
);

-- Table: users
CREATE TABLE users (
    user_id int  NOT NULL,
    first_name varchar(255)  NOT NULL,
    second_name varchar(255)  NOT NULL,
    login_name varchar(20) NOT NULL,
    password_hash varchar(255)  NOT NULL,
    password_salt varchar(255) NOT NULL,
    is_teacher boolean  NOT NULL,
    classes_class_id int  NOT NULL,
    CONSTRAINT users_pk PRIMARY KEY (user_id)
);

-- foreign keys
-- Reference: classes_presentation_library (table: classes)
ALTER TABLE classes ADD CONSTRAINT classes_presentation_library
    FOREIGN KEY (presentation_library_presentation_id)
    REFERENCES presentation_library (presentation_id)  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

-- Reference: users_classes (table: users)
ALTER TABLE users ADD CONSTRAINT users_classes
    FOREIGN KEY (classes_class_id)
    REFERENCES classes (class_id)  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

-- End of file.

