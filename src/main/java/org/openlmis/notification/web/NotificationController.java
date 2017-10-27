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

package org.openlmis.notification.web;

import org.openlmis.notification.service.NotificationService;
import org.openlmis.util.NotificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;

@RestController
@RequestMapping("/api")
public class NotificationController {

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private NotificationRequestValidator validator;

  /**
   * Send an email notification.
   *
   * @param notificationRequest details of the message
   */
  @PostMapping("/notification")
  @ResponseStatus(HttpStatus.OK)
  public void sendNotification(@RequestBody NotificationRequest notificationRequest,
                               BindingResult bindingResult) throws MessagingException {
    validator.validate(notificationRequest, bindingResult);

    if (bindingResult.getErrorCount() > 0) {
      throw new ValidationMessageException(bindingResult.getFieldError().getDefaultMessage());
    }

    notificationService.sendNotification(notificationRequest.getFrom(),
        notificationRequest.getTo(), notificationRequest.getSubject(),
        notificationRequest.getContent());
  }

}
