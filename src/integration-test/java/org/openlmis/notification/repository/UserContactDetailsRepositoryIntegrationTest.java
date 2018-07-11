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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.notification.domain.UserContactDetails;
import org.openlmis.notification.testutils.UserContactDetailsSearchParamsDataBuilder;
import org.openlmis.notification.util.EmailDetailsDataBuilder;
import org.openlmis.notification.util.UserContactDetailsDataBuilder;
import org.openlmis.notification.web.usercontactdetails.UserContactDetailsSearchParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.repository.CrudRepository;

public class UserContactDetailsRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<UserContactDetails> {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Autowired
  private UserContactDetailsRepository repository;

  @Override
  CrudRepository<UserContactDetails, UUID> getRepository() {
    return repository;
  }

  @Override
  protected void assertBefore(UserContactDetails instance) {
    assertThat(instance.getId()).isNotNull();
  }

  @Override
  UserContactDetails generateInstance() {
    return generateInstance(getNextInstanceNumber());
  }

  private UserContactDetails generateInstance(int instanceNumber) {
    return new UserContactDetailsDataBuilder()
        .withEmailDetails(new EmailDetailsDataBuilder()
            .withEmail("test" + instanceNumber + "@integration.test.org")
            .build())
        .build();
  }

  @Before
  public void setUp() {
    repository.deleteAllInBatch();
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldNotAllowCreatingMultipleUserContactDetailsWithTheSameEmail() {
    repository.saveAndFlush(
        new UserContactDetailsDataBuilder()
            .withEmailDetails(
                new EmailDetailsDataBuilder()
                  .withEmail("duplicated@email.com")
                  .build()
            )
            .build()
    );

    repository.saveAndFlush(
        new UserContactDetailsDataBuilder()
            .withEmailDetails(
                new EmailDetailsDataBuilder()
                    .withEmail("duplicated@email.com")
                    .build()
            )
            .build()
    );
  }

  @Test
  public void shouldFindByEmail() {
    UserContactDetails expected = repository.save(generateInstance());
    Pageable pageable = new PageRequest(0, 1000);

    IntStream
        .range(0, 20)
        .forEach(idx -> repository.save(generateInstance()));

    UserContactDetailsSearchParams searchParams = new UserContactDetailsSearchParamsDataBuilder()
        .withEmail(expected.getEmailAddress())
        .build();
    Page<UserContactDetails> actual = repository.search(searchParams, pageable);
    assertThat(actual.getTotalElements()).isEqualTo(1L);
    assertThat(actual.getContent()).contains(expected);
  }

  @Test
  public void shouldFindById() {
    UserContactDetails expected = repository.save(generateInstance());
    Pageable pageable = new PageRequest(0, 1000);

    IntStream
        .range(0, 20)
        .forEach(idx -> repository.save(generateInstance()));

    UserContactDetailsSearchParams searchParams = new UserContactDetailsSearchParamsDataBuilder()
        .withId(expected.getReferenceDataUserId())
        .build();
    Page<UserContactDetails> actual = repository.search(searchParams, pageable);
    assertThat(actual.getTotalElements()).isEqualTo(1L);
    assertThat(actual.getContent()).contains(expected);
  }

  @Test
  public void shouldFindByPartOfEmail() {
    final List<UserContactDetails> contactDetails = IntStream
        .range(0, 50)
        .mapToObj(idx -> repository.save(generateInstance(idx)))
        .collect(Collectors.toList());
    Pageable pageable = new PageRequest(0, 1000);

    // find test1, test10, test11, test12, etc.
    UserContactDetailsSearchParams searchParams = new UserContactDetailsSearchParamsDataBuilder()
        .withEmail("test1")
        .build();
    Page<UserContactDetails> actual = repository.search(searchParams, pageable);
    assertThat(actual.getTotalElements()).isEqualTo(11L);
    assertThat(actual.getContent())
        .extracting(UserContactDetails::getEmailAddress)
        .contains("test1@integration.test.org", "test10@integration.test.org",
            "test11@integration.test.org", "test12@integration.test.org",
            "test13@integration.test.org", "test14@integration.test.org",
            "test15@integration.test.org", "test16@integration.test.org",
            "test17@integration.test.org", "test18@integration.test.org",
            "test19@integration.test.org");

    // find test3, test30, test31, test32, etc.
    searchParams = new UserContactDetailsSearchParamsDataBuilder()
        .withEmail("test3")
        .build();
    actual = repository.search(searchParams, pageable);
    assertThat(actual.getTotalElements()).isEqualTo(11L);
    assertThat(actual.getContent())
        .extracting(UserContactDetails::getEmailAddress)
        .contains("test3@integration.test.org", "test30@integration.test.org",
            "test31@integration.test.org", "test32@integration.test.org",
            "test33@integration.test.org", "test34@integration.test.org",
            "test35@integration.test.org", "test36@integration.test.org",
            "test37@integration.test.org", "test38@integration.test.org",
            "test39@integration.test.org");

    searchParams = new UserContactDetailsSearchParamsDataBuilder()
        .withEmail("@integration")
        .build();
    actual = repository.search(searchParams, pageable);
    assertThat(actual.getTotalElements()).isEqualTo(50L);
    assertThat(actual.getContent()).containsAll(contactDetails);

    searchParams = new UserContactDetailsSearchParamsDataBuilder()
        .withEmail("test.org")
        .build();
    actual = repository.search(searchParams, pageable);
    assertThat(actual.getTotalElements()).isEqualTo(50L);
    assertThat(actual.getContent()).containsAll(contactDetails);
  }

  @Test
  public void shouldFindByIdAndEmail() {
    final List<UserContactDetails> contactDetails = IntStream
        .range(0, 50)
        .mapToObj(idx -> repository.save(generateInstance(idx)))
        .collect(Collectors.toList());
    List<UserContactDetails> firstThirty = contactDetails.subList(0, 30);
    Pageable pageable = new PageRequest(0, 1000);

    UserContactDetailsSearchParams searchParams = new UserContactDetailsSearchParamsDataBuilder()
        .withIds(firstThirty.stream().map(UserContactDetails::getId).collect(Collectors.toSet()))
        .withEmail("1@integration.test.org")
        .build();
    Page<UserContactDetails> actual = repository.search(searchParams, pageable);

    assertThat(actual.getTotalElements()).isEqualTo(3L);
    assertThat(actual.getContent())
        .extracting(UserContactDetails::getEmailAddress)
        .contains("test1@integration.test.org", "test11@integration.test.org",
            "test21@integration.test.org");
  }

  @Test
  public void searchShouldSortByProperties() {
    IntStream
        .range(100, 105)
        .forEach(idx -> repository.save(generateInstance(idx)));

    Pageable pageable = new PageRequest(0, 10, Direction.ASC, "emailDetails.email");
    UserContactDetailsSearchParams searchParams = new UserContactDetailsSearchParamsDataBuilder()
        .build();
    Page<UserContactDetails> actual = repository.search(searchParams, pageable);

    assertThat(actual.getTotalElements()).isEqualTo(5L);
    assertThat(actual.getContent())
        .extracting(UserContactDetails::getEmailAddress)
        .contains("test100@integration.test.org", "test101@integration.test.org",
            "test102@integration.test.org", "test103@integration.test.org",
            "test104@integration.test.org");

    pageable = new PageRequest(0, 10, Direction.DESC, "emailDetails.email");
    searchParams = new UserContactDetailsSearchParamsDataBuilder().build();
    actual = repository.search(searchParams, pageable);

    assertThat(actual.getTotalElements()).isEqualTo(5L);
    assertThat(actual.getContent())
        .extracting(UserContactDetails::getEmailAddress)
        .contains("test104@integration.test.org", "test103@integration.test.org",
            "test102@integration.test.org", "test101@integration.test.org",
            "test100@integration.test.org");
  }
}
