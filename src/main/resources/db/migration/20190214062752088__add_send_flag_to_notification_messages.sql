-- WHEN COMMITTING OR REVIEWING THIS FILE: Make sure that the timestamp in the file name (that serves as a version) is the latest timestamp, and that no new migration have been added in the meanwhile.
-- Adding migrations out of order may cause this migration to never execute or behave in an unexpected way.
-- Migrations should NOT BE EDITED. Add a new migration to apply changes.

-- Since UPDATEs on large tables are not performant, we do this by creating a new table and copying the data.
-- See https://dba.stackexchange.com/questions/52517/best-way-to-populate-a-new-column-in-a-large-table/52531#52531
CREATE TABLE notification_messages_new AS
  SELECT
    nm.id AS "id",
    nm.notificationid AS "notificationid",
    nm.channel AS "channel",
    nm.body AS "body",
    nm.subject AS "subject",
    TRUE AS "send"
  FROM
    notification_messages nm;

ALTER TABLE notification_messages
  DROP CONSTRAINT unq_notification_messages_notificationid_channel;

ALTER TABLE notification_messages_new
  ADD CONSTRAINT pKey_notification_messages PRIMARY KEY (id),
  ADD CONSTRAINT fKey_notification_messages_notificationid FOREIGN KEY (notificationid) REFERENCES notifications(id),
  ADD CONSTRAINT unq_notification_messages_notificationid_channel UNIQUE (notificationid, channel),
  ALTER COLUMN send SET NOT NULL,
  ALTER COLUMN send SET DEFAULT FALSE;

-- Replace old table with the new one
DROP TABLE notification_messages;
ALTER TABLE notification_messages_new RENAME TO notification_messages;
