package com.bell_labs.drs.miro360.config;

import android.content.res.Resources;

import com.bell_labs.drs.miro360.R;

public class Scale {
    public static final String ACR = "acr";
    public static final String DCR = "dcr";
    public static final String VERTIGO = "vertigo";
    public static final String DIZZY = "dizzy";
    public static final String LIKERT = "likert";

    public static final String[] SCALES = {ACR, DCR, VERTIGO, DIZZY, LIKERT};


    public String name = Sequence.NONE;
    public String[] scores = new String[0];

    public static Scale Create(String name, Resources res) {
        switch (name) {
            case ACR:
                return new Scale(Scale.ACR, new String[]{
                        res.getString(R.string.acr_1),
                        res.getString(R.string.acr_2),
                        res.getString(R.string.acr_3),
                        res.getString(R.string.acr_4),
                        res.getString(R.string.acr_5)
                });
            case DCR:
                return new Scale(Scale.DCR, new String[]{
                        res.getString(R.string.dcr_1),
                        res.getString(R.string.dcr_2),
                        res.getString(R.string.dcr_3),
                        res.getString(R.string.dcr_4),
                        res.getString(R.string.dcr_5)
                });
            case VERTIGO:
                return new Scale(Scale.VERTIGO, new String[]{
                        res.getString(R.string.vertigo_1),
                        res.getString(R.string.vertigo_2),
                        res.getString(R.string.vertigo_3),
                        res.getString(R.string.vertigo_4),
                        res.getString(R.string.vertigo_5)
                });
            case DIZZY:
                return new Scale(Scale.DIZZY, new String[]{
                        res.getString(R.string.dizzy_1),
                        res.getString(R.string.dizzy_2),
                        res.getString(R.string.dizzy_3),
                        res.getString(R.string.dizzy_4),
                        res.getString(R.string.dizzy_5)
                });
            case LIKERT:
                return new Scale(Scale.LIKERT, new String[]{
                        res.getString(R.string.likert_1),
                        res.getString(R.string.likert_2),
                        res.getString(R.string.likert_3),
                        res.getString(R.string.likert_4),
                        res.getString(R.string.likert_5)
                });
        }

        return null;
    }

    public Scale() {}

    public Scale(String name, String[] scores) {
        this.name = name;
        this.scores = scores;
    }

    public String scoreText(int level) {
        if(level <= scores.length)
            return scores[level-1];
        return "Unknown";
    }
}
