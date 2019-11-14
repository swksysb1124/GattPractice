package crop.computer.askey.gattpractice;

import java.util.UUID;

public class CurrentTimeProfile implements GattProfile {

    private static final String SERVICE_CURRENT_TIME = "00001805-0000-1000-8000-00805f9b34fb";
    private static final String CHARACTERISTIC_CURRENT_TIME = "00002a2b-0000-1000-8000-00805f9b34fb";
    private static final String CHARACTERISTIC_LOCAL_TIME_INFORMATION = "00002a0f-0000-1000-8000-00805f9b34fb";

    public static final UUID UUID_SERVICE_CURRENT_TIME = UUID.fromString(SERVICE_CURRENT_TIME);
    public static final UUID UUID_CHARACTERISTIC_CURRENT_TIME = UUID.fromString(CHARACTERISTIC_CURRENT_TIME);
    public static final UUID UUID_CHARACTERISTIC_LOCAL_TIME_INFORMATION = UUID.fromString(CHARACTERISTIC_LOCAL_TIME_INFORMATION);
}
