package za.co.inventit.reachvoice;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

import za.co.inventit.reachvoice.db.Database;

/**
 * Main application class, for initialising various things
 * Created by Laurie on 2017/07/20.
 */

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialise the database (Realm)
        Database.init(getApplicationContext());

        // For using vectors on old phones
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

}
