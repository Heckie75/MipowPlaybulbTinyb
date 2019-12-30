package de.heckie.tinyb.mipow.playbulb;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.heckie.tinyb.common.Utils;
import tinyb.BluetoothDevice;
import tinyb.BluetoothGattCharacteristic;

public class Playbulb {

  public static String CHARACTERISTIC_SERVICE_CHANGED = "00002a05-0000-1000-8000-00805f9b34fb";
  public static String CHARACTERISTIC_BATTERY_LEVEL = "00002a19-0000-1000-8000-00805f9b34fb";
  public static String CHARACTERISTIC_SERIAL_NUMBER_STRING = "00002a25-0000-1000-8000-00805f9b34fb";
  public static String CHARACTERISTIC_FIRMWARE_REVISION_STRING = "00002a26-0000-1000-8000-00805f9b34fb";
  public static String CHARACTERISTIC_HARDWARE_REVISION_STRING = "00002a27-0000-1000-8000-00805f9b34fb";
  public static String CHARACTERISTIC_SOFTWARE_REVISION_STRING = "00002a28-0000-1000-8000-00805f9b34fb";
  public static String CHARACTERISTIC_MANUFACTURER_NAME_STRING = "00002a29-0000-1000-8000-00805f9b34fb";
  public static String CHARACTERISTIC_HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
  public static String CHARACTERISTIC_HEART_RATE_CONTROL_POINT = "00002a39-0000-1000-8000-00805f9b34fb";
  public static String CHARACTERISTIC_PNP_ID = "00002a50-0000-1000-8000-00805f9b34fb";

  public static String CHARACTERISTIC_PLAYBULB_PIN = "0000fff7-0000-1000-8000-00805f9b34fb";
  public static String CHARACTERISTIC_PLAYBULB_RUNNING_TIMERS = "0000fff8-0000-1000-8000-00805f9b34fb";
  public static String CHARACTERISTIC_PLAYBULB_RANDOM_MODE = "0000fff9-0000-1000-8000-00805f9b34fb";
  public static String CHARACTERISTIC_PLAYBULB_FFFA = "0000fffa-0000-1000-8000-00805f9b34fb";
  public static String CHARACTERISTIC_PLAYBULB_EFFECT = "0000fffb-0000-1000-8000-00805f9b34fb";
  public static String CHARACTERISTIC_PLAYBULB_COLOR = "0000fffc-0000-1000-8000-00805f9b34fb";
  public static String CHARACTERISTIC_PLAYBULB_FACTORY_RESET = "0000fffd-0000-1000-8000-00805f9b34fb";
  public static String CHARACTERISTIC_PLAYBULB_TIMER_SETTTINGS = "0000fffe-0000-1000-8000-00805f9b34fb";
  public static String CHARACTERISTIC_PLAYBULB_GIVEN_NAME = "0000ffff-0000-1000-8000-00805f9b34fb";

  private final BluetoothDevice bulb;
  private final Map<String, BluetoothGattCharacteristic> characteristics;

  private String name;
  private String serialNumber;
  private String pin;
  private Integer batteryLevel;
  private String firmwareRevision;
  private String hardwareRevision;
  private String softwareRevision;
  private String manufacturer;
  private BigInteger pnpId;
  private Color color;
  private Effect effect;
  private Timers timers;
  private Randommode randommode;

  public static class Color {

    private int white;
    private int red;
    private int green;
    private int blue;

    public Color(int white, int red, int green, int blue) {
      this.white = white;
      this.red = red;
      this.green = green;
      this.blue = blue;
    }

    public static Color fromBytes(byte[] bytes) {
      return new Color(bytes[0] & 0xff, bytes[1] & 0xff, bytes[2] & 0xff, bytes[3] & 0xff);
    }

    public int getWhite() {
      return white;
    }

    public void setWhite(int white) {
      this.white = white;
    }

    public int getRed() {
      return red;
    }

    public void setRed(int red) {
      this.red = red;
    }

    public int getGreen() {
      return green;
    }

    public void setGreen(int green) {
      this.green = green;
    }

    public int getBlue() {
      return blue;
    }

    public void setBlue(int blue) {
      this.blue = blue;
    }

