package de.jpx3.intave.detect.checks.combat.heuristics.detection;

import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import de.jpx3.intave.detect.IntaveMetaCheckPart;
import de.jpx3.intave.detect.checks.combat.Heuristics;
import de.jpx3.intave.event.packet.ListenerPriority;
import de.jpx3.intave.event.packet.PacketDescriptor;
import de.jpx3.intave.event.packet.PacketSubscription;
import de.jpx3.intave.event.packet.Sender;
import de.jpx3.intave.executor.IntaveThreadFactory;
import de.jpx3.intave.tools.MathHelper;
import de.jpx3.intave.tools.wrapper.WrappedMathHelper;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserCustomCheckMeta;
import de.jpx3.intave.user.UserMetaAttackData;
import de.jpx3.intave.user.UserMetaMovementData;
import org.bukkit.entity.Player;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LinearRegressionHeuristic extends IntaveMetaCheckPart<Heuristics, LinearRegressionHeuristic.LinearRegressionHeuristicMeta> {
  private final ExecutorService executorService = Executors.newSingleThreadExecutor(IntaveThreadFactory.ofLowestPriority());

  public LinearRegressionHeuristic(Heuristics parentCheck) {
    super(parentCheck, LinearRegressionHeuristic.LinearRegressionHeuristicMeta.class);
  }

  @PacketSubscription(
    priority = ListenerPriority.HIGH,
    packets = {
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "ENTITY_ACTION")
    }
  )
  public void sneakStart(PacketEvent event) {
    Player player = event.getPlayer();
    EnumWrappers.PlayerAction action = event.getPacket().getPlayerActions().read(0);

    if (action == EnumWrappers.PlayerAction.START_SNEAKING) {
      createNewWindow(player);
    }
  }

  @PacketSubscription(
    priority = ListenerPriority.HIGH,
    packets = {
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "POSITION_LOOK"),
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "LOOK")
    }
  )
  public void clientTickUpdate(PacketEvent event) {
    Player player = event.getPlayer();
    User user = userOf(player);
    LinearRegressionHeuristicMeta meta = metaOf(player);

    boolean updated = addValuesToList(user);

    if (updated) {
      linearRegression(player, meta);

      if (meta.panel != null) {
        meta.panel.repaint();
      }
    }
  }

  private boolean addValuesToList(User user) {
    Player player = user.player();
    UserMetaMovementData movementData = user.meta().movementData();
    LinearRegressionHeuristicMeta meta = metaOf(player);
    UserMetaAttackData attackData = user.meta().attackData();

    if (!attackData.recentlyAttacked(2000)) {
      return false;
    }

    double x = Math.abs(WrappedMathHelper.wrapAngleTo180_double(attackData.perfectYaw() - movementData.rotationYaw + 90));
    double y = Math.abs(WrappedMathHelper.wrapAngleTo180_double(movementData.rotationYaw - movementData.lastRotationYaw + 90));//Math.abs(movementData.rotationPitch - attackData.perfectPitch());

//    if ()
    {
      if (x > meta.highestVectorX)
        meta.highestVectorX = x;

      if (y > meta.highestVectorY)
        meta.highestVectorY = y;

      Vector vector = new Vector(x, y);
      meta.vectorList.add(vector);
      return true;
    }

//    return false;
  }

  private void linearRegression(Player player, LinearRegressionHeuristicMeta meta) {
    double xSum = 0;
    double ySum = 0;

    for (Vector vector : meta.vectorList) {
      xSum += vector.x;
      ySum += vector.y;
    }

    double xAverage = xSum / meta.vectorList.size();
    double yAverage = ySum / meta.vectorList.size();

    double nummerator = 0;
    double denominator = 0;

    for (Vector vector : meta.vectorList) {
      nummerator += (vector.x - xAverage) * (vector.y - yAverage);
      denominator += (vector.x - xAverage) * (vector.x - xAverage);
    }

    double m = nummerator / denominator;
    double b = yAverage - m * xAverage;

    meta.m = m;
    meta.b = b;

    player.sendMessage("" + b);
  }

  private void createNewWindow(Player player) {
    executorService.execute(() -> {
      LinearRegressionHeuristicMeta meta = metaOf(player);
      final int radius = 2;

      JFrame window = new JFrame();
      window.setSize(800, 800);
      window.setFocusable(true);
      window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

      JPanel panel = new JPanel() {
        @Override
        public void paint(Graphics g) {
          super.paint(g);
          String newTitle = "Value count: " + meta.vectorList.size();
          for(int i = newTitle.length(); i < 30; i++)
            newTitle += " ";
          window.setTitle(newTitle + " b: " + meta.b);

          Graphics2D g2d = ((Graphics2D) g);
          g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

          g.setColor(Color.orange);
          for (Vector vector : meta.vectorList) {
            int x = (int) map(vector.x, 0, meta.highestVectorX, 0, getWidth());
            int y = (int) map(vector.y, 0, meta.highestVectorY, 0, getHeight());

            g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
          }

          double x1 = 0;
          double y1 = meta.m * x1 + meta.b;
          double x2 = meta.highestVectorX;
          double y2 = meta.m * x2 + meta.b;

          g2d.setStroke(new BasicStroke(4f));
          g.setColor(Color.white);

          x1 = map(x1, 0, meta.highestVectorX, 0, getWidth());
          y1 = map(y1, 0, meta.highestVectorY, getHeight(), 0);
          x2 = map(x2, 0, meta.highestVectorX, 0, getWidth());
          y2 = map(y2, 0, meta.highestVectorY, getHeight(), 0);

          g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
        }
      };
      panel.setBackground(new Color(51, 51, 51));
      meta.panel = panel;
      window.add(panel);
      window.setVisible(true);
    });
  }

  private double map(double from, double minFrom, double maxFrom, double minTo, double maxTo) {
    return from / (Math.abs(minFrom) + Math.abs(maxFrom)) * (Math.abs(minTo) + Math.abs(maxTo));
  }

  public static class LinearRegressionHeuristicMeta extends UserCustomCheckMeta {
    public JPanel panel;
    public double b;
    public double m;
    List<Vector> vectorList = new ArrayList<>();
    double highestVectorX;
    double highestVectorY;
  }
}

class Vector {
  double x;
  double y;

  public Vector(double x, double y) {
    this.x = x;
    this.y = y;
  }
}