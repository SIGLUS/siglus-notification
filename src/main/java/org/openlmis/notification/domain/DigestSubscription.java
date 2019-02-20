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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "digest_subscriptions")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public final class DigestSubscription extends BaseEntity {

  @Getter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "userContactDetailsId", nullable = false)
  private UserContactDetails userContactDetails;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "digestConfigurationId", nullable = false)
  private DigestConfiguration digestConfiguration;

  private String time;

  /**
   * Exports current status of the object.
   */
  public void export(Exporter exporter) {
    exporter.setId(getId());
    exporter.setDigestConfiguration(digestConfiguration);
    exporter.setTime(time);
  }

  public interface Exporter {

    void setId(UUID id);

    void setDigestConfiguration(DigestConfiguration configuration);

    void setTime(String time);
  }

  public interface Importer {

    UUID getId();

    String getDigestConfigurationTag();

    String getTime();

  }

}