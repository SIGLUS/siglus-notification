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

import org.openlmis.notification.i18n.MessageKeys;
import org.openlmis.notification.web.BaseValidator;
import org.openlmis.util.NotificationRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class NotificationRequestValidator implements BaseValidator {

  @Override
  public boolean supports(Class<?> clazz) {
    return NotificationRequest.class.equals(clazz);
  }

  @Override
  public void validate(Object target, Errors errors) {
    verifyArguments(target, errors, MessageKeys.ERROR_NOTIFICATION_REQUEST_NULL);
    rejectIfEmptyOrWhitespace(errors, "from", MessageKeys.ERROR_FROM_REQUIRED);
    rejectIfEmptyOrWhitespace(errors, "to", MessageKeys.ERROR_TO_REQUIRED);
    rejectIfEmptyOrWhitespace(errors, "subject", MessageKeys.ERROR_SUBJECT_REQUIRED);
    rejectIfEmptyOrWhitespace(errors, "content", MessageKeys.ERROR_CONTENT_REQUIRED);
  }
}
