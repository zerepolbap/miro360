package com.bell_labs.drs.miro360;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.bell_labs.drs.miro360.util.RemoteControl;

import org.ini4j.Wini;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class TestSessionSetup implements RemoteControl.Listener, TestSessionRunner.Listener {

    private static final String TAG = TestSessionSetup.class.getSimpleName();

    public static final String INI_FILE = "bell-labs/miro360.ini";

    public static final String DEFAULT_PLAYLIST = "bell-labs/miro360.json";
    public static final String DEFAULT_SESSION_TAG = "miro360";
    public static final String DEFAULT_USER_ID = "user";


    private Miro360Activity mActivity;
    private String mPlaylistLocation;
    private String mSessionTag;
    private String mUserID;

    private TestSessionRunner mSessionRunner;
    private RemoteControl mRemoteControl;

    private boolean mLaunched = false;


    TestSessionSetup(Miro360Activity activity) {
        this(activity, DEFAULT_SESSION_TAG, DEFAULT_USER_ID, DEFAULT_PLAYLIST);
    }

    TestSessionSetup(Miro360Activity activity, String tag, String userID, String playlistLocation) {
        mActivity = activity;
        mSessionTag = tag;
        mUserID = userID;
        mPlaylistLocation = playlistLocation;

    }


    public void init(Miro360Main main) {
        mSessionRunner = new TestSessionRunner(main, this);

        try {
            Wini ini = new Wini(new File(Environment.getExternalStorageDirectory(), INI_FILE));

            // Read defaults from ini file
            mPlaylistLocation = ini.get("Defaults", "playlist");
            mSessionTag = ini.get("Defaults", "tag");
            mUserID = ini.get("Defaults", "user_id");

            // Launch RemoteControl if needed
            if(ini.get("SocketIO", "enable", int.class) > 0) {
                String remoteControlUrl = ini.get("SocketIO", "uri");
                String remoteControlGroup = ini.get("SocketIO", "device");
                Log.i(TAG, "Launching remote control (" + remoteControlGroup + ") to " + remoteControlUrl);
                mRemoteControl = new RemoteControl(remoteControlUrl, remoteControlGroup);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TestSessionRunner getRunner() {
        if(mSessionRunner == null)
            Log.w(TAG, "Returning null session will probably make stuff break!");
        return mSessionRunner;
    }


    public void launch() {
        if(mRemoteControl != null) {
            // Remote control should be in charge of sending uri
            mRemoteControl.setListener(this);
            mRemoteControl.connect();
        }
        else {
            // No remote control: use existing playlist location
            downloadPlaylist(mPlaylistLocation);
        }
    }

    @Override
    public void onDRCommandReceived(RemoteControl.DRCommand command) {

        Log.d(TAG, "Received DRCommand: " + command);

        if(command.isComplete() || !mLaunched) {
            mPlaylistLocation = command.uri;
            mSessionTag = command.tag;
            mUserID = command.user_id;

            Log.i(TAG, "Launching session for user " + mUserID + " and tag " + mSessionTag + " at " + mPlaylistLocation);
            downloadPlaylist(mPlaylistLocation);
        }
        reportStatus(mSessionRunner.getState());

    }

    @Override
    public void OnTestSessionStateChange(String state) {
        Log.i(TAG, "Status cahange: " + state);
        reportStatus(state);
    }

    private void reportStatus(String state) {
        if(mRemoteControl != null) {
            mRemoteControl.sendDRStatus(new RemoteControl.DRStatus(mPlaylistLocation, mSessionTag, mUserID, state));
        }
    }


    private void downloadPlaylist(String location) {

        try {
            URL url = new URL(location);
            // It is an actual url: try to download it
            new DownloadFilesTask().execute(url);

        } catch (MalformedURLException e) {
            // It seems to be a file name. Let's use it directly
            launchPlaylist(Environment.getExternalStorageDirectory(), location);
        }

        mLaunched = true; // Blocking any new trial of launching again
        // TODO maybe we should unblock if launch fails!
    }

    private void launchPlaylist(File directory, String path) {
        mSessionRunner.startPlay(directory, path, mSessionTag, mUserID);
    }



    private class DownloadFilesTask extends AsyncTask<URL, Integer, Long> {

        File outputDir = mActivity.getCacheDir(); // context being the Activity pointer
        File outputFile;

        protected Long doInBackground(URL... urls) {
            long totalSize = 0;
            int i=0;

            try {
                Log.i(TAG, "Downloading playlist " + urls[i]);
                outputFile = File.createTempFile("miro360", ".tmp", outputDir);
                FileOutputStream fos = new FileOutputStream(outputFile);

                InputStream is = urls[i].openStream();
                DataInputStream dis = new DataInputStream(is);



                byte[] buffer = new byte[1024*1024];
                int length;

                while ((length = dis.read(buffer))>0) {
                    fos.write(buffer, 0, length);
                    totalSize += length;
                    Log.i(TAG, "Reading bytes: " + totalSize);
                }

            } catch (IOException e) {
                Log.w(TAG, "Error accessing " + urls[i]);
                e.printStackTrace();
            }


            return totalSize;
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(Long result) {
            Log.d(TAG, "Downloaded " + result + " bytes");
            launchPlaylist(outputDir, outputFile.getName());
            outputFile.delete();
        }
    }

}
