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
import static org.openlmis.notification.service.NotificationToSendRetriever.IMPORTANT_HEADER;
import static org.openlmis.notification.service.NotificationToSendRetriever.RECIPIENT_HEADER;
import static org.openlmis.notification.service.NotificationToSendRetriever.START_CHANNEL;

import java.util.UUID;
import org.openlmis.notification.domain.UserContactDetails;
import org.openlmis.notification.repository.UserContactDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.Filter;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.messaging.handler.annotation.Header;

@MessageEndpoint
public class AllowNotifyFilter {

  static final String ALLOW_NOTIFY_CHANNEL = "notificationToSend.allowNotify";

  @Autowired
  private UserContactDetailsRepository userContactDetailsRepository;

  /**
   * Checks if user should get a notification.
   */
  @Filter(inputChannel = START_CHANNEL, outputChannel = ALLOW_NOTIFY_CHANNEL)
  public boolean test(@Header(RECIPIENT_HEADER) UUID recipient,
      @Header(IMPORTANT_HEADER) Boolean important) {
    System.out.println("ALLOW_NOTIFY_FILTER: " + recipient + " " + important);
    UserContactDetails userContactDetails = userContactDetailsRepository.findOne(recipient);

    return isTrue(important) || userContactDetails.isAllowNotify();
  }

}
