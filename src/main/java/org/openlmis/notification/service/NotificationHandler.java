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

import static org.openlmis.notification.i18n.MessageKeys.ERROR_USER_CONTACT_DETAILS_NOT_FOUND;
import static org.openlmis.notification.i18n.MessageKeys.ERROR_USER_NOT_ACTIVE_OR_NOT_FOUND;

import java.util.List;
import org.openlmis.notification.domain.Notification;
import org.openlmis.notification.domain.NotificationMessage;
import org.openlmis.notification.domain.UserContactDetails;
import org.openlmis.notification.repository.UserContactDetailsRepository;
import org.openlmis.notification.service.referencedata.UserDto;
import org.openlmis.notification.service.referencedata.UserReferenceDataService;
import org.openlmis.notification.web.NotFoundException;
import org.openlmis.notification.web.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationHandler {

  @Autowired
  private UserContactDetailsRepository userContactDetailsRepository;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Autowired
  private List<NotificationChannelHandler> handlers;

  /**
   * Handles the given notification.
   */
  public void handle(Notification notification) {
    UserContactDetails contactDetails = userContactDetailsRepository
        .findOne(notification.getUserId());

    if (null == contactDetails) {
      throw new NotFoundException(ERROR_USER_CONTACT_DETAILS_NOT_FOUND);
    }

    UserDto user = userReferenceDataService.findOne(contactDetails.getReferenceDataUserId());
    if (null == user || !user.isActive()) {
      throw new ValidationException(ERROR_USER_NOT_ACTIVE_OR_NOT_FOUND);
    }

    for (NotificationMessage message : notification.getMessages()) {
      NotificationChannel notificationChannel = message.getChannel();
      handlers
          .stream()
          .filter(item -> notificationChannel.equals(item.getNotificationChannel()))
          .findFirst()
          .ifPresent(item -> item
              .handle(notification.getImportant(), message, contactDetails));
    }
  }
}
