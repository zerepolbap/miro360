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

import android.graphics.Color;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.io.CursorControllerListener;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObject;
import org.gearvrf.scene_objects.GVRVideoSceneObject.GVRVideoType;
import org.gearvrf.scene_objects.GVRVideoSceneObjectPlayer;

/*
* Miro360Main - this class stores and handles the common graphic elements for scheduled and
* interactive modes.
*/

public class Miro360Main extends GVRMain {
    private static final String TAG = Miro360Main.class.getSimpleName();


    final Miro360Activity mActivity;
    final GVRVideoSceneObjectPlayer<?> mPlayer;
    final TestSessionSetup mTestSetup;

    GVRSceneObject mVideo;
    GVRTextViewSceneObject mMessage;
    GVRAnimationEngine mEngine;
    TestSessionRunner mTest;
    QuestionnaireScene mQuestionnaireScene;
    SliderSceneObject mSlider;

    float sphere_rotation = -90; // fix to correct weird orientation of sphere textures

    private GVRPicker mPicker;

    Miro360Main(Miro360Activity activity, TestSessionSetup setup) {
        mActivity = activity;
        mPlayer =  GVRVideoSceneObject.makePlayerInstance(new MediaPlayer());
        mTestSetup = setup;
    }

    void setTest(TestSessionRunner mode) {
        mTest = mode;
    }

    @Override
    public GVRTexture getSplashTexture(GVRContext gvrContext) {

        return gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.raw.bell_labs_logo));
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onInit(GVRContext gvrContext) {
        Log.d(TAG, "onInit");
        GVRScene scene = gvrContext.getMainScene();

        // Background picture
        GVRTexture texture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.raw.background));
        GVRSphereSceneObject sphereObject = new GVRSphereSceneObject(gvrContext, 72, 144, false, texture);
        sphereObject.getTransform().setScale(300.0f, 300.0f, 300.0f);
        sphereObject.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        sphereObject.getTransform().setRotationByAxis(sphere_rotation, 0, 1, 0);
        scene.addSceneObject(sphereObject);

        // Video sphere
        GVRSphereSceneObject sphere = new GVRSphereSceneObject(gvrContext, 72, 144, false);
        GVRMesh mesh = sphere.getRenderData().getMesh();
        mVideo = new GVRVideoSceneObject(gvrContext, mesh, mPlayer, GVRVideoType.MONO);
        mVideo.setName("Video");
        mVideo.getTransform().setScale(250f, 250f, 250f);
        mVideo.getTransform().setRotationByAxis(sphere_rotation, 0, 1, 0);
        scene.addSceneObject(mVideo);

        // Animation engine (for video)
        mEngine = gvrContext.getAnimationEngine();

        // QuestionnaireScene
        mQuestionnaireScene = new QuestionnaireScene(gvrContext);
        mQuestionnaireScene.disable();

        GVRInputManager input = gvrContext.getInputManager();
        input.addCursorControllerListener(listener);

        // Message board
        mMessage = new GVRTextViewSceneObject(gvrContext, 2.0f, 2.0f, "");
        mMessage.setTextColor(Color.WHITE);
        mMessage.setTextSize(10);
        mMessage.setJustification(GVRTextViewSceneObject.justifyTypes.MIDDLE);
        mMessage.setBackgroundColor(Color.BLACK);
        mMessage.getTransform().setPosition(0.0f, -1.0f, -4.0f);
        scene.getMainCameraRig().addChildObject(mMessage);
        mMessage.setText("Loading...");
        mMessage.setEnable(true);

        // SliderSceneObject for in-sequence evaluation
        mSlider = new SliderSceneObject(gvrContext);

        // Create and launch test control logic
        mTestSetup.init(this);
        mTest = mTestSetup.getRunner();

        //Add controller if detected any
        for (GVRCursorController cursor : input.getCursorControllers()) {
            listener.onCursorControllerAdded(cursor);
        }
        Log.d(TAG, "onInit (done)");
    }

    @Override
    public void onAfterInit() {
        Log.d(TAG, "onAfterInit");
        super.onAfterInit();
        mTestSetup.launch();
    }

    @Override
    public void onStep() {
        super.onStep();
        mTest.onStep();
    }

    //Listener for controller event
    private GVRCursorController.ControllerEventListener controllerEventListener = new
            GVRCursorController.ControllerEventListener() {

                private boolean TouchpadIsDown = false;
                private float lastY = 0.0f;

                @Override
                public void onEvent(GVRCursorController gvrCursorController) {
                    KeyEvent keyEvent = gvrCursorController.getKeyEvent();

                    if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                        Log.d(TAG, "KeyEvent " + keyEvent);
                        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            mTest.mainClick();
                        } else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_A) {
                            mTest.secondaryClick();
                        }
                    }

                    MotionEvent motionEvent = gvrCursorController.getMotionEvent();
                    if(motionEvent != null) {
                        Log.v(TAG, "MotionEvent " + motionEvent);
                        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                            TouchpadIsDown = true;
                            lastY = motionEvent.getY();
                        }
                        else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                            TouchpadIsDown = false;
                        }
                        else if(motionEvent.getAction() == MotionEvent.ACTION_MOVE &&
                                TouchpadIsDown) {
                            float y = motionEvent.getY();
                            int increment = (int)((lastY-y)*100); // inc = -delta(y)
                            if(Math.abs(increment) > 99)
                                increment /= 1000; //Samsung controller!
                            if(increment != 0) {
                                Log.d(TAG, "MotionEvent " + increment + ", " + (lastY-y));
                                mTest.slider(increment);
                                lastY = y;
                            }
                        }
                    }
                }
            };

    //Listener for add/removal of a controller
    private CursorControllerListener listener = new CursorControllerListener() {

        @Override
        public void onCursorControllerAdded(GVRCursorController controller) {

            //Setup GearVR Controller
            if (controller.getControllerType() == GVRControllerType.CONTROLLER) {
                controller.addControllerEventListener(controllerEventListener);
                mActivity.enableTouchpad(false); //Disable touchpad if we find a controller
            } else {
                controller.setEnable(false);
            }
        }

        @Override
        public void onCursorControllerRemoved(GVRCursorController controller) {
            if (controller.getControllerType() == GVRControllerType.CONTROLLER) {
                controller.removeControllerEventListener(controllerEventListener);
                controller.resetSceneObject();
                mActivity.enableTouchpad(true); //Enable touchpad if we lose a controller (but should be good enough)
            }
        }
    };


}