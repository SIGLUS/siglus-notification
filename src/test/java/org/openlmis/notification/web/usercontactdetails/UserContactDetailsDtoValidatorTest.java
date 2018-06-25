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
import static org.mockito.Mockito.when;
import static org.openlmis.notification.i18n.MessageKeys.ERROR_EMAIL_INVALID;
import static org.openlmis.notification.i18n.MessageKeys.ERROR_FIELD_IS_INVARIANT;
import static org.openlmis.notification.web.usercontactdetails.UserContactDetailsDtoValidator.EMAIL;
import static org.openlmis.notification.web.usercontactdetails.UserContactDetailsDtoValidator.EMAIL_VERIFIED;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openlmis.notification.domain.UserContactDetails;
import org.openlmis.notification.repository.UserContactDetailsRepository;
import org.openlmis.notification.util.UserContactDetailsDataBuilder;
import org.openlmis.notification.web.BaseValidatorTest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

@RunWith(MockitoJUnitRunner.class)
public class UserContactDetailsDtoValidatorTest extends BaseValidatorTest {

  @Mock
  private UserContactDetailsRepository repository;

  @InjectMocks
  private UserContactDetailsDtoValidator validator;

  private UserContactDetails contactDetails = new UserContactDetailsDataBuilder().build();
  private UserContactDetailsDto dto = new UserContactDetailsDto();

  private Errors errors;

  @Before
  public void setUp() throws Exception {
    contactDetails.export(dto);

    errors = new BeanPropertyBindingResult(dto, "userContactDetails");
  }

  @Test
  public void shouldValidate() {
    validator.validate(dto, errors);
    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  public void shouldNotRejectIfEmailDetailsNotExists() {
    dto.setEmailDetails((EmailDetailsDto) null);

    validator.validate(dto, errors);
    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  public void shouldRejectIfEmailAddressIsIncorrect() {
    dto.getEmailDetails().setEmail("incorrect_email_address");

    validator.validate(dto, errors);
    assertErrorMessage(errors, EMAIL, ERROR_EMAIL_INVALID);
  }

  @Test
  public void shouldNotRejectIfEmailDetailsNotExistsOnUpdate() {
    when(repository.findOne(contactDetails.getReferenceDataUserId()))
        .thenReturn(contactDetails);

    dto.setEmailDetails((EmailDetailsDto) null);

    validator.validate(dto, errors);
    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  public void shouldRejectIfEmailVerifiedFlagWasChanged() {
    when(repository.findOne(contactDetails.getReferenceDataUserId()))
        .thenReturn(contactDetails);

    dto.getEmailDetails().setEmailVerified(!contactDetails.isEmailAddressVerified());

    validator.validate(dto, errors);
    assertErrorMessage(errors, EMAIL_VERIFIED, ERROR_FIELD_IS_INVARIANT);
  }
}
