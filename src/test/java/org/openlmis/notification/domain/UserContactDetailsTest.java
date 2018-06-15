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

import java.util.UUID;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.openlmis.notification.testutils.ToStringTestUtils;
import org.openlmis.notification.web.UserContactDetailsDto;

public class UserContactDetailsTest {

  @Test
  public void shouldDefaultEmailVerifiedToTrue() {
    UserContactDetails details = UserContactDetails.newUserContactDetails(
        new UserContactDetailsDto(UUID.randomUUID(), null, null, null, null)
    );
    
    assertThat(details.getEmailVerified(), Matchers.is(equalTo(Boolean.TRUE)));
  }

  @Test
  public void shouldDefaultAllowNotifyToTrue() {
    UserContactDetails details = UserContactDetails.newUserContactDetails(
        new UserContactDetailsDto(UUID.randomUUID(), null, null, null, null)
    );

    assertThat(details.getAllowNotify(), Matchers.is(equalTo(Boolean.TRUE)));
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