    @Override
    public String toString() {
      return String.format("Color(white=%d, red=%d, green=%d, blue=%d", getWhite(), getRed(), getGreen(),
          getBlue());
    }

    byte[] toBytes() {
      return new byte[] { (byte) white, (byte) red, (byte) green, (byte) blue };
    }

  }

  public static enum EffectType {

    BLINK(0), PULSE(1), DISCO(2), RAINBOW(3), CANDLE(4), OFF(-1);

    private final int value;

    EffectType(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }

    public static EffectType getByValue(int value) {

      EffectType[] effects = EffectType.values();
      for (int i = 0; i < effects.length; i++) {
        if (effects[i].value == value) {
          return effects[i];
        }
      }
      return OFF;
    }

  }

  public static class Effect {

    private EffectType effectType;
    private Color color;
    private int delay;

    public Effect(EffectType effectType, Color color, int delay) {
      this.effectType = effectType;
      this.color = color;
      this.delay = delay;
    }

    public static Effect fromBytes(byte[] bytes) {
      return new Effect(EffectType.getByValue(bytes[4] & 0xff), Color.fromBytes(Arrays.copyOfRange(bytes, 0, 4)),
          bytes[6] & 0xff);
    }

    public EffectType getEffectType() {
      return effectType;
    }

    public void setEffectType(EffectType effectType) {
      this.effectType = effectType;
    }

    public Color getColor() {
      return color;
    }

    public void setColor(Color color) {
      this.color = color;
    }

    public int getDelay() {
      return delay;
    }

    public void setDelay(int delay) {
      this.delay = delay;
    }

    @Override
    public String toString() {
      return String.format("Effect(type=%s, color=%s, delay=%d", effectType.toString(), color, delay);
    }

    byte[] toBytes() {
      byte[] c;
      if (color != null) {
        c = color.toBytes();
      } else {
        c = new byte[] { 0, 0, 0, 0 };
      }
      return new byte[] { c[0], c[1], c[2], c[3], (byte) effectType.getValue(), 0, (byte) delay, 0 };
    }
  }

  private enum TimerType {
    WAKEUP(0), DOZE(1), OFF(2);

    private final int value;

    TimerType(int value) {
      this.value = value;
    }

    public static TimerType getByValue(int value) {

      TimerType[] timers = TimerType.values();
      for (int i = 0; i < timers.length; i++) {
        if (timers[i].value == value) {
          return timers[i];
        }
      }
      return OFF;
    }

  }

  public static class Timer {

    private int id;
    private boolean active;
    private TimerType type;
    private int startingHour;
    private int startingMinute;
    private int runtime;
    private Color color;

    public Timer(int id, boolean active, TimerType type, int startingHour, int startingMinute, int runtime,
        Color color) {
      this.id = id;
      this.active = active;
      this.type = type;
      this.startingHour = startingHour;
      this.startingMinute = startingMinute;
      this.runtime = runtime;
      this.color = color;
    }

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public boolean isActive() {
      return active;
    }

    public void setActive(boolean active) {
      this.active = active;
    }

    public TimerType getType() {
      return type;
    }

    public void setType(TimerType type) {
      this.type = type;
    }

    public int getStartingHour() {
      return startingHour;
    }

    public void setStartingHour(int startingHour) {
      this.startingHour = startingHour;
    }

    public int getStartingMinute() {
      return startingMinute;
    }

    public void setStartingMinute(int startingMinute) {
      this.startingMinute = startingMinute;
    }

    public int getRuntime() {
      return runtime;
    }

    public void setRuntime(int runtime) {
      this.runtime = runtime;
    }

    public Color getColor() {
      return color;
    }

    public void setColor(Color color) {
      this.color = color;
    }

    @Override
    public String toString() {
      String schedule;

      if (startingHour == -1) {
        schedule = "N/A";
      } else {
        schedule = String.format("%02d:%02d", getStartingHour(), getStartingMinute());
      }

      return String.format("Timer(id=%d, active=%b, type=%s, schedule=%s, runtime=%d, color=%s)", id, active,
          type, schedule, getRuntime(), color);
    }

