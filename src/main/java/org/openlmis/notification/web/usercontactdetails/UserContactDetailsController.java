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

import static org.openlmis.notification.i18n.MessageKeys.ERROR_USER_CONTACT_DETAILS_NOT_FOUND;

import java.util.UUID;
import org.openlmis.notification.domain.UserContactDetails;
import org.openlmis.notification.repository.UserContactDetailsRepository;
import org.openlmis.notification.service.PermissionService;
import org.openlmis.notification.web.NotFoundException;
import org.openlmis.notification.web.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserContactDetailsController {

  @Autowired
  private UserContactDetailsRepository repository;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private UserContactDetailsDtoValidator validator;

  /**
   * Returns an instance of the {@link UserContactDetailsDto} class with the given reference data
   * user ID.
   *
   * @param referenceDataUserId  the reference data user ID
   * @return  the contact details of the user with the given ID
   */
  @GetMapping("/userContactDetails/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UserContactDetailsDto getUserContactDetails(@PathVariable("id") UUID referenceDataUserId) {
    permissionService.canManageUserContactDetails(referenceDataUserId);

    UserContactDetails userContactDetails = repository.findOne(referenceDataUserId);

    if (null == userContactDetails) {
      throw new NotFoundException(ERROR_USER_CONTACT_DETAILS_NOT_FOUND);
    }

    return toDto(userContactDetails);
  }

  /**
   * Creates or updates the user contact details for user with the given reference data ID.
   * 
   * @param referenceDataUserId  the reference data user ID
   * @param userContactDetailsDto  the update contact details
   * @return  the new or updated contact details of the user with the given ID
   */
  @PutMapping("/userContactDetails/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UserContactDetailsDto saveUpdateUserContactDetails(
      @PathVariable("id") UUID referenceDataUserId,
      @RequestBody UserContactDetailsDto userContactDetailsDto,
      BindingResult bindingResult) {
    permissionService.canManageUserContactDetails(referenceDataUserId);

    validator.validate(userContactDetailsDto, bindingResult);
    if (bindingResult.getErrorCount() > 0) {
      throw new ValidationException(bindingResult.getFieldError().getDefaultMessage());
    }

    UserContactDetails userContactDetails = repository.save(fromDto(userContactDetailsDto));

    return toDto(userContactDetails);
  }

  private UserContactDetailsDto toDto(UserContactDetails userContactDetails) {
    UserContactDetailsDto dto = new UserContactDetailsDto();

    userContactDetails.export(dto);

    return dto;
  }

  private UserContactDetails fromDto(UserContactDetailsDto userContactDetailsDto) {
    return UserContactDetails.newUserContactDetails(userContactDetailsDto);
  }

}
