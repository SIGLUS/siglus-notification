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

package org.openlmis.notification.repository;

import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.notification.domain.Notification;
import org.openlmis.notification.service.NotificationChannel;
import org.openlmis.notification.util.NotificationDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.CrudRepository;

public class NotificationRepositoryIntegrationTest 
    extends BaseCrudRepositoryIntegrationTest<Notification> {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Autowired
  private NotificationRepository repository;

  @Override
  CrudRepository<Notification, UUID> getRepository() {
    return repository;
  }

  @Override
  Notification generateInstance() {
    return new NotificationDataBuilder().withEmptyMessage(NotificationChannel.EMAIL).build();
  }

  @Test
  public void shouldNotAllowDuplicateChannelsForNotification() {
    expectedException.expect(DataIntegrityViolationException.class);
    expectedException.expectMessage("unq_notification_messages_notificationid_channel");

    Notification notification = new NotificationDataBuilder()
        .withMessage(NotificationChannel.EMAIL, "Body", "Subject")
        .withMessage(NotificationChannel.EMAIL, "Body", "Subject")
        .build();
    repository.saveAndFlush(notification);
  }
}
