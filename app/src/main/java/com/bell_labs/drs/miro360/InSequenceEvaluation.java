package com.bell_labs.drs.miro360;

import android.util.Log;

import com.bell_labs.drs.miro360.config.Scale;
import com.bell_labs.drs.miro360.config.Sequence;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;

/**
 * Element to continuously monitoring score level
 */

public class InSequenceEvaluation {

    private final static String TAG = InSequenceEvaluation.class.getSimpleName();

    private static final int MAX_SCORE = 99;
    private static final int INITIAL_SCORE = 50;
    private static final int MIN_SCORE = 0;


    int score = INITIAL_SCORE;

    private GVRActivity mActivity;
    private Scale mScale;
    private SliderSceneObject mSlider = null;
    private EventWriter mEventWriter = null;
    private QuestionnaireScene mQuestion = null;
    private String mMethod = Sequence.SSCQE;

    InSequenceEvaluation(GVRContext gvrContext, QuestionnaireScene questionnaireScene, SliderSceneObject slider) {
        mActivity = gvrContext.getActivity();
        mScale = Scale.Create(Scale.ACR, mActivity.getResources());

        mQuestion = questionnaireScene;
        mSlider = slider;

        updateText();
    }

    public void setEventWriter(EventWriter eventWriter) {
        mEventWriter = eventWriter;
    }

    public void setEnable(boolean enable) {
        setEnable(enable, Sequence.SSCQE, Scale.Create(Scale.ACR, mActivity.getResources()));
    }

    public void setEnable(boolean enable, String method, Scale scale) {
        mMethod = method;
        mScale = scale;

        if(!(method.equals(Sequence.SSCQE) || (method.equals(Sequence.SSDQE))))
            return; // Nothing to do

        if(enable) {
            if (mMethod.equals(Sequence.SSCQE))
                mSlider.setEnable(true);
            setScore(INITIAL_SCORE);
        }
        else {
            mSlider.setEnable(false);
            mQuestion.disable();
        }
    }


    public void show() {
        if(!mMethod.equals(Sequence.SSDQE))
            return; // Nothing to do: we are probably at SSCQE
        mQuestion.enable(mScale, true);
    }

    public void clear() {
        if(!mMethod.equals(Sequence.SSDQE))
            return;
        mQuestion.disable();
    }

    private void setScore(int level) {
        if(level < MIN_SCORE)
            score = MIN_SCORE;
        else if(level > MAX_SCORE)
            score = MAX_SCORE;
        else
            score = level;
        updateText();
        if(mEventWriter != null && mMethod.equals(Sequence.SSCQE))
            mEventWriter.writeEvent("SSCQE", score);
    }

    public void selectResponse() {
        if(!mMethod.equals(Sequence.SSDQE))
            return;

        int response = mQuestion.getResponse();
        Log.i(TAG, "RESPONSE was '" + response + "'");
        if(response != QuestionnaireScene.RESPONSE_NONE) {
            mEventWriter.writeEvent("SSDQE", response);
            mQuestion.disable();
        }
    }

    public void increaseScore(int howmuch) {
        if(mMethod.equals(Sequence.SSCQE))
            setScore(score + howmuch);
    }


    private void updateText() {
        mSlider.setLevel(score, mScale.scoreText(1 + score/20));
    }



}
