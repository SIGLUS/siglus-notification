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
