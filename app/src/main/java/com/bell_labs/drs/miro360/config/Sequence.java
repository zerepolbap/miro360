/*
MIT License

Copyright (c) 2019 Nokia

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

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
    public boolean stereo = false;

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


    public void prepareVideo(Activity activity, MediaPlayer player) {
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
