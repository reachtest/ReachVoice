package za.co.inventit.reachvoice;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.File;

import omrecorder.AudioChunk;
import omrecorder.AudioRecordConfig;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.PullableSource;
import omrecorder.Recorder;

public class IdentifyActivity extends AppCompatActivity {
    public static final String TAG = IdentifyActivity.class.getSimpleName();

    private Recorder recorder;
    private String filename;

    private boolean gotAudio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.identify_activity);

        // init database
        Database.init(this);

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
}
