package za.co.inventit.reachvoice.ui;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.microsoft.cognitive.speakerrecognition.SpeakerIdentificationRestClient;
import com.microsoft.cognitive.speakerrecognition.SpeakerVerificationRestClient;
import com.microsoft.cognitive.speakerrecognition.contract.identification.CreateProfileResponse;
import com.microsoft.cognitive.speakerrecognition.contract.identification.OperationLocation;
import com.microsoft.cognitive.speakerrecognition.contract.verification.VerificationPhrase;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import omrecorder.AudioChunk;
import omrecorder.AudioRecordConfig;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.PullableSource;
import omrecorder.Recorder;
import za.co.inventit.reachvoice.R;
import za.co.inventit.reachvoice.db.Database;
import za.co.inventit.reachvoice.db.RealmUser;

public class AddUserActivity extends AppCompatActivity {
    private static final String TAG = AddUserActivity.class.getSimpleName();

    private SpeakerVerificationRestClient verClient;
    private SpeakerIdentificationRestClient idClient;
    private List<VerificationPhrase> phrases;

    private Recorder recorder;

    private boolean gotPhrases;
    private boolean gotAudio;
    private String filename;
    private UUID uuid;

    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_user_activity);

        gotAudio = false;
        gotPhrases = false;
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
        final ImageView record = (ImageView) findViewById(R.id.record);

        filename = getApplicationInfo().dataDir + "/voice_audio.wav";
        record.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!gotPhrases) {
                        Snackbar.make(findViewById(android.R.id.content), getString(R.string.still_loading), Snackbar.LENGTH_SHORT).show();
                        return false;
                    }
                    else if (gotAudio) {
                        // TODO play
                        record.setImageDrawable(ContextCompat.getDrawable(AddUserActivity.this, R.drawable.ic_pause_circle));
                    }
                    else {
                        record.setImageDrawable(ContextCompat.getDrawable(AddUserActivity.this, R.drawable.ic_microphone_on));

                        // record
                        Log.d(TAG, "STARTING audio recording");
                        initRecording();
                        recorder.startRecording();
                    }
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.d(TAG, "STOPPING audio recording");
                    if (gotPhrases) {

                        try {
                            recorder.stopRecording();
                        }
                        catch (Exception e) {
                            Log.e(TAG, "Exception " + e);
                        }
                    }

                    gotAudio = true;

                    record.setImageDrawable(ContextCompat.getDrawable(AddUserActivity.this, R.drawable.ic_play_circle));
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

    private void initRecording() {
        recorder = OmRecorder.wav(
                new PullTransport.Default(mic(), new PullTransport.OnAudioChunkPulledListener() {
                    @Override public void onAudioChunkPulled(AudioChunk audioChunk) {
                        animateVoice((float) (audioChunk.maxAmplitude() / 200.0));
                    }
                }), file());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                // user has saved the voice
                gotAudio = true;
                saveUser();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled
                gotAudio = false;
            }
        }
    }

    private void animateVoice(final float maxPeak) {
        //recordButton.animate().scaleX(1 + maxPeak).scaleY(1 + maxPeak).setDuration(10).start();
    }

    private PullableSource mic() {
        return new PullableSource.Default(
                new AudioRecordConfig.Default(
                        MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                        AudioFormat.CHANNEL_IN_MONO, 16000
                )
        );
    }

    @NonNull
    private File file() {
        return new File(filename);
    }

    private void saveUser() {
        // create azure user profile
        if (!gotAudio) {
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.still_loading), Snackbar.LENGTH_SHORT).show();
            return;
        }
        new ServerCallCreateProfile().execute("");
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
            gotPhrases = true;
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

                OperationLocation loc = idClient.enroll(stream, uuid);
                if (loc != null) {
                    Log.d(TAG, "Location Empty");
                }
                Log.d(TAG, "Got location info back: " + loc.Url);

                return 0;
            }
            catch (Exception e) {
                Log.e(TAG, "Error pulling Phrases" + e.toString());
                return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            // save the user
            EditText edit = (EditText) findViewById(R.id.name);
            String name = edit.getText().toString();
            RealmUser user = new RealmUser(uuid.toString(), name);
            Database.User.save(user);
            Log.d(TAG, "Added user " + name);

            finish();
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
