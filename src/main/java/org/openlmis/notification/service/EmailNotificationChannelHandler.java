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
import static org.openlmis.notification.i18n.MessageKeys.ERROR_SEND_MAIL_FAILURE;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.openlmis.notification.domain.UserContactDetails;
import org.openlmis.notification.service.referencedata.UserReferenceDataService;
import org.openlmis.notification.web.notification.MessageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationChannelHandler implements NotificationChannelHandler {

  @Autowired
  private JavaMailSender mailSender;

  @Autowired
  private UserReferenceDataService userReferenceDataService;

  @Value("${email.noreply}")
  private String from;

  @Override
  public NotificationChannel getNotificationChannel() {
    return NotificationChannel.EMAIL;
  }

  @Override
  public void handle(UserContactDetails contactDetails, MessageDto message) {
    if (shouldSentMessage(contactDetails, message)) {
      trySendEmail(contactDetails.getEmailAddress(), message);
    }
  }

  void handle(String email, MessageDto message) {
    trySendEmail(email, message);
  }

  private void trySendEmail(String email, MessageDto message) {
    try {
      sendMail(email, message);
    } catch (Exception exp) {
      throw new ServerException(exp, ERROR_SEND_MAIL_FAILURE);
    }
  }

  private void sendMail(String to, MessageDto message) throws MessagingException {
    MimeMessage mailMessage = mailSender.createMimeMessage();

    MimeMessageHelper helper = new MimeMessageHelper(mailMessage, false);
    helper.setFrom(from);
    helper.setTo(to);
    helper.setSubject(message.getSubject());
    helper.setText(message.getBody());

    mailSender.send(mailMessage);
  }

  private boolean shouldSentMessage(UserContactDetails contactDetails, MessageDto message) {
    if (null == contactDetails || !contactDetails.isEmailAddressVerified()) {
      return false;
    }

    boolean isActive = isTrue(userReferenceDataService
        .findOne(contactDetails.getReferenceDataUserId())
        .isActive());

    if (!isActive) {
      return false;
    }

    return contactDetails.isAllowNotify() || isTrue(message.getImportant());
  }

}
