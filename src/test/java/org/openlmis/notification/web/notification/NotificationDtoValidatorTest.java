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
import static org.openlmis.notification.i18n.MessageKeys.ERROR_NOTIFICATION_REQUEST_FIELD_REQUIRED;
import static org.openlmis.notification.i18n.MessageKeys.ERROR_NOTIFICATION_REQUEST_MESSAGES_EMPTY;

import org.junit.Before;
import org.junit.Test;
import org.openlmis.notification.util.NotificationDataBuilder;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

public class NotificationDtoValidatorTest {
  private NotificationDtoValidator validator = new NotificationDtoValidator();
  private NotificationDto request = new NotificationDataBuilder()
      .withMessage("email", "subject", "body")
      .build();
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
  public void shouldRejectIfUserIdIsNull() {
    request.setUserId(null);

    validator.validate(request, errors);
    assertErrorMessage(errors, "userId", ERROR_NOTIFICATION_REQUEST_FIELD_REQUIRED);
  }

  @Test
  public void shouldRejectIfMessagesAreNotSet() {
    request = new NotificationDataBuilder().build();

    validator.validate(request, errors);
    assertErrorMessage(errors, "messages", ERROR_NOTIFICATION_REQUEST_MESSAGES_EMPTY);
  }

  @Test
  public void shouldRejectIfMessageBodyIsEmpty() {
    request = new NotificationDataBuilder()
        .withEmptyMessage("sms")
        .build();

    validator.validate(request, errors);
    assertErrorMessage(errors, "messages", ERROR_NOTIFICATION_REQUEST_FIELD_REQUIRED);
  }

  private void assertErrorMessage(Errors errors, String field, String expectedMessage) {
    assertThat(errors.hasFieldErrors(field)).as("There is no errors for field: " + field).isTrue();

    boolean match = errors.getFieldErrors(field)
        .stream()
        .anyMatch(e -> field.equals(e.getField()) && expectedMessage.equals(e.getDefaultMessage()));

    assertThat(match).as("There is no error with default message: " + expectedMessage).isTrue();
  }
}
