package za.co.inventit.reachvoice;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.microsoft.cognitive.speakerrecognition.SpeakerIdentificationRestClient;
import com.microsoft.cognitive.speakerrecognition.SpeakerVerificationRestClient;
import com.microsoft.cognitive.speakerrecognition.contract.identification.CreateProfileResponse;
import com.microsoft.cognitive.speakerrecognition.contract.identification.OperationLocation;
import com.microsoft.cognitive.speakerrecognition.contract.verification.VerificationPhrase;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddUserActivity extends AppCompatActivity {
    private static final String TAG = AddUserActivity.class.getSimpleName();

    private SpeakerVerificationRestClient verClient;
    private SpeakerIdentificationRestClient idClient;
    private List<VerificationPhrase> phrases;

    private String filename;
    private UUID uuid;

    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_user_activity);

        phrases = new ArrayList<>();
        verClient = new SpeakerVerificationRestClient(getString(R.string.microsoft_azure_key));
        idClient = new SpeakerIdentificationRestClient(getString(R.string.microsoft_azure_key));

        spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                updatePhrase();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        // get the list of phrases
        new ServerCallGetPhrases().execute("");

        // record button
        View record = findViewById(R.id.record);
        final MediaInteractor media = new MediaInteractor();
        filename = getApplicationInfo().dataDir + "/voice_audio";
        record.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.d(TAG, "STARTING audio recording");
                    media.startRecording(filename);
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.d(TAG, "STOPPING audio recording");
                    media.stopRecording();
                }

                return true;
            }
        });

        // add button
        View add = findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUser();
            }
        });
    }

    private void saveUser() {

        // create azure user profile
        new ServerCallCreateProfile().execute("");

        // send to azure
        // TODO (filename)

        // save to DB
        // TODO
    }

    private void updatePhrases() {
        List<String> list = new ArrayList<String>();

        for (int i = 0; i < phrases.size(); i++) {
            VerificationPhrase phrase = phrases.get(i);
            Log.d(TAG, "Phrase = " + phrase.phrase);
            list.add(phrase.phrase);
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        updatePhrase();
    }

    private void updatePhrase() {
        TextView phrase = (TextView) findViewById(R.id.phrase);
        phrase.setText(spinner.getSelectedItem().toString());
    }

    private class ServerCallGetPhrases extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            // pull list of phrases
            try {
                phrases = verClient.getPhrases(getString(R.string.microsoft_azure_local));
                return 0;
            }
            catch (Exception e) {
                Log.e(TAG, "Error pulling Phrases" + e.toString());
                return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            updatePhrases();
        }
    }

    private class ServerCallEnroll extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            // enroll
            try {
                InputStream stream;
                stream = new FileInputStream(filename);

                OperationLocation loc = idClient.enroll(stream ,uuid);
                Log.d(TAG, "Got location info back");

                // TODO ????

                return 0;
            }
            catch (Exception e) {
                Log.e(TAG, "Error pulling Phrases" + e.toString());
                return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            // TODO
        }
    }

    private class ServerCallCreateProfile extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            // create profile
            try {
                CreateProfileResponse response = idClient.createProfile(getString(R.string.microsoft_azure_local));
                uuid = response.identificationProfileId;
                Log.d(TAG, "Got UUID " + uuid);

                return 0;
            }
            catch (Exception e) {
                Log.e(TAG, "Error pulling Phrases" + e.toString());
                return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            new ServerCallEnroll().execute("");
        }
    }
}
