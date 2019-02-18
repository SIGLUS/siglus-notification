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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.persistence.EntityManager;
import org.assertj.core.util.Lists;
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
  private EntityManager entityManager;

  @Autowired
  @Qualifier(NotificationToSendRetriever.START_CHANNEL)
  private MessageChannel startChannel;

  private List<UserContactDetails> userContactDetails = Lists.newArrayList();
  private List<Notification> emailNotifications = Lists.newArrayList();

  private List<PendingNotification> pendingEmailNotifications = Lists.newArrayList();

  @Before
  public void setUp() {
    userContactDetails = IntStream
        .range(0, 3)
        .mapToObj(idx -> new UserContactDetailsDataBuilder().build())
        .collect(Collectors.toList());

    emailNotifications = userContactDetails
        .stream()
        .map(contactDetail -> new NotificationDataBuilder()
            .withUserId(contactDetail.getId())
            .withMessage(NotificationChannel.EMAIL, BODY, SUBJECT)
            .buildAsNew())
        .collect(Collectors.toList());

    pendingEmailNotifications = emailNotifications
        .stream()
        .map(notification -> new PendingNotification(notification, NotificationChannel.EMAIL))
        .collect(Collectors.toList());


    userContactDetailsRepository.deleteAll();
    userContactDetailsRepository.save(userContactDetails);

    notificationRepository.deleteAll();
    notificationRepository.save(emailNotifications);

    pendingNotificationRepository.deleteAll();
    pendingNotificationRepository.save(pendingEmailNotifications);

    entityManager.flush();
  }

  @Test
  public void shouldSendEmail() {
    // given
    Message<Notification> message = retriever.retrieve();
    UserContactDetails contactDetails = userContactDetails
        .stream()
        .filter(details -> Objects.equals(message.getPayload().getUserId(), details.getId()))
        .findFirst()
        .orElse(null);
    PendingNotification pendingNotification = pendingEmailNotifications
        .stream()
        .filter(item -> Objects.equals(message.getPayload().getId(), item.getNotificationId()))
        .findFirst()
        .orElse(null);

    // when
    assert null != contactDetails;
    assert null != pendingNotification;

    startChannel.send(message);

    // then
    verify(emailSender).sendMail(contactDetails.getEmailAddress(), SUBJECT, BODY);

    assertThat(pendingNotificationRepository.exists(pendingNotification.getId())).isFalse();

    assertThat(userContactDetailsRepository.count()).isEqualTo(userContactDetails.size());
    assertThat(notificationRepository.count()).isEqualTo(emailNotifications.size());
    assertThat(pendingNotificationRepository.count())
        .isEqualTo(pendingEmailNotifications.size() - 1);
  }

}
