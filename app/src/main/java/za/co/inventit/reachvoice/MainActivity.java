package za.co.inventit.reachvoice;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    public static final int PERMISSIONS_REQUEST_CODE = 1234;

    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context context = this;

        // identify
        View identify = findViewById(R.id.button_identify);
        identify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, IdentifyActivity.class);
                startActivity(intent);
            }
        });

        // add
        View add = findViewById(R.id.button_add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AddUserActivity.class);
                startActivity(intent);
            }
        });

        // list
        View list = findViewById(R.id.button_list);
        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, UserListActivity.class);
                startActivity(intent);
            }
        });

        if (!PermissionUtil.hasPermissions(this, PermissionUtil.PERMISSIONS_ALL_REQUIRED)) {
            requestPermissions();
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, PermissionUtil.PERMISSIONS_ALL_REQUIRED, PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:

                if (permissions.length == 0) {
                    Log.d(TAG, "The user cancelled the the permission requests.");
                } else {
                    boolean allPermissionsGranted = true;
                    for (int grantResult : grantResults) {
                        if (grantResult == PackageManager.PERMISSION_DENIED) {
                            allPermissionsGranted = false;
                            break;
                        }
                    }

                    if (!allPermissionsGranted) {

                        // check if we should show the rationale. This will be the first time it is denied.
                        if (PermissionUtil.shouldShowRequestPermissionRationale(this, PermissionUtil.PERMISSIONS_ALL_REQUIRED)) {
                            new AlertDialog.Builder(this)
                                    .setTitle(R.string.app_name)
                                    .setMessage(getString(R.string.yoWeNeedPermissionToRecord))
                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            requestPermissions();
                                        }
                                    })
                                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Log.d(TAG, "User chose to not try and perform the permission request again.");
                                        }
                                    }).create()
                                    .show();
                        } else {
                            // this is if the user selected the never ask again
                            new AlertDialog.Builder(this)
                                    .setMessage(R.string.heyYouDeniedPermissions)
                                    .setCancelable(false)
                                    .setNeutralButton(R.string.ok, null)
                                    .create()
                                    .show();
                        }

                    }

                    break;
                }
        }
    }
}
