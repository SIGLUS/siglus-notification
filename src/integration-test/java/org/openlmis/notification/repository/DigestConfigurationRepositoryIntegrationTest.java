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

package org.openlmis.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.notification.domain.DigestConfiguration;
import org.openlmis.notification.testutils.DigestConfigurationDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

public class DigestConfigurationRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<DigestConfiguration, UUID> {

  @Autowired
  private DigestConfigurationRepository repository;

  @Override
  CrudRepository<DigestConfiguration, UUID> getRepository() {
    return repository;
  }

  @Override
  DigestConfiguration generateInstance() {
    return new DigestConfigurationDataBuilder().buildAsNew();
  }

  private List<DigestConfiguration> configurations;

  @Before
  public void setUp() {
    configurations = IntStream
        .range(0, 10)
        .mapToObj(idx -> generateInstance())
        .peek(repository::save)
        .collect(Collectors.toList());
  }

  @Test
  public void shouldFindDigestConfigurationByTagInCollection() {
    // given
    Set<String> tags = Sets.newHashSet(
        configurations.get(0).getTag(),
        configurations.get(5).getTag(),
        configurations.get(9).getTag());

    // when
    List<DigestConfiguration> found = repository.findByTagIn(tags);

    assertThat(found)
        .hasSize(tags.size())
        .contains(configurations.get(0))
        .contains(configurations.get(5))
        .contains(configurations.get(9));
  }

}
