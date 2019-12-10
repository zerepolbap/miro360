package com.bell_labs.drs.miro360.util;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.socket.client.IO;
import io.socket.client.Manager;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.Transport;

/**
 * Remote controller for Miro360 -
 * uses IOSocket chat to receive control commands from
 * (and send status commands to)
 * a external entity.
 *
 */

public class RemoteControl {

    private static final String TAG = RemoteControl.class.getSimpleName();

    public static final String NO_MESSAGE = "__empty__";

    public static final String MSG_DR_COMMAND = "dr_command";
    public static final String MSG_DR_STATUS = "dr_status";
    public static final String MSG_JOIN = "join";

    // Configurable parameters
    String uri;
    String group;


    private Listener mListener;
    private Socket mSocket;

    public RemoteControl(String uri, String group) {
        this.uri = uri;
        this.group = group;
    }


    private Emitter.Listener onConnect = new Emitter.Listener(){
        @Override
        public void call(final Object... args) {
            Log.i(TAG, "Connected to " + uri);
            try {
                JSONObject data = new JSONObject()
                        .put("group", group);
                mSocket.emit(MSG_JOIN, data);
                sendDRStatus(new DRStatus("", "", "", "connected"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onDRCommand = new Emitter.Listener(){
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];
            Log.i(TAG, "Received: " + data.toString());
            if(mListener != null) {
                mListener.onDRCommandReceived(new DRCommand(data));
            }
        }
    };


    public void connect() {
        Log.i(TAG, "Connecting to " + uri);

        try {
            IO.Options opts = new IO.Options();
            opts.forceNew = true;
            mSocket = IO.socket(uri, opts);
        } catch (URISyntaxException e) {
            Log.e(TAG, "Unable to connect: syntax error in " + uri, e);
            e.printStackTrace();
            return;
        }

        mSocket.io().on(Manager.EVENT_TRANSPORT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Transport transport = (Transport) args[0];
                transport.on(Transport.EVENT_ERROR, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        Exception e = (Exception) args[0];
                        Log.e("SCSocket", "Transport error " + e);
                        e.printStackTrace();
                        e.getCause().printStackTrace();
                    }
                });
            }
        });

        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(MSG_DR_COMMAND, onDRCommand);
        mSocket.connect();
    }

    public void disconnect() {
        if(mSocket != null) {
            mSocket.disconnect();
            mSocket.off(MSG_DR_COMMAND, onDRCommand);
            mSocket = null;
        }
    }

    public void sendDRStatus(DRStatus status) {

        if(mSocket != null) {
            try {
                JSONObject json = status.toJson();
                json.put("group", group);
                Log.i(TAG, "Sending: " + json.toString());
                mSocket.emit(MSG_DR_STATUS, json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            Log.e(TAG, "Socket not connected. Message will not be sent: "
                    + status.message);
        }
    }
    
    
    public RemoteControl setListener(Listener listener) {
        mListener = listener;
        return this;
    }
    
    
    public interface Listener {

        void onDRCommandReceived(DRCommand command);
    }

    public static class DRCommand {

        public final String group;
        public final String uri;
        public final String tag;
        public final String user_id;

        public DRCommand(JSONObject data) {
            group = data.optString("group");
            uri = data.optString("uri", NO_MESSAGE);
            tag = data.optString("tag", NO_MESSAGE);
            user_id = data.optString("user_id", NO_MESSAGE);

        }

        public boolean isComplete() {
            return !(uri.equals(NO_MESSAGE) || tag.equals(NO_MESSAGE) || user_id.equals(NO_MESSAGE));
        }

    }

    public static class DRStatus {
        public final String uri;
        public final String tag;
        public final String user_id;
        public final String message;
        public final String time;


        public DRStatus(String uri, String tag, String user_id, String message) {
            this.uri = uri;
            this.tag = tag;
            this.user_id = user_id;
            this.message = message;
            this.time = getTime();
        }

        public JSONObject toJson() throws JSONException {
            return new JSONObject()
                    .put("time", time)
                    .put("uri", uri)
                    .put("tag", tag)
                    .put("user_id", user_id)
                    .put("message", message);
        }

        private String getTime() {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
            return sdf.format(c.getTime());
        }
    }
    
}
