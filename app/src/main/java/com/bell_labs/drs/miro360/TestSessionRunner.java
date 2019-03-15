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

import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;

import com.bell_labs.drs.miro360.config.Playlist;
import com.bell_labs.drs.miro360.config.Sequence;

/**
 * Pair class of Miro360Main, used to handle programmed testing sessions
 */

public class TestSessionRunner {

    private static final String TAG = TestSessionRunner.class.getSimpleName();

    public static final String DEFAULT_PLAYLIST = "bell-labs/miro360.json";
    public static final String DEFAULT_SESSION_TAG = "miro360";

    Playlist mPlaylist = null;
    private final Miro360Main mMiro360Main;
    private final String mSessionTag;

    private EventWriter mEventWriter = null;
    private String mPlaylistPath = DEFAULT_PLAYLIST;
    public static final String S_IDLE = "idle";
    public static final String S_VIDEO = "video";
    public static final String S_QUESTION = "questionnaire";

    private boolean mPlaying = false;
    private int mCurrentItem = 0;
    private String mCurrentStep = S_IDLE;

    private PostSequenceEvaluation mPostSeqEval;
    private InSequenceEvaluation mInSeqEval;

    private Resources mRes;

    public TestSessionRunner(Miro360Main main) {
        this(main, DEFAULT_SESSION_TAG, DEFAULT_PLAYLIST);
    }

    public TestSessionRunner(Miro360Main main, String tag, String playlistPath) {
        mMiro360Main = main;
        mSessionTag = tag != null ? tag : DEFAULT_SESSION_TAG;
        mPlaylistPath = playlistPath != null ? playlistPath : DEFAULT_PLAYLIST;
        mRes = mMiro360Main.mActivity.getResources();

    }


    public void play() {
        startPlay(mPlaylistPath);
    }

    public void stop() {

    }

    public boolean isPlaying() {
        return mPlaying;
    }

    public void mainClick() {
        if(!mPlaying) {
            Log.d(TAG, "Event, but not playing yet");
        }
        else if(mCurrentStep.equals(S_QUESTION)) {
            mPostSeqEval.vote();
            if(mPostSeqEval.hasFinished())
                step();
        }
        else if(mCurrentStep.equals(S_VIDEO)) {
            mInSeqEval.selectResponse();
        }
        else if(mCurrentStep.equals(S_IDLE) && mCurrentItem < mPlaylist.sequences.length) {
            step();
        }
    }

    public void secondaryClick() {

    }

    public void slider(int increment) {
        if(mCurrentStep.equals(S_VIDEO)) {
            mInSeqEval.increaseScore(increment);
        }
    }

    public void onStep() {
        if(mPlaying && mEventWriter != null) {
            float[] lookAt = mMiro360Main.getGVRContext().getMainScene().getMainCameraRig().getLookAt();
            mEventWriter.writeEvent("LOOK_AT", lookAt);
        }
    }


    private void startPlay(String playlistPath) {
        if(mPlaying)
            return;
        mPlaylist = Playlist.fromFile(playlistPath, mRes);
        Log.d(TAG, "PLAYING: \n" + mPlaylist);
        mEventWriter = new EventWriter(mSessionTag);
        // In-sequence question
        mInSeqEval = new InSequenceEvaluation(mMiro360Main.getGVRContext(), mMiro360Main.mQuestionnaireScene, mMiro360Main.mSlider);
        mInSeqEval.setEventWriter(mEventWriter);

        mPlaying = true;
        idleMessage();
    }

    private void finishPlay() {
        mPlaying = false;
        mMiro360Main.mMessage.setText(mRes.getString(R.string.test_finished));
        mMiro360Main.mMessage.setEnable(true);
        mEventWriter.close();
        mEventWriter = null;
        mInSeqEval.setEventWriter(null);
        mInSeqEval = null;
    }