    byte[] toBytes() {
      Calendar calendar = Calendar.getInstance();
      int currentSecond = calendar.get(Calendar.SECOND);
      int currentMinute = calendar.get(Calendar.MINUTE);
      int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

      byte[] c = color.toBytes();
      return new byte[] { (byte) id, (byte) type.value, (byte) currentSecond, (byte) currentMinute,
          (byte) currentHour, (byte) (active ? 0 : -1), (byte) startingMinute, (byte) startingHour, c[0],
          c[1], c[2], c[3], (byte) runtime };
    }
  }

  public static class Timers {

    private Timer[] timers;
    private int currentHour;
    private int currentMinute;

    private Timers(int currentHour, int currentMinute) {
      this.timers = new Timer[] { null, null, null, null };
      this.currentHour = currentHour;
      this.currentMinute = currentMinute;
    }

    public static Timers fromBytes(byte[] bytesTimer, byte[] bytesEffect) {

      Timers timers = new Timers(bytesTimer[12] & 0xff, bytesTimer[13] & 0xff);

      for (int i = 0; i < 4; i++) {
        TimerType type = TimerType.getByValue(bytesTimer[0 + i * 3]);
        Color color = Color.fromBytes(Arrays.copyOfRange(bytesEffect, 0 + i * 5, 4 + i * 5));
        timers.timers[i] = new Timer(i, (bytesTimer[1 + i * 3] != -1), type, bytesTimer[1 + i * 3],
            bytesTimer[2 + i * 3], bytesEffect[4 + i * 5], color);
      }

      return timers;
    }

    public Timer getTimer(int id) {
      id %= 4;
      return timers[id];
    }

    public void setTimer(Timer timer) {
      timers[timer.getId() % 4] = timer;
    }

    public int getCurrentHour() {
      return currentHour;
    }

    public int getCurrentMinute() {
      return currentMinute;
    }

    @Override
    public String toString() {
      String s = "Timers(time=" + String.format("%02d:%02d", getCurrentHour(), getCurrentMinute());
      List<String> ts = Arrays.asList(timers).stream().map(t -> t.toString()).collect(Collectors.toList());
      s += ", " + String.join(", ", ts);
      s += ")";
      return s;
    }
  }

  public static class Randommode {

    private int startingHour;
    private int startingMinute;
    private int endingHour;
    private int endingMinute;
    private int minInterval;
    private int maxInterval;
    private Color color;

    public Randommode(int startingHour, int startingMinute, int endingHour, int endingMinute, int minInterval,
        int maxInterval, Color color) {
      this.startingHour = startingHour;
      this.startingMinute = startingMinute;
      this.endingHour = endingHour;
      this.endingMinute = endingMinute;
      this.minInterval = minInterval;
      this.maxInterval = maxInterval;
      this.color = color;
    }

    public static Randommode fromBytes(byte[] bytes) {
      Color color = Color.fromBytes(Arrays.copyOfRange(bytes, 9, 13));
      return new Randommode(bytes[3], bytes[4], bytes[5], bytes[6], bytes[7], bytes[8], color);
    }

    public int getStartingHour() {
      return startingHour;
    }

    public void setStartingHour(int startingHour) {
      this.startingHour = startingHour;
    }

    public int getStartingMinute() {
      return startingMinute;
    }

    public void setStartingMinute(int startingMinute) {
      this.startingMinute = startingMinute;
    }

    public int getEndingHour() {
      return endingHour;
    }

    public void setEndingHour(int endingHour) {
      this.endingHour = endingHour;
    }

    public int getEndingMinute() {
      return endingMinute;
    }

    public void setEndingMinute(int endingMinute) {
      this.endingMinute = endingMinute;
    }

    public int getMinInterval() {
      return minInterval;
    }

    public void setMinInterval(int minInterval) {
      this.minInterval = minInterval;
    }

    public int getMaxInterval() {
      return maxInterval;
    }

    public void setMaxInterval(int maxInterval) {
      this.maxInterval = maxInterval;
    }

    public Color getColor() {
      return color;
    }

    public void setColor(Color color) {
      this.color = color;
    }

