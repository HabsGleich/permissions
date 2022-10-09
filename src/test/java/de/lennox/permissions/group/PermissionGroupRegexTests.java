package de.lennox.permissions.group;

import de.lennox.permissions.database.model.PermissionGroup;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PermissionGroupRegexTests {

  @Test
  public void testRegexTransformation() {
    PermissionGroup fakeGroup = new PermissionGroup("Test", "", false, List.of("*"), List.of());

    assertEquals(fakeGroup.createRegexFromInput("hello.world.*"), "hello\\.world\\.(.*)");
  }
}
