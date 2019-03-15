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

public class Questionnaire {
    public static final String ACR = "acr";
    public static final String DCR = "dcr";
    public static final String VERTIGO = "vertigo";
    public static final String DIZZY = "dizzy";
    public static final String MEC = "mec";
    public static final String SPES = "spes";

    public static final String[] QUESTIONS = {ACR, DCR, VERTIGO, DIZZY, MEC, SPES};

    public String name = Sequence.NONE;
    public Item[] items = new Item[0];

    public static Questionnaire Create(String name, Resources res) {
        switch (name) {
            case ACR:
                return new Questionnaire(Questionnaire.ACR, new Item[]{
                        new Item(Scale.ACR, res.getString(R.string.acr_q), "ACR")
                });
            case DCR:
                return new Questionnaire(Questionnaire.DCR, new Item[]{
                        new Item(Scale.DCR, res.getString(R.string.dcr_q), "DCR")
                });
            case VERTIGO:
                return new Questionnaire(Questionnaire.VERTIGO, new Item[]{
                        new Item(Scale.VERTIGO, res.getString(R.string.vertigo_q), "VERTIGO")
                });
            case DIZZY:
                return new Questionnaire(Questionnaire.DIZZY, new Item[]{
                        new Item(Scale.DIZZY, res.getString(R.string.dizzy_q), "DIZZY")
                });
            case MEC:
                return new Questionnaire(Questionnaire.MEC, new Item[]{
                        new Item(Scale.LIKERT, res.getString(R.string.mec_aa), "MEC.AA"),
                        new Item(Scale.LIKERT, res.getString(R.string.mec_hci), "MEC.HCI"),
                        new Item(Scale.LIKERT, res.getString(R.string.mec_sod), "MEC.SOD"),
                        new Item(Scale.LIKERT, res.getString(R.string.mec_sppa), "MEC.SPPA"),
                        new Item(Scale.LIKERT, res.getString(R.string.mec_spsl), "MEC.SPSL"),
                        new Item(Scale.LIKERT, res.getString(R.string.mec_ssm), "MEC.SSM")
                });
            case SPES:
                return new Questionnaire(Questionnaire.SPES, new Item[]{
                        new Item(Scale.LIKERT, res.getString(R.string.spes_pa_1), "SPES.PA1"),
                        new Item(Scale.LIKERT, res.getString(R.string.spes_pa_2), "SPES.PA2"),
                        new Item(Scale.LIKERT, res.getString(R.string.spes_pa_3), "SPES.PA3"),
                        new Item(Scale.LIKERT, res.getString(R.string.spes_pa_4), "SPES.PA4"),
                        new Item(Scale.LIKERT, res.getString(R.string.spes_sl_1), "SPES.SL1"),
                        new Item(Scale.LIKERT, res.getString(R.string.spes_sl_2), "SPES.SL2"),
                        new Item(Scale.LIKERT, res.getString(R.string.spes_sl_3), "SPES.SL3"),
                        new Item(Scale.LIKERT, res.getString(R.string.spes_sl_4), "SPES.SL4")
                });
        }

        return null;
    }

    public Questionnaire(){}

    public Questionnaire(String name, Item[] items) {
        this.name = name;
        this.items = items;
    }
}
