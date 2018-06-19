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

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;

@Entity
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_contact_details", schema = "notification")
public class UserContactDetails implements Identifiable {

  @Getter
  @Setter
  @Id
  @Type(type = "pg-uuid")
  @Column(nullable = false, unique = true)
  private UUID referenceDataUserId;

  @Getter
  @Setter
  private String phoneNumber;

  @Column(columnDefinition = "boolean DEFAULT true")
  @Getter
  @Setter
  private Boolean allowNotify;

  @Getter
  @Setter
  @Embedded
  private EmailDetails emailDetails;

  private UserContactDetails(Importer importer) {
    referenceDataUserId = importer.getReferenceDataUserId();
    phoneNumber = importer.getPhoneNumber();
    emailDetails = EmailDetails.newEmailDetails(importer.getEmailDetails());

    if (importer.getAllowNotify() == null) {
      allowNotify = Boolean.TRUE;
    } else {
      allowNotify = importer.getAllowNotify();
    }
  }

  /**
   * Construct new user contact details based on an importer (DTO).
   *
   * @param importer importer (DTO) to use
   * @return new user contact details
   */
  public static UserContactDetails newUserContactDetails(Importer importer) {
    return new UserContactDetails(importer);
  }

  /**
   * Export this object to the specified exporter (DTO).
   *
   * @param exporter exporter to export to
   */
  public void export(Exporter exporter) {
    exporter.setReferenceDataUserId(referenceDataUserId);
    exporter.setAllowNotify(allowNotify);
    exporter.setPhoneNumber(phoneNumber);
    if (this.getEmailDetails() != null) {
      exporter.setEmailDetails(emailDetails);
    }
  }

  @Override
  public UUID getId() {
    return getReferenceDataUserId();
  }

  public interface Exporter {

    void setReferenceDataUserId(UUID id);

    void setPhoneNumber(String phoneNumber);

    void setAllowNotify(Boolean allowNotify);

    void setEmailDetails(EmailDetails emailDetails);
  }

  public interface Importer {

    UUID getReferenceDataUserId();

    String getPhoneNumber();

    Boolean getAllowNotify();

    EmailDetails.Importer getEmailDetails();
  }
}