    @Override
    public String toString() {

      String start, stop;
      if (getStartingHour() == 255) {
        start = "N/A";
        stop = "N/A";
      } else {
        start = String.format("%02d:%02d", getStartingHour(), getStartingMinute());
        stop = String.format("%02d:%02d", getEndingHour(), getEndingMinute());
      }

      return String.format("Randommode(start=%s, stop=%s, min=%d, max=%d, color=%s)", start, stop,
          getMinInterval(), getMaxInterval(), color);
    }

    byte[] toBytes() {
      Calendar calendar = Calendar.getInstance();
      int currentSecond = calendar.get(Calendar.SECOND);
      int currentMinute = calendar.get(Calendar.MINUTE);
      int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
      byte[] c = color.toBytes();
      return new byte[] { (byte) currentSecond, (byte) currentMinute, (byte) currentHour, (byte) startingHour,
          (byte) startingMinute, (byte) endingHour, (byte) endingMinute, (byte) minInterval,
          (byte) maxInterval, c[0], c[1], c[2], c[3] };
    }
  }

  public Playbulb(BluetoothDevice device) {
    this.bulb = device;
    if (!device.getConnected()) {
      device.connect();
    }

    this.characteristics = Utils.getCharacteristics(device);
  }

  public Playbulb readSerialNumber() {
    if (serialNumber == null) {
      serialNumber = Utils.readString(characteristics.get(CHARACTERISTIC_SERIAL_NUMBER_STRING));
    }
    return this;
  }

  public Playbulb readFirmwareRevision() {
    if (firmwareRevision == null) {
      firmwareRevision = Utils.readString(characteristics.get(CHARACTERISTIC_FIRMWARE_REVISION_STRING));
    }
    return this;
  }

  public Playbulb readHardwareRevision() {
    if (hardwareRevision == null) {
      hardwareRevision = Utils.readString(characteristics.get(CHARACTERISTIC_HARDWARE_REVISION_STRING));
    }
    return this;
  }

  public Playbulb readSoftwareRevision() {
    if (softwareRevision == null) {
      softwareRevision = Utils.readString(characteristics.get(CHARACTERISTIC_SOFTWARE_REVISION_STRING));
    }
    return this;
  }

  public Playbulb readManufacturerName() {
    if (manufacturer == null) {
      manufacturer = Utils.readString(characteristics.get(CHARACTERISTIC_MANUFACTURER_NAME_STRING));
    }
    return this;
  }

  public Playbulb readPnpId() {
    if (pnpId == null) {
      pnpId = Utils.readBigInteger(characteristics.get(CHARACTERISTIC_PNP_ID));
    }
    return this;
  }

  public Playbulb readColor() {
    byte[] bytes = characteristics.get(CHARACTERISTIC_PLAYBULB_COLOR).readValue();
    color = Color.fromBytes(bytes);
    return this;
  }

  public Playbulb readEffect() {
    byte[] bytes = characteristics.get(CHARACTERISTIC_PLAYBULB_EFFECT).readValue();
    effect = Effect.fromBytes(bytes);
    return this;
  }

  public Playbulb readTimers() {
    BluetoothGattCharacteristic timerCharacteristic = characteristics.get(CHARACTERISTIC_PLAYBULB_TIMER_SETTTINGS);
    BluetoothGattCharacteristic runningCharacteristic = characteristics.get(CHARACTERISTIC_PLAYBULB_RUNNING_TIMERS);
    if (timerCharacteristic != null && runningCharacteristic != null) {
      byte[] bytesTimer = timerCharacteristic.readValue();
      byte[] bytesEffect = runningCharacteristic.readValue();

      timers = Timers.fromBytes(bytesTimer, bytesEffect);
    }
    return this;
  }

  public Playbulb readRandommode() {
    byte[] bytes = characteristics.get(CHARACTERISTIC_PLAYBULB_RANDOM_MODE).readValue();
    randommode = Randommode.fromBytes(bytes);
    return this;
  }

  public Playbulb readName() {
    name = Utils.readString(characteristics.get(CHARACTERISTIC_PLAYBULB_GIVEN_NAME));
    return this;
  }

  public Playbulb readPin() {
    BluetoothGattCharacteristic characteristic = characteristics.get(CHARACTERISTIC_PLAYBULB_PIN);
    if (characteristic != null) {
      pin = Utils.readString(characteristic);
    } else {
      pin = "N/A";
    }
    return this;
  }

