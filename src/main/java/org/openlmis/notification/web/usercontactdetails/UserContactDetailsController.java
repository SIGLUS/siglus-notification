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
import static org.openlmis.notification.i18n.MessageKeys.ERROR_USER_EMAIL_ALREADY_VERIFIED;
import static org.openlmis.notification.i18n.MessageKeys.ERROR_USER_HAS_NO_EMAIL;

import java.util.UUID;
import org.openlmis.notification.domain.EmailDetails;
import org.openlmis.notification.domain.EmailVerificationToken;
import org.openlmis.notification.domain.UserContactDetails;
import org.openlmis.notification.i18n.ExposedMessageSource;
import org.openlmis.notification.repository.EmailVerificationTokenRepository;
import org.openlmis.notification.repository.UserContactDetailsRepository;
import org.openlmis.notification.service.EmailVerificationNotifier;
import org.openlmis.notification.service.PermissionService;
import org.openlmis.notification.web.NotFoundException;
import org.openlmis.notification.web.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
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
  private ExposedMessageSource messageSource;

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

    return userContactDetailsRepository.exists(userContactDetailsDto.getReferenceDataUserId())
        ? updateUserContactDetails(userContactDetailsDto)
        : addUserContactDetails(userContactDetailsDto);
  }

  private UserContactDetailsDto addUserContactDetails(UserContactDetailsDto toSave) {
    toSave.getEmailDetails().setEmailVerified(false);

    UserContactDetails saved = userContactDetailsRepository
        .save(fromDto(toSave));

    if (saved.hasEmailAddress() && saved.isNotEmailVerified()) {
      emailVerificationNotifier.sendNotification(saved, saved.getEmailAddress());
    }

    return toDto(saved);
  }

  private UserContactDetailsDto updateUserContactDetails(UserContactDetailsDto toUpdate) {
    EmailDetails newEmailDetails = null;

    UserContactDetails toSave = fromDto(toUpdate);
    UserContactDetails existing = userContactDetailsRepository
        .findOne(toUpdate.getReferenceDataUserId());

    if (toSave.hasEmailAddress() && !toSave.getEmailAddress().equals(existing.getEmailAddress())) {
      newEmailDetails = toSave.getEmailDetails();
      toSave.setEmailDetails(new EmailDetails(
          existing.getEmailAddress(), existing.isEmailVerified()
      ));
    }

    UserContactDetails saved = userContactDetailsRepository.save(toSave);
    EmailVerificationToken token = emailVerificationTokenRepository
        .findOneByUserContactDetails(saved);

    if (null != newEmailDetails
        && (null == token || !token.getEmailAddress().equals(newEmailDetails.getEmail()))) {
      emailVerificationNotifier.sendNotification(existing, newEmailDetails.getEmail());
    }

    return toDto(saved);
  }

  /**
   * Get current pending verification email.
   */
  @GetMapping(value = "/userContactDetails/{id}/verifications")
  @ResponseBody
  public EmailVerificationTokenDto getVerifications(@PathVariable("id") UUID userId) {
    permissionService.canManageUserContactDetails(userId);
    UserContactDetails contactDetails = userContactDetailsRepository.findOne(userId);
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

    EmailVerificationToken existsToken = emailVerificationTokenRepository
        .findOneByUserContactDetails(contactDetails);

    if (null == existsToken) {
      if (null == contactDetails.getEmailAddress()) {
        throw new ValidationException(ERROR_USER_HAS_NO_EMAIL);
      } else {
        throw new ValidationException(ERROR_USER_EMAIL_ALREADY_VERIFIED);
      }
    }

    emailVerificationNotifier.sendNotification(contactDetails, existsToken.getEmailAddress());
  }

  /**
   * Verify user email address.
   */
  @GetMapping(value = "/userContactDetails/{id}/verifications/{token}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public String verifyContactDetail(@PathVariable("id") UUID id,
      @PathVariable("token") UUID token) {
    EmailVerificationToken details = emailVerificationTokenRepository.findOne(token);

    if (details == null) {
      throw new ValidationException(ERROR_TOKEN_INVALID);
    }

    if (!id.equals(details.getUserContactDetails().getId())) {
      throw new ValidationException(ERROR_ID_MISMATCH);
    }

    if (details.isExpired()) {
      throw new ValidationException(ERROR_TOKEN_EXPIRED);
    }

    UserContactDetails userContactDetails = userContactDetailsRepository.findOne(id);
    userContactDetails.setEmailDetails(new EmailDetails(details.getEmailAddress(), true));

    userContactDetailsRepository.save(userContactDetails);
    emailVerificationTokenRepository.delete(token);

    return messageSource.getMessage(
        EMAIL_VERIFICATION_SUCCESS,
        new Object[]{details.getEmailAddress()},
        LocaleContextHolder.getLocale()
    );
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
