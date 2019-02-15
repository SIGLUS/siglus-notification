-- WHEN COMMITTING OR REVIEWING THIS FILE: Make sure that the timestamp in the file name (that serves as a version) is the latest timestamp, and that no new migration have been added in the meanwhile.
-- Adding migrations out of order may cause this migration to never execute or behave in an unexpected way.
-- Migrations should NOT BE EDITED. Add a new migration to apply changes.

CREATE TABLE pending_notifications (
  notificationId UUID PRIMARY KEY,
  createdDate timestamptz DEFAULT now(),
  CONSTRAINT fKey_pending_notifications_notifications
    FOREIGN KEY (notificationId)
    REFERENCES notifications(id)
);

CREATE TABLE pending_notification_channels (
  channel VARCHAR(255) NOT NULL,
  pendingNotificationId UUID NOT NULL,
  CONSTRAINT fKey_pending_notification_channels_pending_notifications
    FOREIGN KEY (pendingNotificationId)
    REFERENCES pending_notifications(notificationId)
);

CREATE UNIQUE INDEX pending_notification_channels_unique_idx
  ON pending_notification_channels (channel, pendingNotificationId);
