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

package org.openlmis.notification.service;

import org.openlmis.notification.domain.EmailDetails;
import org.openlmis.notification.domain.EmailVerificationToken;
import org.openlmis.notification.domain.UserContactDetails;
import org.openlmis.notification.repository.EmailVerificationTokenRepository;
import org.openlmis.notification.repository.UserContactDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserContactDetailsService {

  @Autowired
  private UserContactDetailsRepository userContactDetailsRepository;

  @Autowired
  private EmailVerificationNotifier emailVerificationNotifier;

  @Autowired
  private EmailVerificationTokenRepository emailVerificationTokenRepository;

  /**
   * Adds a new or updates existing user's contact details.
   */
  public UserContactDetails addOrUpdate(UserContactDetails details) {
    return userContactDetailsRepository.exists(details.getReferenceDataUserId())
        ? updateUserContactDetails(details)
        : addUserContactDetails(details);
  }

  private UserContactDetails addUserContactDetails(UserContactDetails toSave) {
    if (null != toSave.getEmailDetails()) {
      toSave.getEmailDetails().setEmailVerified(false);
    }

    UserContactDetails saved = userContactDetailsRepository.save(toSave);

    if (saved.hasEmailAddress()) {
      emailVerificationNotifier.sendNotification(saved, saved.getEmailAddress());
    }

    return saved;
  }

  private UserContactDetails updateUserContactDetails(UserContactDetails toUpdate) {
    EmailDetails newEmailDetails = null;

    UserContactDetails existing = userContactDetailsRepository.findOne(toUpdate.getId());

    if (toUpdate.hasEmailAddress()
        && !toUpdate.getEmailAddress().equals(existing.getEmailAddress())) {
      newEmailDetails = toUpdate.getEmailDetails();
      toUpdate.setEmailDetails(new EmailDetails(
          existing.getEmailAddress(), existing.isEmailVerified()
      ));
    }

    UserContactDetails saved = userContactDetailsRepository.save(toUpdate);

    if (null != newEmailDetails) {
      EmailVerificationToken token = emailVerificationTokenRepository
          .findOneByUserContactDetails(saved);

      if (null == token || !token.getEmailAddress().equals(newEmailDetails.getEmail())) {
        emailVerificationNotifier.sendNotification(saved, newEmailDetails.getEmail());
      }
    }

    return saved;
  }

}
