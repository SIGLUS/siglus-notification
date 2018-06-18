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

package org.openlmis.notification.i18n;

import java.util.Arrays;

public abstract class MessageKeys {
  private static final String DELIMITER = ".";

  private static final String PERMISSION = "permission";
  private static final String PERMISSIONS = PERMISSION + "s";
  private static final String MISSING = "missing";
  private static final String USER_CONTACT_DETAILS = "userContactDetails";

  private static final String AUTHENTICATION = "authentication";
  private static final String USER = "user";
  private static final String NOT_FOUND = "notFound";

  private static final String SERVICE_PREFIX = "notification";
  private static final String ERROR_PREFIX = SERVICE_PREFIX + ".error";
  private static final String NOTIFICATION_REQUEST = ERROR_PREFIX + ".notificationRequest";

  public static final String ERROR_CONTEXTUAL_STATE_NULL =
      ERROR_PREFIX + ".validation.contextualState.null";

  public static final String ERROR_USER_CONTACT_DETAILS_NOT_FOUND =
      ERROR_PREFIX + DELIMITER + USER_CONTACT_DETAILS + DELIMITER + NOT_FOUND;

  public static final String ERROR_NOTIFICATION_REQUEST_NULL = NOTIFICATION_REQUEST + ".null";
  public static final String ERROR_FROM_REQUIRED = NOTIFICATION_REQUEST + ".from.required";
  public static final String ERROR_TO_REQUIRED = NOTIFICATION_REQUEST + ".to.required";
  public static final String ERROR_SUBJECT_REQUIRED = NOTIFICATION_REQUEST + ".subject.required";
  public static final String ERROR_CONTENT_REQUIRED = NOTIFICATION_REQUEST + ".content.required";

  public static final String ERROR_EMAIL_INVALID = ERROR_PREFIX + ".users.email.invalid";
  public static final String ERROR_EMAIL_DUPLICATED = ERROR_PREFIX + ".users.email.duplicated";
  public static final String ERROR_SEND_REQUEST = ERROR_PREFIX + ".sendRequest";
  public static final String ERROR_CONSTRAINT = ERROR_PREFIX + ".constraint";

  public static final String ERROR_FIELD_IS_INVARIANT = ERROR_PREFIX + ".fieldIsInvariant";
  public static final String PERMISSION_MISSING = join(ERROR_PREFIX, PERMISSION, MISSING);
  public static final String PERMISSIONS_MISSING = join(ERROR_PREFIX, PERMISSIONS, MISSING);
  public static final String USER_NOT_FOUND =
      join(ERROR_PREFIX, AUTHENTICATION, USER, NOT_FOUND);

  private MessageKeys() {
    throw new UnsupportedOperationException();
  }

  private static String join(String... params) {
    return String.join(DELIMITER, Arrays.asList(params));
  }

}