    private void step() {
        Log.v(TAG, "New step");
        if(mPlaylist == null) {
            Log.d(TAG, "No playlist. Nothing to do.");
            finishPlay();
            return;
        }
        mMiro360Main.getGVRContext().getMainScene().getMainCameraRig().resetYaw();
        boolean next = true;
        while (next) {
            next = false;
            switch (mCurrentStep) {
                case S_IDLE:
                    // Idle done. Move to video
                    mMiro360Main.mMessage.setEnable(false);
                    mCurrentStep = S_VIDEO;
                    Log.d(TAG, "Launching video " + mPlaylist.sequences[mCurrentItem].uri);
                    new SessionRunner().execute(mPlaylist.sequences[mCurrentItem]);
                    break;

                case S_VIDEO:
                    // Video done: move to questionnaire
                    mCurrentStep = S_QUESTION;
                    Log.d(TAG, "Launching post-experience questionnaire");
                    mPostSeqEval = new PostSequenceEvaluation(mMiro360Main.mActivity,
                            mPlaylist,
                            mPlaylist.sequences[mCurrentItem].post_seq_questions,
                            mMiro360Main.mQuestionnaireScene,
                            mEventWriter);
                    mPostSeqEval.enable();
                    Log.d(TAG, mPostSeqEval.toString());
                    if (mPostSeqEval.hasFinished())
                        next = true; // Handle no-question situation
                    break;

                case S_QUESTION:
                    // Questionnaire done: move to next step
                    mPostSeqEval.disable();
                    mCurrentStep = S_IDLE;
                    if(mPlaylist.pause_after_sequence) {
                        Log.d(TAG, "Idle message");
                        idleMessage();
                    }
                    else {
                        next = true; // Skip pause --> go for next video or end
                    }
                    mCurrentItem++;
                    break;
            }
            if (mCurrentItem < mPlaylist.sequences.length) {
                mEventWriter.setCurrentState(mCurrentItem, mPlaylist.sequences[mCurrentItem], mCurrentStep);
                Log.d(TAG, "Changing to " + mCurrentItem + "." + mCurrentStep);
            } else {
                Log.d(TAG, "Done");
                finishPlay();
                return; // No more "step" iterations, regardless the value of "next"
            }
        }

    }

    private void idleMessage() {
        mMiro360Main.mMessage.setText( mRes.getString(R.string.click_to_continue));
        mMiro360Main.mMessage.setEnable(true);
    }

    // Yes, I know this is a horrible sequential implementation instead of a proper
    // event-based one. We _might_ consider refactoring this eventually...
    private class SessionRunner extends AsyncTask<Sequence, Void, Void> {


        private long millis(long t_question, long t_clear, long t_next) {

            return Math.max(Math.min(Math.min(t_question, t_clear), t_next) - System.currentTimeMillis(), 0);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            step();
        }

        @Override
        protected Void doInBackground(Sequence... vertigoSquences) {

            final Sequence sequence = vertigoSquences[0];

            sequence.prepareVideo(mMiro360Main.mActivity, mMiro360Main.mVideo, mMiro360Main.sphere_rotation,
                    (MediaPlayer) mMiro360Main.mPlayer.getPlayer());

            mMiro360Main.mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sequence.startVideo();
                    mMiro360Main.mVideo.setEnable(true);
                    mInSeqEval.setEnable(true, sequence.in_seq_method,
                            mPlaylist.getScale(sequence.in_seq_scale));

                }
            });

            long now = System.currentTimeMillis();
            final long NEVER = now + 3600*24*365; // effectively disable  by launching into future
            long t_end = now + (long) (sequence.duration * 1000);
            long t_next_question = NEVER;
            long t_next_clear = NEVER;
            if(sequence.ssdqe_period > 0)
                t_next_question = now + (long)(sequence.ssdqe_start * 1000);


            int n_ssdqe = 0;
            
            while (System.currentTimeMillis() < t_end) {
                try {
                    Thread.sleep(millis(t_next_question, t_next_clear, t_end));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(System.currentTimeMillis() > t_next_question) {
                    t_next_clear = t_next_question + (long)(sequence.ssdqe_duration * 1000);
                    t_next_question += (long)(sequence.ssdqe_period * 1000);
                    n_ssdqe++;
                    Log.d(TAG, "Launching SSDQE question: " + n_ssdqe + " of " + sequence.ssdqe_total_number);
                    mMiro360Main.mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mInSeqEval.show();
                        }
                    });
                    if(n_ssdqe >= sequence.ssdqe_total_number) {
                        t_next_question = t_end + 100000; // All questionnaires done -> Disable
                    }
                }
                if(System.currentTimeMillis() > t_next_clear) {
                    t_next_clear = NEVER;
                    mMiro360Main.mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mInSeqEval.clear();
                        }
                    });
                }
            }

            mMiro360Main.mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mInSeqEval.setEnable(false);
                    sequence.stopVideo();
                    mMiro360Main.mVideo.setEnable(false);

                }
            });


            return null;
        }
    }

}
