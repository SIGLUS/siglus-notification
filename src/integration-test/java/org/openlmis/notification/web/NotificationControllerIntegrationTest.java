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
import org.openlmis.notification.util.NotificationRequest;

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
    notificationRequest.setHtmlContent("<b>content</b>");

    restAssured.given()
      .queryParam("access_token", getToken())
      .body(notificationRequest)
      .when()
        .post(BASE_URL + "/notification")
      .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE , restAssured.getLastReport(), RamlMatchers.hasNoViolations());
  }
}
