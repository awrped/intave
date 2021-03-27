package de.jpx3.intave.fakeplayer;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.google.common.collect.ImmutableList;
import net.minecraft.server.v1_8_R3.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import net.minecraft.server.v1_8_R3.Scoreboard;
import net.minecraft.server.v1_8_R3.ScoreboardTeam;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public final class FakePlayerNameHelper {
  public static void sendScoreboard(
    Player player,
    String teamName,
    WrappedGameProfile fakePlayerProfile
  ) {
    PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
    ImmutableList<String> teamList = ImmutableList.of(fakePlayerProfile.getName());
    ScoreboardTeam scoreboardTeam = new ScoreboardTeam(new Scoreboard(), teamName);
    PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam(scoreboardTeam, teamList, 3);
    connection.sendPacket(packet);
  }
}