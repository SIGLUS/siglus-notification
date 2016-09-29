package org.openlmis.notification.web;

import org.openlmis.notification.service.NotificationService;
import org.openlmis.notification.util.ErrorResponse;
import org.openlmis.notification.util.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;

@RestController
public class NotificationController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceNameController.class);

  @Autowired
  private NotificationService notificationService;

  /**
   * Send an email notification.
   * @param notificationRequest details of the message
   */
  @RequestMapping("/notification")
  @ResponseStatus(HttpStatus.OK)
  public void sendNotification(@RequestBody NotificationRequest notificationRequest) throws MessagingException {
      notificationService.sendNotification(notificationRequest.getFrom(),
          notificationRequest.getTo(), notificationRequest.getSubject(),
          notificationRequest.getContent(), notificationRequest.getHtmlContent());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse handleException(Exception ex) {
    final String msg = "Unable to send notification";
    LOGGER.error(msg, ex);
    return new ErrorResponse(msg, ex.getMessage());
  }
}
