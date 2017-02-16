package org.openlmis.notification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class NotificationService {

  @Autowired
  private JavaMailSender mailSender;

  /**
   * Send an email notification.
   *
   * @param from email address of the sender
   * @param to email address of the receiver
   * @param subject subject of the email
   * @param content content of the email
   * @throws MessagingException a generic messaging exception
   */
  public void sendNotification(String from, String to, String subject, String content)
      throws MessagingException {
    if (content == null) {
      throw new MessagingException("Content must not be null");
    }
    MimeMessage message = mailSender.createMimeMessage();

    MimeMessageHelper helper = new MimeMessageHelper(message, false);
    helper.setFrom(from);
    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(content);

    mailSender.send(message);
  }
}
