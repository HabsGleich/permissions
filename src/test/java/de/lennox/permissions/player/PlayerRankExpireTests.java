package de.lennox.permissions.player;

import de.lennox.permissions.database.model.PermittedPlayer;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlayerRankExpireTests {

  @Test
  public void testExpiryDateFormatting() {
    PermittedPlayer fakePlayer = new PermittedPlayer(UUID.randomUUID(), "", 1665544201586L);
    assertEquals(fakePlayer.parseExpiryDate(), "12.10.2022 05:10");
  }
}
