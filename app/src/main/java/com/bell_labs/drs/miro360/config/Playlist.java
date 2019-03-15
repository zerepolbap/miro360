package com.bell_labs.drs.miro360.config;

import android.content.res.Resources;
import android.util.Log;

import com.bell_labs.drs.miro360.util.GsonLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Playlist {

    private final String TAG = this.getClass().getSimpleName();

    // Configurable elements, as read from Gson
    public boolean randomize = false;
    public boolean pause_after_sequence = false;
    public int max_random_tries = 1000;
    public Scale[] scales = new Scale[0];
    public Questionnaire[] questionnaires = new Questionnaire[0];
    public Sequence[] items = new Sequence[0];

    // Internal names
    private Map<String, Scale> mScalesMap = new HashMap<String, Scale>();
    private Map<String, Questionnaire> mQuestionsMap = new HashMap<String, Questionnaire>();

    public static Playlist fromFile(String playlistPath, Resources res) {
      Playlist playlist = GsonLoader.load(playlistPath, Playlist.class);
        playlist.init(res);
        if(playlist.randomize)
            playlist.shuffle();
      return playlist;
    }

    public Scale getScale(String name) {
        return mScalesMap.get(name);
    }

    public Questionnaire getQuestion(String name) {
        return mQuestionsMap.get(name);
    }

    private void init(Resources res) {
        //Build map of scales
        // Default scales
        for(String name: Scale.SCALES) {
            mScalesMap.put(name, Scale.Create(name, res));
        }

        // User-defined scales
        for(Scale scale: scales) {
            if(scale.scores != null && scale.scores.length == 5) // Verify correct structure
                mScalesMap.put(scale.name, scale);
        }

        // Build map of questionnaires
        // Default questionnaires
        for(String name: Questionnaire.QUESTIONS) {
            mQuestionsMap.put(name, Questionnaire.Create(name, res));
        }

        // User-defined questionnaires
        for(Questionnaire questionnaire : questionnaires) {
            mQuestionsMap.put(questionnaire.name, questionnaire);
        }
    }

    private void shuffle() {
        // Shuffle a number of times, until the sequence is sorted safely with no consecutive src_ids are equal
        for(int i=0; i<max_random_tries; i++) {
            doShuffle();
            if(orderIsSafe())
                return;
        }
        Log.w(TAG, "Unable to get a correct order after "+ max_random_tries +" tries!");
    }

    private void doShuffle()
    {
        int index;
        Sequence temp;
        Random random = new Random();
        for (int i = items.length - 1; i > 0; i--)
        {
            index = random.nextInt(i + 1);
            temp = items[index];
            items[index] = items[i];
            items[i] = temp;
        }
    }

    private boolean orderIsSafe(){
        for (int i = 0; i < items.length - 1;  i++)
        {
            if(!items[i].src_id.isEmpty() &&
                    items[i].src_id.equals(items[i+1].src_id))
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return GsonLoader.toString(this);
    }
}
