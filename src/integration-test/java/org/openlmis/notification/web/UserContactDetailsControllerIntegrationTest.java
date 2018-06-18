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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openlmis.notification.i18n.MessageKeys.ERROR_EMAIL_DUPLICATED;
import static org.openlmis.notification.i18n.MessageKeys.ERROR_EMAIL_INVALID;
import static org.openlmis.notification.i18n.MessageKeys.ERROR_FIELD_IS_INVARIANT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.jayway.restassured.response.Response;
import guru.nidi.ramltester.junit.RamlMatchers;
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.notification.domain.UserContactDetails;
import org.openlmis.notification.repository.UserContactDetailsRepository;
import org.openlmis.notification.service.PermissionService;
import org.openlmis.notification.util.UserContactDetailsDataBuilder;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;

@SuppressWarnings({"PMD.TooManyMethods"})
public class UserContactDetailsControllerIntegrationTest extends BaseWebIntegrationTest {

  private static final String RESOURCE_URL = "/api/userContactDetails";
  private static final String ID_RESOURCE_URL = RESOURCE_URL + "/{id}";
  private static final String ID = "id";

  @MockBean
  private UserContactDetailsRepository repository;

  @MockBean
  private PermissionService permissionService;

  private UserContactDetails userContactDetails;

  @Before
  public void setUp() {
    userContactDetails = new UserContactDetailsDataBuilder().build();
  }

  @Test
  public void shouldGetUserContactDetails() {
    when(repository.findOne(eq(userContactDetails.getReferenceDataUserId())))
        .thenReturn(userContactDetails);
    doNothing().when(permissionService)
        .canManageUserContactDetails(eq(userContactDetails.getReferenceDataUserId()));

    get(userContactDetails.getReferenceDataUserId())
        .then()
        .statusCode(200);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    verify(repository).findOne(eq(userContactDetails.getReferenceDataUserId()));
    verify(permissionService)
        .canManageUserContactDetails(eq(userContactDetails.getReferenceDataUserId()));
  }

  @Test
  public void shouldReturnNotFoundWhenTryingToFetchNonExistentUserContactDetails() {
    when(repository.findOne(eq(userContactDetails.getReferenceDataUserId())))
        .thenReturn(null);

    get(userContactDetails.getReferenceDataUserId())
        .then()
        .statusCode(404);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    verify(repository).findOne(eq(userContactDetails.getReferenceDataUserId()));
    verify(permissionService)
        .canManageUserContactDetails(eq(userContactDetails.getReferenceDataUserId()));
  }

  @Test
  public void shouldReturnForbiddenWhenTryingToFetchUserContactDetailsWithoutPermissions() {
    doThrow(new MissingPermissionException())
        .when(permissionService)
        .canManageUserContactDetails(userContactDetails.getReferenceDataUserId());

    get(userContactDetails.getReferenceDataUserId())
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    verify(repository, never()).findOne(any(UUID.class));
    verify(permissionService)
        .canManageUserContactDetails(eq(userContactDetails.getReferenceDataUserId()));    
  }

