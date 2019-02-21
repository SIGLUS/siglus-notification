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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Test;
import org.openlmis.notification.domain.DigestConfiguration;
import org.openlmis.notification.domain.DigestSubscription;
import org.openlmis.notification.domain.UserContactDetails;
import org.openlmis.notification.testutils.DigestConfigurationDataBuilder;
import org.openlmis.notification.testutils.DigestSubscriptionDataBuilder;
import org.openlmis.notification.util.UserContactDetailsDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

public class DigestSubscriptionRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<DigestSubscription, UUID> {

  private static final int COUNT = 10;

  @Autowired
  private DigestSubscriptionRepository repository;

  @Autowired
  private UserContactDetailsRepository userContactDetailsRepository;

  @Autowired
  private DigestConfigurationRepository digestConfigurationRepository;

  private Map<UUID, List<DigestSubscription>> userSubscriptions;

  @Override
  CrudRepository<DigestSubscription, UUID> getRepository() {
    return repository;
  }

  @Override
  DigestSubscription generateInstance() {
    UserContactDetails userContactDetails = new UserContactDetailsDataBuilder().build();
    userContactDetailsRepository.saveAndFlush(userContactDetails);

    DigestConfiguration configuration = new DigestConfigurationDataBuilder().buildAsNew();
    digestConfigurationRepository.saveAndFlush(configuration);

    return new DigestSubscriptionDataBuilder()
        .withUserContactDetails(userContactDetails)
        .withDigestConfiguration(configuration)
        .buildAsNew();
  }

  @Before
  public void setUp() {
    userSubscriptions = IntStream
        .range(0, COUNT)
        .mapToObj(idx -> generateInstance())
        .peek(repository::save)
        .collect(Collectors.groupingBy(s -> s.getUserContactDetails().getId()));
  }

  @Test
  public void shouldFindUserSubscriptions() {
    // given
    Iterator<UUID> idIterator = userSubscriptions.keySet().iterator();
    UUID user1 = idIterator.next();
    idIterator.next();
    idIterator.next();
    UUID user2 = idIterator.next();

    // when
    List<DigestSubscription> subscriptions1 = repository.getUserSubscriptions(user1);
    List<DigestSubscription> subscriptions2 = repository.getUserSubscriptions(user2);

    // then
    assertThat(subscriptions1).hasSize(userSubscriptions.get(user1).size());
    assertThat(subscriptions2).hasSize(userSubscriptions.get(user2).size());
  }

  @Test
  public void shouldDeleteUserSubscriptions() {
    // given
    Iterator<UUID> idIterator = userSubscriptions.keySet().iterator();
    UUID user1 = idIterator.next();
    idIterator.next();
    idIterator.next();
    UUID user2 = idIterator.next();
    UUID user3 = idIterator.next();

    // when
    repository.deleteUserSubscriptions(user1);
    repository.deleteUserSubscriptions(user2);

    List<DigestSubscription> subscriptions1 = repository.getUserSubscriptions(user1);
    List<DigestSubscription> subscriptions2 = repository.getUserSubscriptions(user2);
    List<DigestSubscription> subscriptions3 = repository.getUserSubscriptions(user3);

    // then
    assertThat(subscriptions1).isEmpty();
    assertThat(subscriptions2).isEmpty();
    assertThat(subscriptions3).hasSize(userSubscriptions.get(user3).size());
  }

  @Test
  public void shouldReturnTrueIfUserSubscribesForDigestConfiguration() {
    for (List<DigestSubscription> subscriptions : userSubscriptions.values()) {
      // given
      DigestSubscription subscription = subscriptions.get(0);
      UserContactDetails contactDetails = subscription.getUserContactDetails();
      DigestConfiguration configuration = subscription.getDigestConfiguration();

      // when
      boolean exists = repository.existsBy(contactDetails.getId(), configuration);

      // then
      assertThat(exists).isTrue();
    }
  }

  @Test
  public void shouldReturnFalseIfUserSubscribesForDigestConfiguration() {
    // given
    Iterator<List<DigestSubscription>> valueIterator = userSubscriptions.values().iterator();
    DigestSubscription subscription1 = valueIterator.next().get(0);
    DigestSubscription subscription2 = valueIterator.next().get(0);

    // we take contact details from one subscription
    // and configuration from another
    UserContactDetails contactDetails = subscription1.getUserContactDetails();
    DigestConfiguration configuration = subscription2.getDigestConfiguration();

    // when
    boolean exists = repository.existsBy(contactDetails.getId(), configuration);

    // then
    assertThat(exists).isFalse();


  }
}
