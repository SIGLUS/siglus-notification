package org.openlmis.notification.web;

import org.openlmis.notification.service.NotificationService;
import org.openlmis.notification.util.ErrorResponse;
import org.openlmis.notification.util.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;

@RestController
public class NotificationController {

  Logger logger = LoggerFactory.getLogger(ServiceNameController.class);

  @Autowired
  private NotificationService notificationService;

  /**
   * Send an email notification.
   * @param notificationRequest details of the message
   */
  @RequestMapping("/notification")
  public ResponseEntity<?> sendNotification(@RequestBody NotificationRequest notificationRequest) {
    try {
      notificationService.sendNotification(notificationRequest.getFrom(),
          notificationRequest.getTo(), notificationRequest.getSubject(),
          notificationRequest.getContent(), notificationRequest.getHtmlContent());
      return new ResponseEntity<>(HttpStatus.OK);
    } catch (MessagingException | MailAuthenticationException | IllegalArgumentException ex) {
      ErrorResponse errorResponse = new ErrorResponse(
          "An error occurred while sending notification to " + notificationRequest.getTo(),
          ex.getMessage());
      logger.error(errorResponse.getMessage(), ex);
      return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
  }
}
