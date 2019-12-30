package de.heckie.tinyb.common;

import java.math.BigInteger;
import java.util.Map;
import java.util.stream.Collectors;

import tinyb.BluetoothDevice;
import tinyb.BluetoothGattCharacteristic;
import tinyb.BluetoothManager;

public class Utils {

  public static BluetoothDevice getDevice(String mac, BluetoothManager manager) throws DeviceNotFoundException {

    BluetoothDevice device = manager.getDevices().stream().filter(d -> d.getAddress().equalsIgnoreCase(mac))
        .findFirst().orElse(null);

    if (device == null) {
      throw new DeviceNotFoundException("Device with mac " + mac + " not available");
    }

    return device;

  }

  public static Map<String, BluetoothGattCharacteristic> getCharacteristics(BluetoothDevice device) {

    Map<String, BluetoothGattCharacteristic> characteristics = device.getServices().stream()
        .map(s -> s.getCharacteristics()).flatMap(c -> c.stream())
        .collect(Collectors.toMap(c -> c.getUUID(), c -> c));

    return characteristics;

  }

  public static void connect(BluetoothDevice device) throws BluetoothConnectionException {

    device.connect();

    try {
      if (!device.getConnected()) {
        throw new BluetoothConnectionException("Unable to connect to " + device.getAddress());
      }
      int i = 0;
      while (!device.getServicesResolved() && ++i < 5) {
        Thread.sleep(1000);
      }
    } catch (InterruptedException e) {
      throw new BluetoothConnectionException("Resolving services interrupted");
    }

    if (!device.getServicesResolved()) {
      throw new BluetoothConnectionException("Resolving services failed. Timed out");
    }
  }

  public static void disconnect(BluetoothDevice device) throws BluetoothConnectionException {

    boolean disconnected = device.disconnect();
    if (!disconnected) {
      throw new BluetoothConnectionException("Disconnection failed");
    }

  }

  public static String readString(BluetoothGattCharacteristic characteristic) {
    byte[] bytes = characteristic.readValue();
    return new String(bytes);
  }

  public static BigInteger readBigInteger(BluetoothGattCharacteristic characteristic) {
    return new BigInteger(characteristic.readValue());
  }

  public static int readInteger(BluetoothGattCharacteristic characteristic) {
    return readBigInteger(characteristic).intValue();
  }

  public static boolean writeBytes(BluetoothGattCharacteristic characteristic, byte[] bytes) {
    return characteristic.writeValue(bytes);
  }

}