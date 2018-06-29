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
import org.openlmis.notification.domain.NotificationMessage;
import org.openlmis.notification.domain.UserContactDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationChannelHandler implements NotificationChannelHandler {

  @Autowired
  private JavaMailSender mailSender;

  @Value("${email.noreply}")
  private String from;

  @Override
  public NotificationChannel getNotificationChannel() {
    return NotificationChannel.EMAIL;
  }

  @Override
  public void handle(Boolean important, NotificationMessage message,
      UserContactDetails contactDetails) {
    if (shouldSendMessage(contactDetails, important)) {
      trySendEmail(contactDetails.getEmailAddress(), message);
    }
  }

  void handle(String email, NotificationMessage message) {
    trySendEmail(email, message);
  }

  private void trySendEmail(String email, NotificationMessage message) {
    try {
      sendMail(email, message);
    } catch (Exception exp) {
      throw new ServerException(exp, ERROR_SEND_MAIL_FAILURE);
    }
  }

  private void sendMail(String to, NotificationMessage message) throws MessagingException {
    MimeMessage mailMessage = mailSender.createMimeMessage();

    MimeMessageHelper helper = new MimeMessageHelper(mailMessage, false);
    helper.setFrom(from);
    helper.setTo(to);
    helper.setSubject(message.getSubject());
    helper.setText(message.getBody());

    mailSender.send(mailMessage);
  }

  private boolean shouldSendMessage(UserContactDetails contactDetails, Boolean important) {
    return (null != contactDetails 
        && contactDetails.isEmailAddressVerified()
        && (contactDetails.isAllowNotify() || isTrue(important)));
  }

}
