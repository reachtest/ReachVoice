package za.co.inventit.reachvoice.ui;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.microsoft.cognitive.speakerrecognition.SpeakerIdentificationRestClient;
import com.microsoft.cognitive.speakerrecognition.contract.identification.Profile;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import za.co.inventit.reachvoice.db.Database;
import za.co.inventit.reachvoice.R;
import za.co.inventit.reachvoice.db.RealmUser;
import za.co.inventit.reachvoice.events.UpdatePageEvent;

public class UserListActivity extends AppCompatActivity {
    private static final String TAG = UserListActivity.class.getSimpleName();

    private SpeakerIdentificationRestClient client;
    private List<Profile> profiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_list_activity);

        client = new SpeakerIdentificationRestClient(getString(R.string.microsoft_azure_key));
        profiles = new ArrayList<>();

        new ServerCall().execute("");
    }

    private void updatePage() {
        Log.d(TAG, "Updating page...");

        for (int i = 0; i < profiles.size(); i++) {
            Profile profile = profiles.get(i);
            Log.d(TAG, "MS Profile = " + profile.identificationProfileId);
        }

        List<RealmUser> users = Database.User.getAll();

        if (users != null) {
            for (int i = 0; i < users.size(); i++) {
                RealmUser user = users.get(i);

                Log.d(TAG, "User: Name=" + user.getName() + ", Key=" + user.getKey());
            }
        }

        // TODO SHOW USERS HERE IN APP
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @SuppressWarnings({"unused", "UnusedParameters"})
    public void onEventMainThread(final UpdatePageEvent event) {

        updatePage();

        EventBus.getDefault().removeStickyEvent(event);
    }

    private class ServerCall extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            // pull list of the profiles
            try {
                profiles = client.getProfiles();
                return 0;
            }
            catch (Exception e) {
                Log.e(TAG, "Error pulling User List" + e.toString());
                return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            updatePage();
        }
    }
}
