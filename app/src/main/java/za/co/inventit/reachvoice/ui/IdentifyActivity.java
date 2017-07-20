package za.co.inventit.reachvoice.ui;

import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.microsoft.cognitive.speakerrecognition.SpeakerIdentificationRestClient;
import com.microsoft.cognitive.speakerrecognition.contract.identification.IdentificationOperation;
import com.microsoft.cognitive.speakerrecognition.contract.identification.OperationLocation;

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

public class IdentifyActivity extends AppCompatActivity {
    public static final String TAG = IdentifyActivity.class.getSimpleName();

    private SpeakerIdentificationRestClient idClient;

    private Recorder recorder;
    private String filename;

    private boolean gotAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.identify_activity);

        gotAudio = false;

        // record button
        final View record = findViewById(R.id.record);

        //final MediaInteractor media = new MediaInteractor();
        filename = getApplicationInfo().dataDir + "/voice_audio.wav";
        record.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // record
                    Log.d(TAG, "STARTING audio recording");
                    initRecording();
                    recorder.startRecording();
                    //media.startRecording(filename);
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.d(TAG, "STOPPING audio recording");
                    try {
                        recorder.stopRecording();

                        gotAudio = true;
                        new ServerCallIdentify().execute("");
                    }
                    catch (Exception e) {
                        Log.e(TAG, "Exception " + e);
                    }
                }

                return true;
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

    private class ServerCallIdentify extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            try {
                InputStream stream;
                stream = new FileInputStream(filename);

                List<UUID> uuids = new ArrayList<>();
                uuids.add(UUID.fromString("41d9bf45-2372-41e1-8587-128d475b9541"));
                uuids.add(UUID.fromString("7b4f1d87-4abe-4268-af9c-6c79af094daa"));
                uuids.add(UUID.fromString("c60581d8-33cb-437f-bdef-3e9f257cfd6e"));

                idClient = new SpeakerIdentificationRestClient(getString(R.string.microsoft_azure_key));
                OperationLocation loc = idClient.identify(stream, uuids, true);
                stream.close();

                if (loc == null) {
                    Log.d(TAG, "Location Empty");
                }
                else {
                    Log.d(TAG, "Got location info back: " + loc.Url);

                    idClient = new SpeakerIdentificationRestClient(getString(R.string.microsoft_azure_key));
                    IdentificationOperation op = idClient.checkIdentificationStatus(loc);
                    if ((op == null) || (op.processingResult == null)) {
                        Log.d(TAG, "Operation Empty");
                    } else {
                        UUID id = op.processingResult.identifiedProfileId;
                        Log.d(TAG, "MATCHED ID = " + id);
                    }
                }
                return 0;
            }
            catch (Exception e) {
                Log.e(TAG, "Error pulling Phrases" + e.toString());
                return -1;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
        }
    }
}
