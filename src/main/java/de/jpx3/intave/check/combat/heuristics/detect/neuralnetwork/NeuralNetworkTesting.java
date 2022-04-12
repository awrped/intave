package de.jpx3.intave.check.combat.heuristics.detect.neuralnetwork;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import de.jpx3.intave.check.MetaCheckPart;
import de.jpx3.intave.check.combat.Heuristics;
import de.jpx3.intave.executor.IntaveThreadFactory;
import de.jpx3.intave.module.linker.packet.ListenerPriority;
import de.jpx3.intave.module.linker.packet.PacketSubscription;
import de.jpx3.intave.module.tracker.entity.EntityShade;
import de.jpx3.intave.shade.ClientMathHelper;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.meta.CheckCustomMetadata;
import de.jpx3.intave.user.meta.MovementMetadata;
import org.bukkit.entity.Player;

import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static de.jpx3.intave.module.linker.packet.PacketId.Client.*;
import static de.jpx3.intave.check.combat.heuristics.detect.neuralnetwork.activationfunctions.ActivationFunction.*;

public class NeuralNetworkTesting extends MetaCheckPart<Heuristics, NeuralNetworkTesting.NeuralNetworkTestingMeta> {
  private final ExecutorService executorService = Executors.newSingleThreadExecutor(IntaveThreadFactory.ofLowestPriority());

  private final String testUsername = "iTz_Lucky";

  public NeuralNetworkTesting(Heuristics parentCheck) {
    super(parentCheck, NeuralNetworkTesting.NeuralNetworkTestingMeta.class);
  }
  
  public static class NeuralNetworkTestingMeta extends CheckCustomMetadata {
    public int lastAttack;
  }
  
  private static final NeuralNetwork NEURAL_NETWORK = new NeuralNetwork(
    2,
    sigmoid,
    20,
    sigmoid,
    1
  );
  private static List<Point> redPoints = new CopyOnWriteArrayList<>();
  private static List<Point> greenPoints = new CopyOnWriteArrayList<>();
  
  @PacketSubscription(
    priority = ListenerPriority.HIGH,
    packetsIn = {
      USE_ENTITY
    }
  )
  public void playerAttack(PacketEvent event) {
    User user = userOf(event.getPlayer());
    NeuralNetworkTestingMeta meta = metaOf(user);
    PacketContainer packet = event.getPacket();
    EnumWrappers.EntityUseAction action = packet.getEntityUseActions().readSafely(0);
    if (action == null) {
      action = packet.getEnumEntityUseActions().read(0).getAction();
    }
    if (action == EnumWrappers.EntityUseAction.ATTACK) {
      meta.lastAttack = 0;
    }
  }
  
  @PacketSubscription(
    priority = ListenerPriority.HIGH,
    packetsIn = {
      POSITION,
      POSITION_LOOK,
      FLYING,
      LOOK,
    }
  )
  public void playerMove(PacketEvent event) {
    Player player = event.getPlayer();
    User user = userOf(player);
    NeuralNetworkTestingMeta meta = metaOf(player);
    
    EntityShade target = user.meta().attack().lastAttackedEntity();
    if (target == null) {
      return;
    }
    MovementMetadata movementData = user.meta().movement();
    //user.player().sendMessage(String.format("x=%.3f y=%.3f z=%.3f",
    //  target.position.posX, target.position.posY, target.position.posZ));
    float lastPlayerYaw = ClientMathHelper.wrapAngleTo180_float(movementData.lastRotationYaw);
    float playerYaw = ClientMathHelper.wrapAngleTo180_float(movementData.rotationYaw);
    float serverYaw = resolveYawRotation(target.position, movementData.lastPositionX, movementData.lastPositionZ);

    float expectedYawDelta = (serverYaw - lastPlayerYaw) % 360f;
    float yawDelta = (playerYaw -lastPlayerYaw) % 360f;
    //player.sendMessage(String.format("%.4f %.4f", expectedYawDelta, yawDelta));
    if (meta.lastAttack <= 0 && yawDelta > 0) {
      double x = mapData(expectedYawDelta, -45, 45, -1, 1);
      double y = mapData(yawDelta, -45, 45, -1, 1);
      Point point = new Point(x, y);
      addPoint(player, point);
    }
  }

