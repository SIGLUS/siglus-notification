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

package org.openlmis.notification.web.usercontactdetails;

import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.notification.i18n.MessageKeys;
import org.openlmis.notification.testutils.ToStringTestUtils;
import org.openlmis.notification.web.ValidationException;
import org.springframework.util.LinkedMultiValueMap;

public class UserContactDetailsSearchParamsTest {
  @Rule
  public ExpectedException exception = ExpectedException.none();

  private LinkedMultiValueMap<String, String> queryMap;

  @Before
  public void setUp() {
    queryMap = new LinkedMultiValueMap<>();
  }

  @Test
  public void shouldGetEmailValueFromParameters() {
    queryMap.add(UserContactDetailsSearchParams.EMAIL, "test@example.org");
    UserContactDetailsSearchParams params = new UserContactDetailsSearchParams(queryMap);

    assertThat(params.getEmail()).isEqualTo("test@example.org");
  }

  @Test
  public void shouldGetNullIfMapHasNoEmailProperty() {
    UserContactDetailsSearchParams params = new UserContactDetailsSearchParams(queryMap);
    assertThat(params.getEmail()).isNull();
  }

  @Test
  public void shouldThrowExceptionIfThereIsUnknownParameterInParameters() {
    exception.expect(ValidationException.class);
    exception.expectMessage(MessageKeys.ERROR_USER_CONTACT_DETAILS_SEARCH_INVALID_PARAMS);

    queryMap.add("some-param", "some-value");
    new UserContactDetailsSearchParams(queryMap);
  }

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(UserContactDetailsSearchParams.class)
        .suppress(Warning.NONFINAL_FIELDS)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    queryMap.add(UserContactDetailsSearchParams.EMAIL, "test@example.org");
    UserContactDetailsSearchParams params = new UserContactDetailsSearchParams(queryMap);

    ToStringTestUtils.verify(UserContactDetailsSearchParams.class, params,
        "EMAIL", "ALL_PARAMETERS");
  }
}
