package za.co.inventit.reachvoice;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * Class responsible for device interacting with media (specifically MediaRecorder and MediaPlayer)
 *
 * <p/>
 * Created by Laurie on 2017/03/16.
 */

public class MediaInteractor {
    public static final String TAG = MediaInteractor.class.getSimpleName();
    /**
     * The voice recording will be recorded with this media recorder.
     */
    private MediaRecorder mRecorder = null;

    /**
     * The media player that will be playing the audio file
     */
    private MediaPlayer mediaPlayer = null;

    public MediaInteractor() {
    }

    public boolean startRecording(String filePath) {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(filePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioSamplingRate(44100);
        mRecorder.setAudioEncodingBitRate(96000);
        try {
            mRecorder.prepare();

            mRecorder.start();

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error in starting the recorder", e);
            releaseRecorder();
            return false;
        }
    }

    /**
     * Stops the recording and releases the resources.
     * @return True if the recording was successfully recorded, otherwise false.
     */
    public boolean stopRecording() {
        boolean successful = true;
        try {
            Log.v(TAG, "Releasing recorder resources...");

            if (mRecorder != null) {
                try {
                    mRecorder.stop();
                } catch (RuntimeException re) {
                    Log.e(TAG, "Could not stop the recording properly", re);
                }

                releaseRecorder();
            } else {
                Log.w(TAG, "No recorder resources to release");
            }
        } catch (Throwable t) {
            Log.w(TAG, ", t", t);
        }

        return successful;

    }

    public void playPauseMediaItem(final String filePath) {

        //noinspection TryWithIdenticalCatches
        try {
            Log.d(TAG, "Playing recording. Path = " + filePath);

            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();

            } else if (mediaPlayer == null) {
                destroyMediaPlayer();
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(filePath);

                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mediaPlayer.start();
                    }
                });

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                    }
                });

                mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Log.e(TAG, "Error in playing media: " + filePath + ", what = " + what + ", extra = " + extra);

                        return true;
                    }
                });
                mediaPlayer.prepareAsync();
            } else if (mediaPlayer.getCurrentPosition() >= 0) {
                mediaPlayer.start();

            }
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Media playback error", e);
        } catch (Exception e) {
            Log.e(TAG, "Media playback error", e);
        }
    }

    public void pausePlayingRecording(String filePath, int mediaItemId) {
        playPauseMediaItem(filePath);
    }

    public void rewindRecordingToStart() {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(0);
        }
    }

    public void destroyMediaPlayer() {
        // make sure the player is released
        try {

            if (mediaPlayer != null) {

                Log.d(TAG, "Destroying media player...");

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Attempt to release the recorder.
     */
    private void releaseRecorder() {
        try {
            if (mRecorder != null) {
                mRecorder.reset();
                mRecorder.release();
                mRecorder = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error when trying to release the recorder.");
        }
    }

    public MediaRecorder getRecorder() {
        return mRecorder;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
}