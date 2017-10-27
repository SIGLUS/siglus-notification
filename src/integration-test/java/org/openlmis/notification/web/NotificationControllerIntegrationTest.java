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

package org.openlmis.notification.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.openlmis.notification.i18n.MessageKeys.ERROR_CONTENT_REQUIRED;
import static org.openlmis.notification.i18n.MessageKeys.ERROR_FROM_REQUIRED;
import static org.openlmis.notification.i18n.MessageKeys.ERROR_SUBJECT_REQUIRED;
import static org.openlmis.notification.i18n.MessageKeys.ERROR_TO_REQUIRED;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.jayway.restassured.response.Response;

import org.junit.Test;
import org.openlmis.util.NotificationRequest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

import guru.nidi.ramltester.junit.RamlMatchers;

public class NotificationControllerIntegrationTest extends BaseWebIntegrationTest {
  private static final String NOTIFICATION_PATH = "/api/notification";
  private static final String FROM = "from@example.com";
  private static final String TO = "to@example.com";
  private static final String SUBJECT = "subject";
  private static final String CONTENT = "content";

  @MockBean
  private JavaMailSender javaMailSender;

  @Test
  public void shouldSendMessage() throws Exception {
    success(FROM, TO, SUBJECT, CONTENT);
  }

  @Test
  public void shouldNotSendMessageIfFromIsEmpty() throws Exception {
    fail(null, TO, SUBJECT, CONTENT, ERROR_FROM_REQUIRED);
  }

  @Test
  public void shouldNotSendMessageIfToIsEmpty() throws Exception {
    fail(FROM, null, SUBJECT, CONTENT, ERROR_TO_REQUIRED);
  }

  @Test
  public void shouldNotSendMessageIfSubjectIsEmpty() throws Exception {
    fail(FROM, TO, null, CONTENT, ERROR_SUBJECT_REQUIRED);
  }

  @Test
  public void shouldNotSendMessageIfContentIsEmpty() throws Exception {
    fail(FROM, TO, SUBJECT, null, ERROR_CONTENT_REQUIRED);
  }

  private void success(String from, String to, String subject, String content) throws Exception {
    DummyMessage message = new DummyMessage();

    given(javaMailSender.createMimeMessage()).willReturn(message);

    send(from, to, subject, content)
        .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    verify(javaMailSender).send(message);

    assertThat(message.getFrom().length, equalTo(1));
    assertThat(message.getFrom()[0].toString(), equalTo(from));

    assertThat(message.getTo().length, equalTo(1));
    assertThat(message.getTo()[0].toString(), equalTo(to));

    assertThat(message.getSubject(), equalTo(subject));
    assertThat(message.getContent(), equalTo(content));
  }

  private void fail(String from, String to, String subject, String content, String message)
      throws Exception {
    String response = send(from, to, subject, content)
        .then()
        .statusCode(400)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertThat(response, containsString(message));

    verifyZeroInteractions(javaMailSender);
  }

  private Response send(String from, String to, String subject, String content) {
    NotificationRequest notificationRequest = new NotificationRequest();
    notificationRequest.setFrom(from);
    notificationRequest.setTo(to);
    notificationRequest.setSubject(subject);
    notificationRequest.setContent(content);

    return restAssured
        .given()
        .header(AUTHORIZATION, getTokenHeader())
        .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
        .body(notificationRequest)
        .when()
        .post(NOTIFICATION_PATH);
  }

}
