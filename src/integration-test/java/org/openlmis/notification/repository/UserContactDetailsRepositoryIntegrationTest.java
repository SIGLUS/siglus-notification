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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.UUID;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openlmis.notification.domain.UserContactDetails;
import org.openlmis.notification.util.EmailDetailsDataBuilder;
import org.openlmis.notification.util.UserContactDetailsDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
    assertThat(instance.getId(), is(notNullValue()));
  }

  @Override
  UserContactDetails generateInstance() {
    return new UserContactDetailsDataBuilder().build();
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
}
