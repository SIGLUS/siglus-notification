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

import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.notification.domain.DigestConfiguration;
import org.openlmis.notification.domain.DigestSubscription;
import org.openlmis.notification.testutils.DigestConfigurationDataBuilder;
import org.openlmis.notification.testutils.DigestSubscriptionDataBuilder;
import org.openlmis.notification.testutils.ToStringTestUtils;

public class DigestSubscriptionDtoTest {

  @Test
  public void shouldCreateInstanceBasedOnDomain() {
    // given
    String time = "time";
    DigestConfiguration configuration = new DigestConfigurationDataBuilder().build();
    DigestSubscription domain = new DigestSubscriptionDataBuilder()
        .withDigestConfiguration(configuration)
        .withTime(time)
        .build();

    // when
    DigestSubscriptionDto dto = DigestSubscriptionDto.newInstance(domain);

    // then
    assertThat(dto).isNotNull();
    assertThat(dto.getId()).isEqualTo(domain.getId());
    assertThat(dto.getDigestConfigurationTag()).isEqualTo(configuration.getTag());
    assertThat(dto.getTime()).isEqualTo(time);
  }

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(DigestSubscriptionDto.class)
        .suppress(Warning.NONFINAL_FIELDS)
        .withRedefinedSuperclass()
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    ToStringTestUtils.verify(DigestSubscriptionDto.class, new DigestSubscriptionDto());
  }

}
