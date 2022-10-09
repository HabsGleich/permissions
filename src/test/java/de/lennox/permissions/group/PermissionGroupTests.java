package de.lennox.permissions.group;

import de.lennox.permissions.database.model.PermissionGroup;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PermissionGroupTests {

  @Test
  public void testSimplePermission() {
    PermissionGroup fakeGroup =
        new PermissionGroup("Test", "", false, List.of("i.am.a.test"), List.of("hello.world"));

    assertTrue(fakeGroup.hasPermission("i.am.a.test"));
    assertFalse(fakeGroup.hasPermission("hello.world"));
  }

  @Test
  public void testComplexPermission() {
    PermissionGroup fakeGroup =
        new PermissionGroup("Test", "", false, List.of("i.am.a.*"), List.of("hello.*"));

    assertTrue(fakeGroup.hasPermission("i.am.a.test"));
    assertFalse(fakeGroup.hasPermission("hello.world"));
  }

  @Test
  public void testEverythingPermission() {
    PermissionGroup fakeGroup = new PermissionGroup("Test", "", false, List.of("*"), List.of());

    assertTrue(fakeGroup.hasPermission("i.am.a.test"));
    assertTrue(fakeGroup.hasPermission("hello.world"));
  }
}
