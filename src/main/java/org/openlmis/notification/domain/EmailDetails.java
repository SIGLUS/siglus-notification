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


import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Embeddable
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class EmailDetails {

  @Column(unique = true)
  @Getter
  @Setter
  private String email;

  @Column(nullable = false, columnDefinition = "boolean DEFAULT false")
  @Getter
  @Setter
  private Boolean emailVerified;

  private EmailDetails(Importer importer) {
    if (null != importer) {
      email = importer.getEmail();
      if (importer.getEmailVerified() == null) {
        emailVerified = Boolean.FALSE;
      } else {
        emailVerified = importer.getEmailVerified();
      }
    }
  }


  /**
   * Construct new email details based on an importer (DTO).
   *
   * @param importer importer (DTO) to use
   * @return new email details
   */
  static EmailDetails newEmailDetails(EmailDetails.Importer importer) {
    return new EmailDetails(importer);
  }

  public interface Exporter {
    void setEmail(String email);

    void setEmailVerified(Boolean emailVerified);
  }

  public interface Importer {
    String getEmail();

    Boolean getEmailVerified();
  }

  /**
   * Copy data from the given email details to the instance that implement
   * {@link Exporter} interface.
   */
  public void export(Exporter exporter) {
    exporter.setEmail(email);
    exporter.setEmailVerified(emailVerified);
  }
}
