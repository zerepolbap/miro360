package com.bell_labs.drs.miro360.config;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Environment;
import android.util.Log;

import org.gearvrf.GVRSceneObject;

import java.io.IOException;

/**
 * Representation of a single c¡369 video sequence to be played
 */

public class Sequence {

    private static final String TAG = Sequence.class.getSimpleName();

    public static final String NONE = "";
    public static final String SSCQE = "sscqe";
    public static final String SSDQE = "ssdqe";

    public static final float DEAFAULT_SSDQE_START = 30.0f;
    public static final float DEAFAULT_SSDQE_PERIOD = 40.0f;
    public static final float DEAFAULT_SSDQE_DURATION = 10.0f;
    public static final int DEFAULT_SSDQE_TOTAL_NUMBER = 1000000; //Unlimited


    /* Configurable parameters. They can be externally loaded via GsonLoader */
    public String uri = "";
    public float duration = 0.0f;
    public float start = 0.0f;
    public float orientation = 0.0f;

    // Metadata
    public String src_id = ""; // Source ID. Randomization must guarantee that two consecutive PVS do not share SRC

    // In-sequence question evaluation
    public String in_seq_method = NONE;
    public String in_seq_scale = Scale.ACR;
    public float ssdqe_start = DEAFAULT_SSDQE_START; // First SSDQE question
    public float ssdqe_period = DEAFAULT_SSDQE_PERIOD; // Frequency of SSDQE questionnaires
    public float ssdqe_duration = DEAFAULT_SSDQE_DURATION; // Duration of SSDQE questionnaires
    public int ssdqe_total_number = DEFAULT_SSDQE_TOTAL_NUMBER; // Total (max) number of SSDQE questionnaires

    // Post-sequence question evaluation
    public String[] post_seq_questions = {};

    /* Private status variables */
    private MediaPlayer mPlayer;


    public Sequence() {}


    public void prepareVideo(Activity activity, GVRSceneObject videoObject, float orientation_offset, MediaPlayer player) {
        mPlayer = player;
        setPlayerDataSource(activity);
        try {
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(duration <= 0.0f) {
            // Compute duration automatically
            duration = mPlayer.getDuration() / 1000.0f - start;
        }
        videoObject.getTransform().setRotationByAxis(orientation + orientation_offset, 0, 1, 0);
    }

    public void startVideo() {
        if(mPlayer != null) {
            mPlayer.seekTo((int)start*1000);
            mPlayer.start();
        }
    }

    private void setPlayerDataSource(Context context) {
        try {
            Log.d(TAG, "Trying with asset");
            AssetFileDescriptor afd = context.getAssets().openFd(uri);
            mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            return;
        } catch (IOException e) {
            Log.d(TAG, "Asset not found: try with file");

        }
        try {
            mPlayer.setDataSource(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stopVideo() {
        if(mPlayer != null) {
            mPlayer.stop();
            mPlayer.reset();
            mPlayer = null;
        }
    }

}
