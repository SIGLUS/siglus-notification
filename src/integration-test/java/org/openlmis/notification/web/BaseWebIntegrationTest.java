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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.openlmis.notification.web.WireMockResponses.MOCK_CHECK_RESULT;
import static org.openlmis.notification.web.WireMockResponses.MOCK_TOKEN_REQUEST_RESPONSE;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.ObjectMapperConfig;
import com.jayway.restassured.config.RestAssuredConfig;

import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.RamlLoaders;
import guru.nidi.ramltester.restassured.RestAssuredClient;

import javax.annotation.PostConstruct;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseWebIntegrationTest {
  private static final String USER_ACCESS_TOKEN = "418c89c5-7f21-4cd1-a63a-38c47892b0fe";
  private static final String USER_ACCESS_TOKEN_HEADER = "Bearer " + USER_ACCESS_TOKEN;

  protected static final String RAML_ASSERT_MESSAGE =
      "HTTP request/response should match RAML definition.";

  protected static final String MESSAGE_KEY = "messageKey";

  protected RestAssuredClient restAssured;

  private static final RamlDefinition ramlDefinition =
      RamlLoaders.fromClasspath().load("api-definition-raml.yaml").ignoringXheaders();

  @Value("${service.url}")
  protected String baseUri;

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(80);

  @LocalServerPort
  private int randomPort;

  @Autowired
  private ObjectMapper objectMapper;

  /**
   * Constructor for test.
   */
  protected BaseWebIntegrationTest() {
    // This mocks the auth check to always return valid admin credentials.
    wireMockRule.stubFor(post(urlEqualTo("/api/oauth/check_token"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .withBody(MOCK_CHECK_RESULT)));

    // This mocks the auth token request response
    wireMockRule.stubFor(post(urlPathEqualTo("/api/oauth/token?grant_type=client_credentials"))
        .willReturn(aResponse()
            .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
            .withBody(MOCK_TOKEN_REQUEST_RESPONSE)));
  }

  /**
   * Initialize the REST Assured client. Done here and not in the constructor, so that randomPort is
   * available.
   */
  @PostConstruct
  public void init() {

    RestAssured.baseURI = baseUri;
    RestAssured.port = randomPort;
    RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
        new ObjectMapperConfig().jackson2ObjectMapperFactory((clazz, charset) -> objectMapper)
    );
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    restAssured = ramlDefinition.createRestAssured();
  }

  /**
   * Get a user access token. An arbitrary UUID string is returned and the tests assume it is a
   * valid one for an admin user.
   *
   * @return an access token
   */
  protected String getTokenHeader() {
    return USER_ACCESS_TOKEN_HEADER;
  }

}
