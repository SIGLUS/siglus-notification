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

package org.openlmis.notification.util;

import java.util.UUID;
import org.openlmis.notification.domain.UserContactDetails;

@SuppressWarnings("PMD.TooManyMethods")
public class UserContactDetailsDataBuilder {
  private static int instanceNumber = 0;

  private UUID referenceDataUserId = UUID.randomUUID();
  private String email;
  private String phoneNumber = "000-000-000";
  private Boolean verified = true;
  private Boolean allowNotify = true;


  /**
   * Builds instance of {@link UserContactDetailsDataBuilder} with sample data.
   */
  public UserContactDetailsDataBuilder() {
    instanceNumber++;

    email = instanceNumber + "example@mail.com";
  }

  public UserContactDetailsDataBuilder withReferenceDataUserId(UUID referenceDataUserId) {
    this.referenceDataUserId = referenceDataUserId;
    return this;
  }

  public UserContactDetailsDataBuilder withUnverifiedFlag() {
    this.verified = false;
    return this;
  }

  public UserContactDetailsDataBuilder withEmail(String email) {
    this.email = email;
    return this;
  }

  public UserContactDetailsDataBuilder withVerified(boolean verified) {
    this.verified = verified;
    return this;
  }

  /**
   * Builds instance of {@link UserContactDetails}.
   */
  public UserContactDetails build() {
    return new UserContactDetails(
        referenceDataUserId, email, phoneNumber,  verified, allowNotify
    );
  }

}
