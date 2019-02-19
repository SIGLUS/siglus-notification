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

package org.openlmis.notification.web.subscription;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.openlmis.notification.domain.DigestConfiguration;
import org.openlmis.notification.domain.DigestSubscription;
import org.openlmis.notification.web.BaseDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class DigestSubscriptionDto
    extends BaseDto
    implements DigestSubscription.Importer, DigestSubscription.Exporter {

  private String tag;
  private String time;

  static DigestSubscriptionDto newInstance(DigestSubscription domain) {
    DigestSubscriptionDto dto = new DigestSubscriptionDto();
    domain.export(dto);

    return dto;
  }

  @Override
  @JsonIgnore
  public void setDigestConfiguration(DigestConfiguration configuration) {
    this.tag = configuration.getTag();
  }

  @Override
  @JsonIgnore
  public String getDigestConfigurationTag() {
    return tag;
  }
}
