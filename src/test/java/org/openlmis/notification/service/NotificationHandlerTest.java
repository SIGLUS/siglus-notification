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

package org.openlmis.notification.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.notification.domain.Notification;
import org.openlmis.notification.domain.UserContactDetails;
import org.openlmis.notification.repository.UserContactDetailsRepository;
import org.openlmis.notification.util.NotificationDataBuilder;
import org.openlmis.notification.util.UserContactDetailsDataBuilder;
import org.openlmis.notification.web.NotFoundException;
import org.openlmis.notification.web.ValidationException;
import org.openlmis.notification.web.notification.MessageDto;
import org.openlmis.notification.web.notification.NotificationDto;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class NotificationHandlerTest {

  @Mock
  private UserContactDetailsRepository userContactDetailsRepository;

  @Mock
  private NotificationChannelHandler notificationChannelHandler;

  @InjectMocks
  private NotificationHandler notificationHandler;

  private UserContactDetails contactDetails = new UserContactDetailsDataBuilder().build();
  private MessageDto message = new MessageDto("subject", "body");
  private NotificationDto notification = new NotificationDto();

  @Before
  public void setUp() {
    Notification notificationObj = new NotificationDataBuilder()
        .withUserId(contactDetails.getId())
        .withMessage(NotificationChannel.EMAIL, "body", "subject")
        .build();
    notificationObj.export(notification);
    
    when(userContactDetailsRepository.findOne(notification.getUserId()))
        .thenReturn(contactDetails);
    when(notificationChannelHandler.getNotificationChannel()).thenReturn(NotificationChannel.EMAIL);

    ReflectionTestUtils
        .setField(notificationHandler, "handlers", Lists.newArrayList(notificationChannelHandler));
  }

  @Test
  public void shouldHandleNotification() {
    notificationHandler.handle(notification);
    verify(userContactDetailsRepository).findOne(notification.getUserId());
    verify(notificationChannelHandler).getNotificationChannel();
    verify(notificationChannelHandler).handle(notification.getImportant(), message, contactDetails);
  }

  @Test(expected = NotFoundException.class)
  public void shouldThrowExceptionIfContactDetailsDoesNotExist() {
    when(userContactDetailsRepository.findOne(notification.getUserId()))
        .thenReturn(null);
    notificationHandler.handle(notification);
  }

  @Test(expected = ValidationException.class)
  public void shouldThrowExceptionIfMessageTypeDoesNotExist() {
    notification = new NotificationDto();
    notification.setUserId(contactDetails.getId());
    notification.addMessage("sms", new MessageDto());
    notificationHandler.handle(notification);
  }

}
