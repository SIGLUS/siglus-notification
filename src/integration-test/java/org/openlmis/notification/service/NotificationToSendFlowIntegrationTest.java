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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openlmis.notification.domain.Notification;
import org.openlmis.notification.domain.PendingNotification;
import org.openlmis.notification.domain.UserContactDetails;
import org.openlmis.notification.repository.NotificationRepository;
import org.openlmis.notification.repository.PendingNotificationRepository;
import org.openlmis.notification.repository.UserContactDetailsRepository;
import org.openlmis.notification.util.NotificationDataBuilder;
import org.openlmis.notification.util.UserContactDetailsDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(properties = { "notificationToSend.autoStartup=false" })
@Transactional
public class NotificationToSendFlowIntegrationTest {

  private static final String SUBJECT = "subject";
  private static final String BODY = "body";

  @MockBean
  private EmailSender emailSender;

  @Autowired
  private NotificationRepository notificationRepository;

  @Autowired
  private UserContactDetailsRepository userContactDetailsRepository;

  @Autowired
  private PendingNotificationRepository pendingNotificationRepository;

  @Autowired
  private NotificationToSendRetriever retriever;

  @Autowired
  @Qualifier(NotificationToSendRetriever.START_CHANNEL)
  private MessageChannel startChannel;

  private UserContactDetails contactDetails = new UserContactDetailsDataBuilder()
      .build();

  private Notification emailNotification = new NotificationDataBuilder()
      .withUserId(contactDetails.getId())
      .withMessage(NotificationChannel.EMAIL, BODY, SUBJECT)
      .buildAsNew();

  private PendingNotification pendingEmailNotification =
      new PendingNotification(emailNotification, NotificationChannel.EMAIL);

  @Before
  public void setUp() {
    userContactDetailsRepository.saveAndFlush(contactDetails);
    notificationRepository.saveAndFlush(emailNotification);
    pendingNotificationRepository.saveAndFlush(pendingEmailNotification);
  }

  @Test
  public void shouldSendEmail() {
    // given
    Message<Notification> message = retriever.retrieve();

    // when
    startChannel.send(message);

    // then
    verify(emailSender).sendMail(contactDetails.getEmailAddress(), SUBJECT, BODY);
    assertThat(pendingNotificationRepository.exists(pendingEmailNotification.getId())).isFalse();
  }
}
