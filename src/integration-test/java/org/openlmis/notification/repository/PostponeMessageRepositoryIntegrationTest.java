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

import java.util.UUID;
import javax.persistence.EntityManager;
import org.junit.Test;
import org.openlmis.notification.domain.DigestConfiguration;
import org.openlmis.notification.domain.PostponeMessage;
import org.openlmis.notification.domain.UserContactDetails;
import org.openlmis.notification.testutils.DigestConfigurationDataBuilder;
import org.openlmis.notification.testutils.PostponeMessageDataBuilder;
import org.openlmis.notification.util.UserContactDetailsDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

public class PostponeMessageRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<PostponeMessage, UUID> {

  @Autowired
  private DigestConfigurationRepository digestConfigurationRepository;

  @Autowired
  private UserContactDetailsRepository userContactDetailsRepository;

  @Autowired
  private PostponeMessageRepository repository;

  @Autowired
  private EntityManager entityManager;

  @Override
  CrudRepository<PostponeMessage, UUID> getRepository() {
    return repository;
  }

  @Override
  PostponeMessage generateInstance() {
    UserContactDetails contactDetails = new UserContactDetailsDataBuilder().build();
    userContactDetailsRepository.save(contactDetails);

    DigestConfiguration configuration = new DigestConfigurationDataBuilder().buildAsNew();
    digestConfigurationRepository.save(configuration);

    return new PostponeMessageDataBuilder()
        .withConfiguration(configuration)
        .withUserId(contactDetails.getReferenceDataUserId())
        .buildAsNew();
  }

  @Test
  public void shouldNotRemoveNotificationWhenPendingNotificationWasRemoved() {
    // given
    PostponeMessage message = generateInstance();
    repository.save(message);

    UUID configurationId = message.getConfiguration().getId();

    // when
    repository.delete(message.getId());
    entityManager.flush();

    // then
    boolean configurationExists = digestConfigurationRepository.exists(configurationId);

    assertThat(configurationExists).isTrue();
  }

}
