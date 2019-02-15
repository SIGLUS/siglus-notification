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

import static org.junit.Assert.assertEquals;

import java.util.UUID;
import org.junit.Test;
import org.openlmis.notification.domain.EmailVerificationToken;
import org.openlmis.notification.domain.UserContactDetails;
import org.openlmis.notification.testutils.EmailVerificationTokenDataBuilder;
import org.openlmis.notification.util.UserContactDetailsDataBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.repository.CrudRepository;

public class EmailVerificationTokenRepositoryIntegrationTest
    extends BaseCrudRepositoryIntegrationTest<EmailVerificationToken, UUID> {

  @Autowired
  private EmailVerificationTokenRepository repository;

  @Autowired
  private UserContactDetailsRepository userContactDetailsRepository;

  @Override
  CrudRepository<EmailVerificationToken, UUID> getRepository() {
    return repository;
  }

  @Override
  EmailVerificationToken generateInstance() {
    UserContactDetails contactDetails = new UserContactDetailsDataBuilder().build();
    contactDetails = userContactDetailsRepository.save(contactDetails);

    return new EmailVerificationTokenDataBuilder()
        .withoutId()
        .withContactDetails(contactDetails)
        .build();
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldBePossibleToHaveOnlyOneTokenPerUser() {
    EmailVerificationToken token = generateInstance();
    repository.save(token);

    EmailVerificationToken newToken = new EmailVerificationTokenDataBuilder()
        .withoutId()
        .withContactDetails(token.getUserContactDetails())
        .build();
    repository.saveAndFlush(newToken);
  }

  @Test(expected = DataIntegrityViolationException.class)
  public void shouldBePossibleToHaveOnlyOneTokenPerEmail() {
    EmailVerificationToken token = generateInstance();
    repository.save(token);

    EmailVerificationToken newToken = generateInstance();
    newToken.setEmailAddress(token.getEmailAddress());

    repository.saveAndFlush(newToken);
  }

  @Test
  public void shouldFindTokenByEmailAddress() {
    EmailVerificationToken token = generateInstance();

    repository.save(generateInstance());
    repository.save(token);
    repository.save(generateInstance());

    EmailVerificationToken result = repository.findOneByEmailAddress(token.getEmailAddress());

    assertEquals(token, result);
  }

}
