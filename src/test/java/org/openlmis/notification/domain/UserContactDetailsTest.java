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

package org.openlmis.notification.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.UUID;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.openlmis.notification.testutils.ToStringTestUtils;
import org.openlmis.notification.util.UserContactDetailsDataBuilder;
import org.openlmis.notification.web.usercontactdetails.UserContactDetailsDto;

@SuppressWarnings("PMD.TooManyMethods")
public class UserContactDetailsTest {
  private UserContactDetails contactDetails = new UserContactDetailsDataBuilder().build();

  @Test
  public void shouldDefaultAllowNotifyToTrue() {
    UserContactDetails details = UserContactDetails.newUserContactDetails(
        new UserContactDetailsDto(UUID.randomUUID(), null, null, null)
    );

    assertThat(details.getAllowNotify(), Matchers.is(equalTo(Boolean.TRUE)));
  }

  @Test
  public void shouldReturnEmail() {
    assertThat(contactDetails.getEmailAddress(), is(contactDetails.getEmailDetails().getEmail()));
  }

  @Test
  public void shouldReturnNullIfEmailDetailsAreNotSet() {
    contactDetails.setEmailDetails(null);
    assertThat(contactDetails.getEmailAddress(), is(nullValue()));
  }

  @Test
  public void shouldSayEmailIsNotVerifiedIfEmailDetailsAreNotSet() {
    contactDetails.setEmailDetails(null);
    assertThat(contactDetails.isEmailAddressVerified(), is(false));
  }

  @Test
  public void shouldSayEmailIsNotVerifiedIfEmailIsNotSet() {
    contactDetails.getEmailDetails().setEmail(null);
    assertThat(contactDetails.isEmailAddressVerified(), is(false));
  }

  @Test
  public void shouldSayEmailIsNotVerifiedIfEmailVerifiedIsFalse() {
    contactDetails.getEmailDetails().setEmailVerified(false);
    assertThat(contactDetails.isEmailAddressVerified(), is(false));
  }

  @Test
  public void shouldSayEmailIsNotVerifiedIfEmailVerifiedIsNotSet() {
    contactDetails.getEmailDetails().setEmailVerified(null);
    assertThat(contactDetails.isEmailAddressVerified(), is(false));
  }

  @Test
  public void shouldSayEmailIsVerifiedIfEmailIsSetAndEmailVerifiedIsTrue() {
    assertThat(contactDetails.isEmailAddressVerified(), is(true));
  }

  @Test
  public void shouldSayNotificationIsEnabledIfFlagIsSet() {
    contactDetails.setAllowNotify(true);
    assertThat(contactDetails.isAllowNotify(), is(true));
  }

  @Test
  public void shouldSayNotificationIsDisabledIfFlagIsUnset() {
    contactDetails.setAllowNotify(false);
    assertThat(contactDetails.isAllowNotify(), is(false));
  }

  @Test
  public void shouldSayNotificationIsDisabledIfFlagIsNotSet() {
    contactDetails.setAllowNotify(null);
    assertThat(contactDetails.isAllowNotify(), is(false));
  }

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(UserContactDetails.class)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    ToStringTestUtils.verify(UserContactDetails.class, new UserContactDetails());
  }


}
