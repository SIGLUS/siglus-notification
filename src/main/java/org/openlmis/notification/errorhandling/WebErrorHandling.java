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

package org.openlmis.notification.errorhandling;

import org.openlmis.notification.i18n.LocalizedMessage;
import org.openlmis.notification.web.ValidationMessageException;
import org.openlmis.util.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class WebErrorHandling extends BaseHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebErrorHandling.class);

  /**
   * Logs any exceptions that occur while sending notifications and returns proper response.
   *
   * @param ex An instance of Exception
   * @return ErrorResponse
   */
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public ErrorResponse handleException(Exception ex) {
    final String msg = "Unable to send notification";
    LOGGER.error(msg, ex);
    return new ErrorResponse(msg, ex.getMessage());
  }

  /**
   * Handles Message exceptions and returns status 400 Bad Request.
   *
   * @param ex the ValidationMessageException to handle
   * @return the error response for the user
   */
  @ExceptionHandler(ValidationMessageException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public LocalizedMessage handleMessageException(ValidationMessageException ex) {
    LOGGER.info(ex.getMessage());
    return getLocalizedMessage(ex.asMessage());
  }

}
