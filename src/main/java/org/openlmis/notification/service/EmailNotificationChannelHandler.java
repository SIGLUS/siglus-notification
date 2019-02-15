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

import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.openlmis.notification.service.NotificationChannelRouter.EMAIL_SEND_NOW_CHANNEL;
import static org.openlmis.notification.service.NotificationToSendRetriever.IMPORTANT_HEADER;
import static org.openlmis.notification.service.NotificationToSendRetriever.RECIPIENT_HEADER;
import static org.openlmis.notification.service.NotificationTransformer.NOTIFICATION_ID_HEADER;

import java.util.UUID;
import org.openlmis.notification.domain.NotificationMessage;
import org.openlmis.notification.domain.PendingNotification.PendingNotificationId;
import org.openlmis.notification.domain.UserContactDetails;
import org.openlmis.notification.repository.PendingNotificationRepository;
import org.openlmis.notification.repository.UserContactDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;

@MessageEndpoint
public class EmailNotificationChannelHandler {

  @Autowired
  private UserContactDetailsRepository userContactDetailsRepository;

  @Autowired
  private PendingNotificationRepository pendingNotificationRepository;

  @Autowired
  private EmailSender emailSender;

  /**
   * Tries to send a notification to a user by using email channel.
   */
  @ServiceActivator(inputChannel = EMAIL_SEND_NOW_CHANNEL)
  public void handle(NotificationMessage payload,
      @Header(RECIPIENT_HEADER) UUID recipient,
      @Header(NOTIFICATION_ID_HEADER) UUID notificationId,
      @Header(IMPORTANT_HEADER) Boolean important) {
    System.out.println("HANDLER: " + recipient + " " + important);
    UserContactDetails contactDetails = userContactDetailsRepository.findOne(recipient);

    if (shouldSendMessage(contactDetails, important)) {
      emailSender.sendMail(contactDetails.getEmailAddress(),
          payload.getSubject(), payload.getBody());
    }

    // in the end the pending notification should be always removed
    pendingNotificationRepository
        .delete(new PendingNotificationId(notificationId, NotificationChannel.EMAIL));
  }

  private boolean shouldSendMessage(UserContactDetails contactDetails, Boolean important) {
    if (!contactDetails.hasEmailAddress()) {
      return false;
    }

    if (isTrue(important)) {
      return true;
    }

    return contactDetails.isEmailAddressVerified() && contactDetails.isAllowNotify();
  }

}
