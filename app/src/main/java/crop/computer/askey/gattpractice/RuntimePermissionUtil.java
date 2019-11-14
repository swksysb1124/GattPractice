package crop.computer.askey.gattpractice;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

public class RuntimePermissionUtil {
    private final static String TAG = "LOG_TAG_"+RuntimePermissionUtil.class.getSimpleName();

    public static boolean checkPermissionGranted(Context context, String[] manifestPermissions) {
        boolean granted = true;
        for(String permission: manifestPermissions) {
            boolean temp = PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(context, permission);

            Log.w(TAG, "temp = "+temp);

            granted &= PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(context, permission);
        }

        Log.w(TAG, "granted = "+granted);
        return granted;
    }
}
