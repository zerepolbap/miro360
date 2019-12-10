package com.bell_labs.drs.gearvr;

import android.graphics.Color;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRScene;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;

public class TextHUD {

    public static final float FOREVER = -1.0f;

    // Externally configurable
    int text_color = Color.WHITE;
    int BACKGROUND_COLOR = Color.BLACK;
    float quad_x = 4.0f;
    float quad_y = 1.0f;
    float pos_x = 0.0f;
    float pos_y = 2.0f;
    float depth = -10.0f;


    private GVRContext mGVRContext = null;
    private GVRTextViewSceneObject mMessage = null;
    private GVRAnimationEngine mEngine;
    private GVRAnimation mAnimation = null;

    public void init(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        mEngine = GVRAnimationEngine.getInstance(gvrContext);

        GVRScene mainScene = mGVRContext.getMainScene();

        // Message board
        mMessage = new GVRTextViewSceneObject(gvrContext, quad_x, quad_y, "");
        mMessage.setTextColor(text_color);
        mMessage.setBackgroundColor(BACKGROUND_COLOR);
        mMessage.setJustification(GVRTextViewSceneObject.justifyTypes.MIDDLE);
        mMessage.getTransform().setPosition(pos_x, pos_y, depth);
        mainScene.getMainCameraRig().addChildObject(mMessage);
        clear();
    }

    public void clear() {
        if((mAnimation != null) && !mAnimation.isFinished()) {
            mEngine.stop(mAnimation);
            mAnimation.reset();
        }
        mMessage.setText("");
        mMessage.setEnable(false);
    }

    public void show(String text, float seconds) {
        if((mAnimation != null) && !mAnimation.isFinished())
            return; // Nothing to do: an animation is already running

        if((text == null) || text.equals("")) {
            mMessage.setText("");
            mMessage.setEnable(false);
        }
        else {
            mMessage.setText(text);
            mMessage.setEnable(true);
        }

        if(seconds > 0) {
            // HACK - we are using an animation to just enable/disable the message board
            mAnimation = new GVRAnimation(mMessage, seconds) {
                @Override
                protected void animate(GVRHybridObject target, float ratio) {
                    // Do nothing!
                }
            }.setOnFinish(new GVROnFinish() {
                @Override
                public void finished(GVRAnimation animation) {
                    mMessage.setEnable(false);
                }
            }).start(mEngine);
        }
    }

}
