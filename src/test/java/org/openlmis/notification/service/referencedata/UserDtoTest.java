package org.openlmis.notification.service.referencedata;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.openlmis.notification.testutils.ToStringTestUtils;

public class UserDtoTest {

  @Test
  public void equalsContract() {
    EqualsVerifier
        .forClass(UserDto.class)
        .suppress(Warning.NONFINAL_FIELDS)
        .suppress(Warning.STRICT_INHERITANCE)
        .withRedefinedSuperclass()
        .verify();
  }

  @Test
  public void shouldImplementToString() {
    ToStringTestUtils.verify(UserDto.class, new UserDto());
  }
  
}
