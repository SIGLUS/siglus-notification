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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.openlmis.notification.service.NotificationChannel.EMAIL;

import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.openlmis.notification.domain.Notification;
import org.openlmis.notification.domain.NotificationMessage;
import org.openlmis.notification.domain.PendingNotification.PendingNotificationId;
import org.openlmis.notification.domain.UserContactDetails;
import org.openlmis.notification.repository.PendingNotificationRepository;
import org.openlmis.notification.repository.UserContactDetailsRepository;
import org.openlmis.notification.util.NotificationDataBuilder;
import org.openlmis.notification.util.UserContactDetailsDataBuilder;

public class EmailNotificationChannelHandlerTest {

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private UserContactDetailsRepository userContactDetailsRepository;

  @Mock
  private PendingNotificationRepository pendingNotificationRepository;

  @Mock
  private EmailSender emailSender;

  @InjectMocks
  private EmailNotificationChannelHandler handler;

  private UserContactDetails contactDetails = new UserContactDetailsDataBuilder().build();
  private Notification notification = new NotificationDataBuilder()
      .withMessage(EMAIL, "body", "subject")
      .buildAsNew();
  private NotificationMessage message = notification.getMessages().get(0);

  private UUID recipient = contactDetails.getId();
  private UUID notificationId = notification.getId();

  @Before
  public void setUp() {
    given(userContactDetailsRepository.findOne(recipient)).willReturn(contactDetails);
  }

  @After
  public void tearDown() {
    verify(pendingNotificationRepository).delete(new PendingNotificationId(notificationId, EMAIL));
  }

  @Test
  public void shouldSendMessage() {
    // when
    handler.handle(message, recipient, notificationId, false);

    // then
    verify(emailSender)
        .sendMail(contactDetails.getEmailAddress(), message.getSubject(), message.getBody());
  }

  @Test
  public void shouldNotSendMessageIfUserEmailIsNotSet() {
    // given
    contactDetails.getEmailDetails().setEmail(null);

    // when
    handler.handle(message, recipient, notificationId, false);

    // then
    verifyZeroInteractions(emailSender);
  }

  @Test
  public void shouldNotSendMessageIfUserEmailIsNotVerified() {
    // given
    contactDetails.getEmailDetails().setEmailVerified(false);

    // when
    handler.handle(message, recipient, notificationId, false);

    // then
    verifyZeroInteractions(emailSender);
  }

  @Test
  public void shouldSentImportantMessageIfUsersEmailIsNotVerified() {
    // given
    contactDetails.getEmailDetails().setEmailVerified(false);

    // when
    handler.handle(message, recipient, notificationId, true);

    // then
    verify(emailSender)
        .sendMail(contactDetails.getEmailAddress(), message.getSubject(), message.getBody());
  }

}