  private float resolveYawRotation(EntityShade.EntityPositionContext entityPositions, double posX, double posZ) {
    final double diffX = entityPositions.posX - posX;
    final double diffZ = entityPositions.posZ - posZ;
    return (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
  }

  @PacketSubscription(
    priority = ListenerPriority.HIGHEST,
    packetsIn = {
      POSITION,
      POSITION_LOOK,
      FLYING,
      LOOK,
    }
  )
  public void playerMoveEnd(PacketEvent event) {
    Player player = event.getPlayer();
    NeuralNetworkTestingMeta neuralNetworkTestingMeta = metaOf(player);
    neuralNetworkTestingMeta.lastAttack++;
  }
  
  void addPoint(Player player, Point point) {
    if (player.getName().contains(testUsername)) {
      greenPoints.add(point);
    } else {
      redPoints.add(point);
    }
  }
  
  double mapData(double value, double min, double max, double minTo, double maxTo) {
    return (1 - ((value - min) / (max - min))) * minTo + ((value - min) / (max - min)) * maxTo;
  }
  
  @PacketSubscription(
    priority = ListenerPriority.NORMAL,
    packetsIn = {
      ENTITY_ACTION
    }
  )
  public void playerSneaking(PacketEvent event) {
    EnumWrappers.PlayerAction playerActions = event.getPacket().getPlayerActions().readSafely(0);
    Player player = event.getPlayer();
    User user = userOf(player);
    MovementMetadata movement = user.meta().movement();
    
    if (playerActions != null) {
      if (playerActions == EnumWrappers.PlayerAction.START_SNEAKING && player.getName().contains(testUsername)) {
        double motion = Math.hypot(movement.motionX(), movement.motionZ());
        if (motion < 0.01) {
          openWindow();
        }
      }
    }
  }
  
  void openWindow() {
    redPoints = new CopyOnWriteArrayList<>();
    greenPoints = new CopyOnWriteArrayList<>();
  
    // openeing a new thread to let old JFrames open
    ExecutorService executorService = Executors.newSingleThreadExecutor(IntaveThreadFactory.ofLowestPriority());
    executorService.execute(() -> {
      JFrame frame = new JFrame();
      frame.setSize(800, 800);
      frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      Scene scene = new Scene(redPoints, greenPoints);
      frame.add(scene);
      frame.setVisible(true);
      frame.setLocationRelativeTo(null);
      
      while (true) {
        try {
          Thread.sleep(32);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        scene.repaint();
      }
    });
  }
  
  class Scene extends JPanel {
    private final static int radius = 2;
    private final static int secondRadius = 3;
    List<Point> redPoints;
    List<Point> greenPoints;
    
    public Scene(List<Point> redPoints, List<Point> greenPoints) {
      this.redPoints = redPoints;
      this.greenPoints = greenPoints;
    }
    
    public void paint(Graphics graphics) {
      BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
      int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
      
      for (int x = 0; x < image.getWidth(); x++) {
        for (int y = 0; y < image.getHeight(); y++) {
          int index = x + y * image.getHeight();
          
          double[] inputs = new double[] {
            mapData(x, 0, image.getWidth(), -1, 1),
            mapData(y, 0, image.getHeight(), -1, 1),
          };
          double result = NEURAL_NETWORK.predict(inputs).data[0][0];
          int brightness = (int) (Math.min(Math.max(result, 0), 1) * 255d);
          pixels[index] = new Color(brightness, brightness, brightness).getRGB();
        }
      }
      graphics.drawImage(image, 0, 0, getWidth(), getHeight(), null);
      
      ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      for (Point point : greenPoints) {
        drawPoint(graphics, point, secondRadius, Color.black);
        drawPoint(graphics, point, radius, Color.green);
      }
      
      for (Point point : redPoints) {
        drawPoint(graphics, point, secondRadius, Color.black);
        drawPoint(graphics, point, radius, Color.red);
      }
    }
    
    void drawPoint(Graphics graphics, Point point, int radius, Color color) {
      graphics.setColor(color);
      int x = (int) mapData(point.x, -1, 1, 0, getWidth());
      int y = (int) mapData(point.y, -1, 1, 0, getHeight());
      graphics.fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }
  }
  
  class Point extends JPanel {
    double x, y;
    
    public Point(double x, double y) {
      this.x = x;
      this.y = y;
    }
  }
}