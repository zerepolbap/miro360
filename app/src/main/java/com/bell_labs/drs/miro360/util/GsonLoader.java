package com.bell_labs.drs.miro360.util;

import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;

/**
 * Wrapper functions to read and load JSON from objects (using GSJON) from disk.
 *
 * Create objects in functions by using GsonLoader.load(path, obj.class) to
 * replace configurable variables from a json file.
 */

public abstract class GsonLoader {

    private static final String TAG = GsonLoader.class.getName();

    private static Gson G() {
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC,
                Modifier.PRIVATE);
        Gson gson = builder.create();
        return gson;
    }

    public static<T> T load(String filename, Class<T> classOfT) {
        return loadPath(Environment.getExternalStorageDirectory(), filename, classOfT);
    }


    public static<T> T loadPath(File directory, String fileName, Class<T> classOfT) {
        Gson gson = G();
        T config = gson.fromJson("{}", classOfT);
        try {
            Log.d(TAG, "Reading configuration from SD card: " + fileName);
            FileReader frd;
            File sdfile = new File (directory, fileName);
            int size = (int) sdfile.length();

            Log.d(TAG, "File " + sdfile + " got length " + size);

            if(size <= 0) {
                return config;
            }
            frd = new FileReader(sdfile);
            config = gson.fromJson(frd, classOfT);
            /*
            char[] buffer = new char[size];
            frd.read(buffer, 0, size);
            frd.close();
            String json = new String(buffer);
            config = gson.fromJson(json, classOfT);
            */
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return config;
    }

    public static void save(String filename, Object config) {
        savePath(Environment.getExternalStorageDirectory(), filename, config);
    }


    public static void savePath(File directory, String fileName, Object config) {
        try {
            Log.d(TAG, "Writing configuration to SD card: " + fileName);
            FileWriter fwr;
            File sdfile = new File (directory, fileName);
            fwr = new FileWriter(sdfile);
            fwr.write(toString(config));
            fwr.close();

        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e(TAG, "Error writing configuration to SD card: " + ex);

        }
    }

    public static String toString(Object config) {
        return G().toJson(config);
    }
}
