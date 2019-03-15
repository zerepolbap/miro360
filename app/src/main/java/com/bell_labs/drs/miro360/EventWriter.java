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

package com.bell_labs.drs.miro360;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;

import com.bell_labs.drs.miro360.config.Sequence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class to write events to disk
 */

public class EventWriter {

    private final static String TAG = EventWriter.class.getSimpleName();

    private final static String DIRECTORY = "bell-labs/miro360";

    private BufferedWriter mWriter;
    private Handler mHandler;

    // State data
    private int mItem = 0;
    private String mUri = "";
    private String mStep = TestSessionRunner.S_IDLE;
    private long mTimeStampOffsetNs = 0;


    public EventWriter(String tag) {
        HandlerThread handlerThread = new HandlerThread("WriterThread");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());

        long wallclock_ms = System.currentTimeMillis();
        long clock_ns = SystemClock.elapsedRealtimeNanos();

        mTimeStampOffsetNs = wallclock_ms * 1000000 - clock_ns;

        File dir = new File(Environment.getExternalStorageDirectory(), DIRECTORY);
        File parent = new File(dir.getParent());
        if(!parent.exists())
            parent.mkdir();
        if(!dir.exists())
            dir.mkdir();
        String filename = tag + "_" + wallclock_ms + ".log";
        File logfile = new File(dir, filename);
        boolean shouldWritePrefix = logfile.exists();
        try {
            mWriter = new BufferedWriter(new FileWriter(logfile, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(shouldWritePrefix)
            writePrefix();
        writeEvent("START", 0);
    }

    public void setCurrentState(int item, Sequence sequence, String step) {
        mItem = item;
        mUri = sequence.uri + ":" + sequence.start;
        mStep = step;
        writeEvent("STATE_CHANGE", 0);
    }

    public void writePrefix() {
        writeLine("time_utc_ms,uptime_ns,item,uri,phase,event,value");
    }

    public void writeEvent(String key, float value) {
        writeEventString(SystemClock.elapsedRealtimeNanos(),
                key, "" + value);
    }

    public void writeEvent(String key, float[] value) {
        String joined = "" + value[0];
        for(int i=1; i < value.length; i++)
            joined += "," + value[i];
        writeEventString(SystemClock.elapsedRealtimeNanos(), key, joined);
    }

    private void writeEventString(long system_nanos, String key, String value) {
        long utc_nanos = system_nanos + mTimeStampOffsetNs;
        final String line = "" +
                utc_nanos + "," +
                mItem + "," + mUri+ "," + mStep + "," +
                key + "," + value;
        writeLine(line);
    }


    private void writeLine(final String line) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(mWriter != null)
                    try {
                        mWriter.write(line);
                        mWriter.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        });
    }

    public void close() {
        if(mWriter != null) {
            writeEvent("END", 0);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                if(mWriter != null)
                    try {
                        mWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
