package crop.computer.askey.gattpractice;

import java.util.UUID;

public class RT4436WProfile implements GattProfile {

    private static final String SERVICE_WIFI_SETTING = "00002456-0000-1000-8000-00805f9b34fb";
    private static final String CHARACTERISTIC_SSID_2G = "00002222-0000-1000-8000-00805f9b34fb";
    private static final String CHARACTERISTIC_ENCRYPTION_2G = "78a810ea-2687-43e1-8d5b-d50a966c0772";
    private static final String CHARACTERISTIC_CHANNEL_2G = "0930d14f-aadf-4b6c-89e4-fb823eb9b24a";

    private static final String SERVICE_WRITE_COMMAND = "829437a2-0743-4a45-9b8c-047b62505d60";
    private static final String CHARACTERISTIC_WRITE_VALUE = "c60cdde9-1b98-44b3-82c6-f41c5644f5f2";

    public static final UUID UUID_SERVICE_WIFI_SETTING = UUID.fromString(SERVICE_WIFI_SETTING);
    public static final UUID UUID_CHARACTERISTIC_SSID_2G = UUID.fromString(CHARACTERISTIC_SSID_2G);
    public static final UUID UUID_CHARACTERISTIC_ENCRYPTION_2G = UUID.fromString(CHARACTERISTIC_ENCRYPTION_2G);
    public static final UUID UUID_CHARACTERISTIC_CHANNEL_2G = UUID.fromString(CHARACTERISTIC_CHANNEL_2G);

    public static final UUID UUID_SERVICE_WRITE_COMMAND = UUID.fromString(SERVICE_WRITE_COMMAND);
    public static final UUID UUID_CHARACTERISTIC_WRITE_VALUED = UUID.fromString(CHARACTERISTIC_WRITE_VALUE);
}
