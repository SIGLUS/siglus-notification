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

import static org.openlmis.notification.i18n.MessageKeys.EMAIL_VERIFICATION_SUCCESS;
import static org.openlmis.notification.i18n.MessageKeys.ERROR_ID_MISMATCH;
import static org.openlmis.notification.i18n.MessageKeys.ERROR_TOKEN_EXPIRED;
import static org.openlmis.notification.i18n.MessageKeys.ERROR_TOKEN_INVALID;
import static org.openlmis.notification.i18n.MessageKeys.ERROR_USER_CONTACT_DETAILS_NOT_FOUND;
import static org.openlmis.notification.i18n.MessageKeys.ERROR_USER_HAS_NO_EMAIL;
import static org.openlmis.notification.i18n.MessageKeys.ERROR_VERIFICATION_EMAIL_VERIFIED;

import java.util.UUID;
import org.openlmis.notification.domain.EmailDetails;
import org.openlmis.notification.domain.EmailVerificationToken;
import org.openlmis.notification.domain.UserContactDetails;
import org.openlmis.notification.i18n.Message;
import org.openlmis.notification.i18n.Message.LocalizedMessage;
import org.openlmis.notification.i18n.MessageService;
import org.openlmis.notification.repository.EmailVerificationTokenRepository;
import org.openlmis.notification.repository.UserContactDetailsRepository;
import org.openlmis.notification.service.EmailVerificationNotifier;
import org.openlmis.notification.service.PermissionService;
import org.openlmis.notification.service.UserContactDetailsService;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserContactDetailsController {

  @Autowired
  private UserContactDetailsRepository userContactDetailsRepository;

  @Autowired
  private EmailVerificationTokenRepository emailVerificationTokenRepository;

  @Autowired
  private EmailVerificationNotifier emailVerificationNotifier;

  @Autowired
  private PermissionService permissionService;

  @Autowired
  private UserContactDetailsDtoValidator validator;

  @Autowired
  private MessageService messageService;

  @Autowired
  private UserContactDetailsService userContactDetailsService;

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

    UserContactDetails userContactDetails = userContactDetailsRepository
        .findOne(referenceDataUserId);

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

    UserContactDetails contactDetails = fromDto(userContactDetailsDto);
    contactDetails = userContactDetailsService.addOrUpdate(contactDetails);

    return toDto(contactDetails);
  }

  /**
   * Get current pending verification email.
   */
  @GetMapping(value = "/userContactDetails/{id}/verifications")
  @ResponseBody
  public EmailVerificationTokenDto getVerifications(@PathVariable("id") UUID userId) {
    permissionService.canManageUserContactDetails(userId);
    UserContactDetails contactDetails = userContactDetailsRepository.findOne(userId);

    if (null == contactDetails) {
      throw new NotFoundException(ERROR_USER_CONTACT_DETAILS_NOT_FOUND);
    }

    EmailVerificationToken token = emailVerificationTokenRepository
        .findOneByUserContactDetails(contactDetails);
    return null == token
        ? null
        : new EmailVerificationTokenDto(token.getEmailAddress(), token.getExpiryDate());
  }

  /**
   * Generates token which can be used to verify user's email.
   */
  @RequestMapping(value = "/userContactDetails/{id}/verifications", method = RequestMethod.POST)
  @ResponseStatus(HttpStatus.OK)
  public void sendVerification(@PathVariable("id") UUID userId) {
    permissionService.canManageUserContactDetails(userId);

    UserContactDetails contactDetails = userContactDetailsRepository.findOne(userId);

    if (null == contactDetails) {
      throw new NotFoundException(ERROR_USER_CONTACT_DETAILS_NOT_FOUND);
    }

    EmailVerificationToken token = emailVerificationTokenRepository
        .findOneByUserContactDetails(contactDetails);

    if (null == token) {
      if (null == contactDetails.getEmailAddress()) {
        throw new ValidationException(ERROR_USER_HAS_NO_EMAIL);
      } else {
        throw new ValidationException(ERROR_VERIFICATION_EMAIL_VERIFIED);
      }
    }

    emailVerificationNotifier.sendNotification(contactDetails, token.getEmailAddress());
  }

  /**
   * Verify user email address.
   */
  @GetMapping(value = "/userContactDetails/{id}/verifications/{token}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public LocalizedMessage verifyContactDetail(@PathVariable("id") UUID userId,
      @PathVariable("token") UUID token) {
    EmailVerificationToken verificationToken = emailVerificationTokenRepository.findOne(token);

    if (verificationToken == null) {
      throw new ValidationException(ERROR_TOKEN_INVALID);
    }

    if (!userId.equals(verificationToken.getUserContactDetails().getReferenceDataUserId())) {
      throw new ValidationException(ERROR_ID_MISMATCH);
    }

    if (verificationToken.isExpired()) {
      throw new ValidationException(ERROR_TOKEN_EXPIRED);
    }

    UserContactDetails userContactDetails = userContactDetailsRepository.findOne(userId);
    userContactDetails.setEmailDetails(new EmailDetails(verificationToken.getEmailAddress(), true));

    userContactDetailsRepository.save(userContactDetails);
    emailVerificationTokenRepository.delete(token);

    return messageService
        .localize(new Message(EMAIL_VERIFICATION_SUCCESS, verificationToken.getEmailAddress()));
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
