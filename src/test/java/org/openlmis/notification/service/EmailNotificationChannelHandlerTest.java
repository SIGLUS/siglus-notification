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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.UUID;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.notification.domain.UserContactDetails;
import org.openlmis.notification.service.referencedata.UserDto;
import org.openlmis.notification.service.referencedata.UserReferenceDataService;
import org.openlmis.notification.testutils.UserDataBuilder;
import org.openlmis.notification.util.UserContactDetailsDataBuilder;
import org.openlmis.notification.web.notification.MessageDto;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@RunWith(MockitoJUnitRunner.class)
public class EmailNotificationChannelHandlerTest {
  private static final UUID USER_ID = UUID.randomUUID();

  @Mock
  private JavaMailSender mailSender;

  @Mock
  private UserReferenceDataService userReferenceDataService;

  @InjectMocks
  private EmailNotificationChannelHandler handler = new EmailNotificationChannelHandler();

  @Captor
  private ArgumentCaptor<MimeMessage> mimeMessageCaptor;

  private UserContactDetails contactDetails = new UserContactDetailsDataBuilder()
      .withReferenceDataUserId(USER_ID)
      .build();
  private UserDto user = new UserDataBuilder().build();
  private MessageDto message = new MessageDto("subject", "body");
  private String from = "noreply@test.org";


  @Before
  public void setUp() {
    ReflectionTestUtils.setField(handler, "from", from);

    when(userReferenceDataService.findOne(USER_ID)).thenReturn(user);
    when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
  }

  @Test
  public void shouldSendMessage() throws MessagingException, IOException {
    handler.handle(contactDetails, message);
    verify(mailSender).send(mimeMessageCaptor.capture());

    MimeMessage value = mimeMessageCaptor.getValue();
    assertThat(value.getFrom()[0].toString(), is(from));
    assertThat(value.getAllRecipients()[0].toString(), is(contactDetails.getEmailAddress()));
    assertThat(value.getSubject(), is(message.getSubject()));
    assertThat(value.getContent().toString(), is(message.getBody()));
  }

  @Test
  public void shouldSendMessageToGivenEmail() throws MessagingException, IOException {
    String email = "example@test.org";
    handler.handle(email, message);
    verify(mailSender).send(mimeMessageCaptor.capture());

    MimeMessage value = mimeMessageCaptor.getValue();
    assertThat(value.getFrom()[0].toString(), is(from));
    assertThat(value.getAllRecipients()[0].toString(), is(email));
    assertThat(value.getSubject(), is(message.getSubject()));
    assertThat(value.getContent().toString(), is(message.getBody()));
  }

  @Test
  public void shouldNotSendMessageIfUserEmailIsNotVerified() {
    contactDetails.getEmailDetails().setEmailVerified(false);
    handler.handle(contactDetails, message);
    verifyZeroInteractions(mailSender);
  }

  @Test
  public void shouldNotSendMessageIfUserHasUnsetAllowNotifyFlag() {
    contactDetails.getEmailDetails().setEmailVerified(false);
    handler.handle(contactDetails, message);
    verifyZeroInteractions(mailSender);
  }

  @Test
  public void shouldNotSendMessageIfUserIsNotActive() {
    user.setActive(false);
    handler.handle(contactDetails, message);
    verifyZeroInteractions(mailSender);
  }

  @Test(expected = ServerException.class)
  public void shouldThrowExceptionIfMailCanNotBeSend() {
    doThrow(new MailSendException("test")).when(mailSender).send(any(MimeMessage.class));
    handler.handle(contactDetails, message);
  }
}
