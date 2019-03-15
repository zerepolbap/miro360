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

import android.util.Log;

import com.bell_labs.drs.miro360.config.Item;
import com.bell_labs.drs.miro360.config.Playlist;
import com.bell_labs.drs.miro360.config.Questionnaire;
import com.bell_labs.drs.miro360.config.Scale;

import org.gearvrf.GVRActivity;

import java.util.ArrayList;
import java.util.Collections;

public class PostSequenceEvaluation {

    private final String TAG = this.getClass().getSimpleName();

    public static class Q {
        final Scale scale;
        final String heading;
        final String tag;
        Q(Scale scale, String heading, String tag) {
            this.scale = scale;
            this.heading = heading;
            this.tag = tag;
        }
    }

    private ArrayList<Q> mQuestions;
    private QuestionnaireScene mQuestionnaireScene;
    private EventWriter mEventWriter;
    private boolean mFinished = true;
    private Q mCurrentQ;

    private final boolean CENTER_GAZE = false;

    public PostSequenceEvaluation(GVRActivity activity, Playlist playlist, String[] tests,
                                  QuestionnaireScene questionnaireScene, EventWriter eventWriter) {
        mQuestions = new ArrayList<Q>();
        mQuestionnaireScene = questionnaireScene;
        mEventWriter = eventWriter;

        for(String test: tests) {
            addQuestion(playlist, test);
        }
    }

    public void enable() {
        mFinished = false;
        next();
    }

    public void vote() {
        if(mCurrentQ == null) {
            Log.w(TAG, "Voting without current question!!");
            return;
        }
        int response = mQuestionnaireScene.getResponse();
        Log.i(TAG, "RESPONSE ("+ mCurrentQ.tag + ") was '" + response + "'");
        if(response != QuestionnaireScene.RESPONSE_NONE) {
            mEventWriter.writeEvent(mCurrentQ.tag, response);
            next();
        }
    }

    private void next() {
        if(mQuestions.isEmpty()) {
            disable();
            return;
        }
        mQuestionnaireScene.disable(); // Clean previous status
        mCurrentQ = mQuestions.remove(0);
        mQuestionnaireScene.enable(mCurrentQ.scale, CENTER_GAZE, mCurrentQ.heading);
    }

    public void disable() {
        mFinished = true;
        mQuestionnaireScene.disable();
        mCurrentQ = null;
    }

    public boolean hasFinished() {
        return mFinished;
    }

    private void addQuestion(Playlist playlist, String test) {
        ArrayList<Q> mec = new ArrayList<Q>();
        Questionnaire questionnaire = playlist.getQuestion(test);
        for(Item item: questionnaire.items) {
            mec.add(new Q(playlist.getScale(item.scale), item.text, item.tag));
        }
        Collections.shuffle(mec);
        mQuestions.addAll(mec);
    }

    @Override
    public String toString() {
        String txt = "PostSequenceEvaluation:";
        for(Q q: mQuestions) {
            txt += " " + q.tag;
        }
        return txt;
    }
}
