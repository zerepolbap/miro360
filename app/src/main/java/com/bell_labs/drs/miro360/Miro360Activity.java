
package com.bell_labs.drs.miro360;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;


import org.gearvrf.GVRActivity;
import org.gearvrf.utility.VrAppSettings;

public class Miro360Activity extends GVRActivity {

    private static final String TAG = Miro360Activity.class.getSimpleName();
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "OnCreate");

        Intent intent = getIntent();

        mMain = new Miro360Main(this);
        if(intent.getBooleanExtra("scheduledMode", false)) {
            String tag = intent.getStringExtra("resultTag");
            String playlist = intent.getStringExtra("playlist");
            mTest = new TestSessionRunner(mMain, tag, playlist);
        }
        else {
            mTest = new TestSessionRunner(mMain);
        }
        mMain.setTest(mTest);
        setMain(mMain);
        Log.d(TAG, "OnCreate (done)");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.v(TAG, "MotionEvent " + event);
        super.onTouchEvent(event);
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            lastDownTime = event.getDownTime();
            TouchpadIsDown = true;
            lastY = event.getY();
        }

        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            if (event.getEventTime() - lastDownTime < 200) {
                mTest.mainClick();
            }
            else if(event.getEventTime() - lastDownTime > 2000) {
                mTest.secondaryClick();
            }
            TouchpadIsDown = false;
        }
        if (event.getActionMasked() == MotionEvent.ACTION_MOVE && TouchpadIsDown) {
            float y = event.getY();
            int increment = (int)((lastY-y)/10); // inc = -delta(y)
            if(increment != 0) {
                Log.d(TAG, "MotionEvent " + increment);
                mTest.slider(increment);
                lastY = y;
            }
        }

            return true;
    }

    @Override
    protected void onInitAppSettings(VrAppSettings appSettings) {
        super.onInitAppSettings(appSettings);
        Log.d(TAG, "onInitAppSettings");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    private boolean TouchpadIsDown = false;
    private float lastY = 0.0f;
    private long lastDownTime;
    private Miro360Main mMain;
    private TestSessionRunner mTest;

}
