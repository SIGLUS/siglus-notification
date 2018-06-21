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

package org.openlmis.notification.web.notification;

import javax.mail.MessagingException;
import org.apache.commons.lang3.StringUtils;
import org.openlmis.notification.domain.UserContactDetails;
import org.openlmis.notification.repository.UserContactDetailsRepository;
import org.openlmis.notification.service.NotificationService;
import org.openlmis.notification.service.referencedata.UserReferenceDataService;
import org.openlmis.notification.web.ValidationException;
import org.openlmis.util.NotificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NotificationController {

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private UserContactDetailsRepository userContactDetailsRepository;

  @Autowired
  private NotificationRequestValidator notificationRequestValidator;

  @Autowired
  private NotificationDtoValidator notificationValidator;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Value("${email.noreply}")
  private String defaultFrom;

  /**
   * Send an email notification.
   *
   * @param notificationRequest details of the message
   */
  @PostMapping("/notification")
  @ResponseStatus(HttpStatus.OK)
  public void sendNotificationRequest(@RequestBody NotificationRequest notificationRequest,
      BindingResult bindingResult) throws MessagingException {
    notificationRequestValidator.validate(notificationRequest, bindingResult);

    if (bindingResult.getErrorCount() > 0) {
      throw new ValidationException(bindingResult.getFieldError().getDefaultMessage());
    }

    notificationService.sendNotification(notificationRequest.getFrom(),
        notificationRequest.getTo(), notificationRequest.getSubject(),
        notificationRequest.getContent());
  }

  /**
   * Send an email notification.
   *
   * @param notification details of the message
   */
  @PostMapping("/v2/notification")
  @ResponseStatus(HttpStatus.OK)
  public void sendNotification(@RequestBody NotificationDto notification,
      BindingResult bindingResult) throws MessagingException {
    notificationValidator.validate(notification, bindingResult);

    if (bindingResult.getErrorCount() > 0) {
      throw new ValidationException(bindingResult.getFieldError().getDefaultMessage());
    }

    UserContactDetails contactDetails = userContactDetailsRepository
        .findOne(notification.getUserId());

    if (canBeNotified(contactDetails)) {
      String from = StringUtils.defaultIfBlank(notification.getFrom(), defaultFrom);

      notificationService.sendNotification(
          from, contactDetails.getEmailAddress(),
          notification.getSubject(), notification.getContent()
      );
    }
  }

  private boolean canBeNotified(UserContactDetails contactDetails) {
    if (null == contactDetails
        || !contactDetails.isEmailVerified()
        || !contactDetails.isAllowNotify()) {
      return false;
    }

    return userReferenceDataService
        .findOne(contactDetails.getReferenceDataUserId())
        .isActive();
  }

}
