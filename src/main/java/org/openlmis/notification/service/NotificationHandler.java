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

import static org.openlmis.notification.i18n.MessageKeys.ERROR_UNSUPPORTED_MESSAGE_TYPE;
import static org.openlmis.notification.i18n.MessageKeys.ERROR_USER_CONTACT_DETAILS_NOT_FOUND;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import org.openlmis.notification.domain.UserContactDetails;
import org.openlmis.notification.repository.UserContactDetailsRepository;
import org.openlmis.notification.web.NotFoundException;
import org.openlmis.notification.web.ValidationException;
import org.openlmis.notification.web.notification.MessageDto;
import org.openlmis.notification.web.notification.NotificationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationHandler {

  @Autowired
  private UserContactDetailsRepository userContactDetailsRepository;

  @Autowired
  private List<MessageHandler> handlers;

  /**
   * Handles the given notification.
   */
  public void handle(NotificationDto notification) {
    UserContactDetails contactDetails = userContactDetailsRepository
        .findOne(notification.getUserId());

    if (null == contactDetails) {
      throw new NotFoundException(ERROR_USER_CONTACT_DETAILS_NOT_FOUND);
    }

    for (Entry<String, MessageDto> entry : notification.getMessages().entrySet()) {
      MessageType messageType = getMessageType(entry.getKey());
      handlers
          .stream()
          .filter(item -> messageType.equals(item.getMessageType()))
          .findFirst()
          .ifPresent(item -> item.handle(contactDetails, entry.getValue()));
    }
  }

  private MessageType getMessageType(String value) {
    return Optional
        .ofNullable(MessageType.fromString(value))
        .orElseThrow(() -> new ValidationException(ERROR_UNSUPPORTED_MESSAGE_TYPE, value));
  }

}
