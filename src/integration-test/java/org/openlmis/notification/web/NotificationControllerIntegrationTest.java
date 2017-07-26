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

import static org.junit.Assert.assertThat;

import com.jayway.restassured.RestAssured;
import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.junit.RamlMatchers;
import guru.nidi.ramltester.restassured.RestAssuredClient;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openlmis.util.NotificationRequest;
import org.springframework.http.HttpHeaders;

public class NotificationControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RAML_ASSERT_MESSAGE = "HTTP request/response should match RAML "
      + "definition.";

  private RamlDefinition ramlDefinition;
  private RestAssuredClient restAssured;

  /**
   * Prepare the test environment.
   */
  @Before
  public void setUp() {
    RestAssured.baseURI = BASE_URL;
    ramlDefinition = RamlLoaders.fromClasspath().load("api-definition-raml.yaml");
    restAssured = ramlDefinition.createRestAssured();
  }

  // TODO: find a fake smtp server
  @Ignore
  @Test
  public void testSendMessage() {
    NotificationRequest notificationRequest = new NotificationRequest();
    notificationRequest.setFrom("from@example.com");
    notificationRequest.setTo("to@example.com");
    notificationRequest.setSubject("subject");
    notificationRequest.setContent("content");

    restAssured.given()
      .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
      .body(notificationRequest)
      .when()
        .post(BASE_URL + "/notification")
      .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE , restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
