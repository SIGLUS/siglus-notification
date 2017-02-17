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
import org.openlmis.util.ErrorResponse;
import org.openlmis.util.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;

@RestController
@RequestMapping("/api")
public class NotificationController {

  private static final Logger LOGGER = LoggerFactory.getLogger(NotificationController.class);

  @Autowired
  private NotificationService notificationService;

  /**
   * Send an email notification.
   * @param notificationRequest details of the message
   */
  @RequestMapping("/notification")
  @ResponseStatus(HttpStatus.OK)
  public void sendNotification(@RequestBody NotificationRequest notificationRequest)
      throws MessagingException {
    notificationService.sendNotification(notificationRequest.getFrom(),
        notificationRequest.getTo(), notificationRequest.getSubject(),
        notificationRequest.getContent());
  }

  /**
   * Logs any exceptions that occur while sending notifications and returns proper response.
   *
   * @param ex An instance of Exception
   * @return ErrorResponse
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public ErrorResponse handleException(Exception ex) {
    final String msg = "Unable to send notification";
    LOGGER.error(msg, ex);
    return new ErrorResponse(msg, ex.getMessage());
  }
}
