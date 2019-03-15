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