  public Playbulb readBatteryLevel() {
    BluetoothGattCharacteristic characteristic = characteristics.get(CHARACTERISTIC_BATTERY_LEVEL);
    if (characteristic != null) {
      batteryLevel = Utils.readInteger(characteristic);
    } else {
      batteryLevel = null;
    }
    return this;
  }

  public void readAll() {
    this.readFirmwareRevision().readHardwareRevision().readManufacturerName().readPnpId().readSerialNumber()
        .readSoftwareRevision().readColor().readEffect().readTimers().readRandommode().readName().readPin().readBatteryLevel();
  }

  public String getSerialNumber() {
    readSerialNumber();
    return serialNumber;
  }

  public String getFirmwareRevision() {
    readFirmwareRevision();
    return firmwareRevision;
  }

  public String getHardwareRevision() {
    readHardwareRevision();
    return hardwareRevision;
  }

  public String getSoftwareRevision() {
    readSoftwareRevision();
    return softwareRevision;
  }

  public String getManufacturer() {
    readManufacturerName();
    return manufacturer;
  }

  public BigInteger getPnpId() {
    readPnpId();
    return pnpId;
  }

  public Color getColor(boolean force) {
    if (color == null || force) {
      readColor();
    }
    return color;
  }

  public void setColor(Color color) {
    byte[] bytes = color.toBytes();
    boolean success = characteristics.get(CHARACTERISTIC_PLAYBULB_COLOR).writeValue(bytes);
    if (success) {
      this.color = color;
    }
  }

  public Effect getEffect(boolean force) {
    if (effect == null || force) {
      readEffect();
    }
    return effect;
  }

  public void setEffect(Effect effect) {
    byte[] bytes = effect.toBytes();
    boolean success = characteristics.get(CHARACTERISTIC_PLAYBULB_EFFECT).writeValue(bytes);
    if (success) {
      this.effect = effect;
    }
  }

  public Timers getTimers(boolean force) {
    if (timers == null || force) {
      readTimers();
    }
    return timers;
  }

  public void setTimer(Timer timer) {
    byte[] bytes = timer.toBytes();
    boolean success = characteristics.get(CHARACTERISTIC_PLAYBULB_TIMER_SETTTINGS).writeValue(bytes);
    if (success) {
      timers.setTimer(timer);
    }
  }

  public Randommode getRandommode(boolean force) {
    if (randommode == null || force) {
      readRandommode();
    }
    return randommode;
  }

  public void setRandommode(Randommode randommode) {
    byte[] bytes = randommode.toBytes();
    boolean success = characteristics.get(CHARACTERISTIC_PLAYBULB_RANDOM_MODE).writeValue(bytes);
    if (success) {
      this.randommode = randommode;
    }
  }

  public String getName(boolean force) {
    if (name == null || force) {
      readName();
    }
    return name;
  }

  public void setName(String name) {
    byte[] bytes = name.getBytes();
    boolean success = characteristics.get(CHARACTERISTIC_PLAYBULB_GIVEN_NAME).writeValue(bytes);
    if (success) {
      this.name = name;
    }
  }

  public String getPin(boolean force) {
    if (pin == null || force) {
      readPin();
    }
    return pin;
  }

  public void setPin(String pin) {
    byte[] bytes = pin.getBytes();
    boolean success = characteristics.get(CHARACTERISTIC_PLAYBULB_GIVEN_NAME).writeValue(bytes);
    if (success) {
      this.pin = pin;
    }
  }

  public Integer getBatteryLevel(boolean force) {
    if (batteryLevel == null || force) {
      readBatteryLevel();
    }
    return batteryLevel;
  }

  @Override
  public String toString() {
    return String.format(
        "Playbulb(mac=%s, name=%s, pin=%s, battery=%d, manufacturer=%s, serialnumber=%s, firmware=%s, hardware=%s, software=%s, pnp=%d, color=%s, effect=%s, timers=%s, randommode=%s)",
        bulb.getAddress(), name, pin, batteryLevel, manufacturer, serialNumber, firmwareRevision, hardwareRevision,
        softwareRevision, pnpId, color, effect, timers, randommode);
  }

}
