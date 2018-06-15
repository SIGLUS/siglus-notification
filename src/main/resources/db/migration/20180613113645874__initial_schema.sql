CREATE TABLE user_contact_details (
    allownotify boolean DEFAULT true,
    email character varying(255) NOT NULL,
    emailVerified boolean DEFAULT false NOT NULL,
    phoneNumber CHARACTER VARYING(255),
    referencedatauserid uuid PRIMARY KEY
);

CREATE UNIQUE INDEX unq_contact_details_email
ON user_contact_details(email)