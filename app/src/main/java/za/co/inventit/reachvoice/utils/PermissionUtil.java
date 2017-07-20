package za.co.inventit.reachvoice.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

/**
 * Utility methods that help with Marshmallow permission checks.
 * <p/>
 * Created by richard on 2016/04/19.
 */
public class PermissionUtil {

    /**
     * Write external storage permission
     */
    public static final String PERMISSION_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    /**
     * Read external storage permission
     */
    public static final String PERMISSION_READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;

    /**
     * Record audio permission
     */
    public static final String PERMISSION_RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;

    /**
     * All required permissions.
     */
    public static final String[] PERMISSIONS_ALL_REQUIRED = {
            PERMISSION_WRITE_EXTERNAL_STORAGE,
            PERMISSION_READ_EXTERNAL_STORAGE,
            PERMISSION_RECORD_AUDIO,
    };

    /**
     * Check to see if the user has granted the app the specified permissions and if the device is
     * running marshmallow or not. If it is running anything below marshmallow then true will be
     * returned.
     *
     * @param context     The current context.
     * @param permissions The list of required permissions.
     * @return True if the permissions have been granted, otherwise false.
     */
    public static boolean hasPermissions(Context context, String... permissions) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks to see if the permission rationale should be shown or not. If any of the permissions
     * should have the rationale shown then this will return true. If none should then false will
     * be returned.
     * @param activity The current activity.
     * @param permissions The list of permissions that will be checked.
     * @return True if one or more of the permissions should have the rationale shown, otherwise false.
     */
    public static boolean shouldShowRequestPermissionRationale(@NonNull Activity activity, String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }

        return false;
    }
}
