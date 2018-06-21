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

package org.openlmis.notification.web.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.notification.i18n.MessageKeys;
import org.openlmis.notification.web.notification.NotificationDto;
import org.openlmis.notification.web.notification.NotificationDtoValidator;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

public class NotificationDtoValidatorTest {
  private NotificationDtoValidator validator = new NotificationDtoValidator();
  private NotificationDto request = new NotificationDto(
      "from", UUID.randomUUID(), "subject", "content"
  );
  private Errors errors;

  @Before
  public void setUp() {
    errors = new BeanPropertyBindingResult(request, "request");
  }

  @Test
  public void shouldValidate() {
    validator.validate(request, errors);
    assertThat(errors.getErrorCount()).isEqualTo(0);
  }

  @Test
  public void shouldNotRejectIfFromIsEmpty() {
    request.setFrom(null);

    validator.validate(request, errors);
    assertThat(errors.getErrorCount()).isEqualTo(0);
  }

  @Test
  public void shouldRejectIfToIsEmpty() {
    request.setUserId(null);

    validator.validate(request, errors);
    assertErrorMessage(errors, "userId", MessageKeys.ERROR_USER_ID_REQUIRED);
  }

  @Test
  public void shouldRejectIfSubjectIsEmpty() {
    request.setSubject(null);

    validator.validate(request, errors);
    assertErrorMessage(errors, "subject", MessageKeys.ERROR_SUBJECT_REQUIRED);
  }

  @Test
  public void shouldRejectIfContentIsEmpty() {
    request.setContent(null);

    validator.validate(request, errors);
    assertErrorMessage(errors, "content", MessageKeys.ERROR_CONTENT_REQUIRED);
  }

  private void assertErrorMessage(Errors errors, String field, String expectedMessage) {
    assertThat(errors.hasFieldErrors(field)).as("There is no errors for field: " + field).isTrue();

    boolean match = errors.getFieldErrors(field)
        .stream()
        .anyMatch(e -> e.getField().equals(field) && e.getDefaultMessage().equals(expectedMessage));

    assertThat(match).as("There is no error with default message: " + expectedMessage).isTrue();
  }
}
