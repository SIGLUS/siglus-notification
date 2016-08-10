package org.openlmis.notification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

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
   * @param htmlContent html content of the email
   * @throws MessagingException a generic messaging exception
   */
  public void sendNotification(String from, String to, String subject,
                               String content, String htmlContent) throws MessagingException {
    if (content == null && htmlContent == null) {
      throw new MessagingException("Content must not be null");
    }
    MimeMessage message = mailSender.createMimeMessage();

    MimeMessageHelper helper = new MimeMessageHelper(message, false);
    helper.setFrom(from);
    helper.setTo(to);
    helper.setSubject(subject);

    // multipart/alternative (both plain text and html content are included)
    Multipart multipart = new MimeMultipart("alternative");

    if (content != null) {
      MimeBodyPart textPart = new MimeBodyPart();
      textPart.setContent(content, "text/plain");
      multipart.addBodyPart(textPart);
    }

    if (htmlContent != null) {
      MimeBodyPart htmlPart = new MimeBodyPart();
      htmlPart.setContent(htmlContent, "text/html");
      multipart.addBodyPart(htmlPart);
    }

    message.setContent(multipart);

    mailSender.send(message);
  }
}
