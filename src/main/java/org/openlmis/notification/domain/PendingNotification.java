/*
 * This program is part of the OpenLMIS logistics management information system platform software.
 * Copyright © 2017 VillageReach
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program. If not, see
 * http://www.gnu.org/licenses.  For additional information contact info@OpenLMIS.org.
 */

package org.openlmis.notification.domain;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.openlmis.notification.service.NotificationChannel;

@Getter
@Entity
@Table(name = "pending_notifications")
@NoArgsConstructor
public class PendingNotification implements Identifiable {
  private static final String NOTIFICATION_ID_COLUMN_NAME = "notificationId";

  @Id
  @Column(name = NOTIFICATION_ID_COLUMN_NAME, unique = true, nullable = false)
  private UUID id;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = NOTIFICATION_ID_COLUMN_NAME, unique = true, nullable = false)
  private Notification notification;

  @ElementCollection(fetch = FetchType.LAZY, targetClass = NotificationChannel.class)
  @CollectionTable(
      name = "pending_notification_channels",
      joinColumns = @JoinColumn(name = "pendingNotificationId")
  )
  @Column(name = "channel")
  @Enumerated(value = EnumType.STRING)
  private Set<NotificationChannel> channels;

  @Column(columnDefinition = "timestamp with time zone", nullable = false)
  @Getter
  private ZonedDateTime createdDate;

  /**
   * Sets default values before persisting the given object in a database.
   */
  @PrePersist
  public void setDefaultValues() {
    if (null == createdDate) {
      createdDate = ZonedDateTime.now();
    }
  }

  public PendingNotification(Notification notification) {
    this.notification = notification;
    this.channels = notification.getChannels();
  }

}
