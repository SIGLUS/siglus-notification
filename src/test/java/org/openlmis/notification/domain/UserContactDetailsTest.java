package org.openlmis.notification.domain;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.UUID;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.openlmis.notification.testutils.ToStringTestUtils;
import org.openlmis.notification.web.UserContactDetailsDto;

public class UserContactDetailsTest {

  @Test
  public void shouldDefaultEmailVerifiedToTrue() {
    UserContactDetails details = UserContactDetails.newUserContactDetails(
        new UserContactDetailsDto(UUID.randomUUID(), null, null, null, null)
    );
    
    assertThat(details.getEmailVerified(), Matchers.is(equalTo(Boolean.TRUE)));
  }

  @Test
  public void shouldDefaultAllowNotifyToTrue() {
    UserContactDetails details = UserContactDetails.newUserContactDetails(
        new UserContactDetailsDto(UUID.randomUUID(), null, null, null, null)
    );

    assertThat(details.getAllowNotify(), Matchers.is(equalTo(Boolean.TRUE)));
  }

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(UserContactDetails.class)
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    ToStringTestUtils.verify(UserContactDetails.class, new UserContactDetails());
  }
  
  
}
