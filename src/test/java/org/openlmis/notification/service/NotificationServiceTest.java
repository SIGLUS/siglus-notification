package org.openlmis.notification.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.notification.service.NotificationService;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

@RunWith(MockitoJUnitRunner.class)
public class NotificationServiceTest {

  @Mock
  private JavaMailSender javaMailSender;

  @InjectMocks
  private NotificationService service = new NotificationService();

  private Session session;

  @Test
  public void shouldSendMessage() throws MessagingException {
    MimeMessage mimeMessage = new MimeMessage(session);
    when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    service.sendNotification("from@example.com", "to@example.com", "subject", "content");
    verify(javaMailSender).send(mimeMessage);
  }
}
