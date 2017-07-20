package za.co.inventit.reachvoice;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

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

        PermissionUtil.hasPermissions(this, PermissionUtil.PERMISSIONS_ALL_REQUIRED);

        PermissionUtil.shouldShowRequestPermissionRationale(this, PermissionUtil.PERMISSIONS_ALL_REQUIRED);
    }
}