  @Test
  public void shouldCreateUserContactDetails() {
    UserContactDetailsDto request = toDto(userContactDetails);

    when(repository.save(userContactDetails)).thenReturn(userContactDetails);

    UserContactDetailsDto response = put(toDto(userContactDetails))
        .then()
        .statusCode(200)
        .extract()
        .as(UserContactDetailsDto.class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertEquals(request, response);

    verify(repository).save(eq(userContactDetails));
  }

  @Test
  public void shouldUpdateUserContactDetails() {
    UserContactDetails existing = new UserContactDetailsDataBuilder()
        .withReferenceDataUserId(userContactDetails.getReferenceDataUserId())
        .build();

    when(repository.findOne(userContactDetails.getReferenceDataUserId())).thenReturn(existing);
    when(repository.save(userContactDetails)).thenReturn(userContactDetails);

    UserContactDetailsDto request = toDto(userContactDetails);
    UserContactDetailsDto response = put(toDto(userContactDetails))
        .then()
        .statusCode(200)
        .extract()
        .as(UserContactDetailsDto.class);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertEquals(request, response);

    verify(repository).save(eq(userContactDetails));
    verify(repository).findOne(userContactDetails.getReferenceDataUserId());
  }

  @Test
  public void shouldReturnForbiddenWhenTryingToSaveUserContactDetailsWithoutPermissions() {
    doThrow(new MissingPermissionException())
        .when(permissionService)
        .canManageUserContactDetails(userContactDetails.getReferenceDataUserId());

    put(toDto(userContactDetails))
        .then()
        .statusCode(403);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());

    verify(repository, never()).save(any(UserContactDetails.class));
    verify(permissionService)
        .canManageUserContactDetails(eq(userContactDetails.getReferenceDataUserId()));
  }

  @Test
  public void shouldReturnBadRequestWhenTryingToChangeIsEmailVerifiedFlag() {
    UserContactDetails existing = new UserContactDetailsDataBuilder()
        .withReferenceDataUserId(userContactDetails.getReferenceDataUserId())
        .withUnverifiedFlag()
        .build();

    when(repository.findOne(eq(userContactDetails.getReferenceDataUserId())))
        .thenReturn(existing);

    String response = put(toDto(userContactDetails))
        .then()
        .statusCode(400)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertThat(response, containsString(ERROR_FIELD_IS_INVARIANT));

    verify(repository, never()).save(any(UserContactDetails.class));
    verify(permissionService)
        .canManageUserContactDetails(eq(userContactDetails.getReferenceDataUserId()));
  }

  @Test
  public void shouldReturnBadRequestWhenTryingToSetEmailThatIsAlreadyInUseByOtherUser() {
    doThrow(new DataIntegrityViolationException("",
        new ConstraintViolationException("", null, "unq_contact_details_email"))
    ).when(repository).save(userContactDetails);

    String response = put(toDto(userContactDetails))
        .then()
        .statusCode(400)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertThat(response, containsString(ERROR_EMAIL_DUPLICATED));

    verify(repository).save(userContactDetails);
    verify(permissionService)
        .canManageUserContactDetails(eq(userContactDetails.getReferenceDataUserId()));
  }

  @Test
  public void shouldReturnBardRequestWhenTryingToSaveUserContactDetailsWithInvalidEmail() {
    userContactDetails.setEmail("someDefinitelyInvalidEmail");

    String response = put(toDto(userContactDetails))
        .then()
        .statusCode(400)
        .extract()
        .path(MESSAGE_KEY);

    assertThat(RAML_ASSERT_MESSAGE, restAssured.getLastReport(), RamlMatchers.hasNoViolations());
    assertThat(response, containsString(ERROR_EMAIL_INVALID));

    verify(repository, never()).save(any(UserContactDetails.class));
    verify(permissionService)
        .canManageUserContactDetails(eq(userContactDetails.getReferenceDataUserId()));
  }

  private Response put(UserContactDetailsDto dto) {
    return restAssured
        .given()
        .header(AUTHORIZATION, getTokenHeader())
        .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
        .body(dto)
        .when()
        .pathParam(ID, dto.getReferenceDataUserId())
        .put(ID_RESOURCE_URL);
  }

  private Response get(UUID referenceDataUserId) {
    return restAssured
        .given()
        .header(HttpHeaders.AUTHORIZATION, getTokenHeader())
        .contentType(APPLICATION_JSON_VALUE)
        .pathParam(ID, referenceDataUserId)
        .when()
        .get(ID_RESOURCE_URL);
  }

  private UserContactDetailsDto toDto(UserContactDetails userContactDetails) {
    UserContactDetailsDto dto = new UserContactDetailsDto();
    userContactDetails.export(dto);
    return dto;
  }

}
