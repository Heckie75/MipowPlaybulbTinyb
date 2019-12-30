import de.heckie.tinyb.common.BluetoothConnectionException;
import de.heckie.tinyb.common.DeviceNotFoundException;
import de.heckie.tinyb.common.Utils;
import de.heckie.tinyb.mipow.playbulb.Playbulb;
import de.heckie.tinyb.mipow.playbulb.Playbulb.Color;
import de.heckie.tinyb.mipow.playbulb.Playbulb.Effect;
import de.heckie.tinyb.mipow.playbulb.Playbulb.EffectType;
import de.heckie.tinyb.mipow.playbulb.Playbulb.Randommode;
import tinyb.BluetoothDevice;
import tinyb.BluetoothManager;

public class PlayPlayBulb {

  public static void main(String[] args) throws InterruptedException, DeviceNotFoundException {

    BluetoothManager manager = BluetoothManager.getBluetoothManager();
    try {
      BluetoothDevice device = Utils.getDevice("6A:9C:4B:0F:AC:E6", manager);
      Utils.connect(device);

      Playbulb playbulb = new Playbulb(device);
      playbulb.readAll();

      playbulb.setColor(new Color(255, 0, 0, 0));
      playbulb.setEffect(new Effect(EffectType.RAINBOW, null, 25));
      playbulb.setRandommode(new Randommode(16, 30, 22, 30, 40, 75, new Color(255, 0, 0, 0)));
      playbulb.setName("Wohnzimmer");
      System.out.println(playbulb);

      Utils.disconnect(device);

    } catch (BluetoothConnectionException e) {
      e.printStackTrace();
    }
  }

}
