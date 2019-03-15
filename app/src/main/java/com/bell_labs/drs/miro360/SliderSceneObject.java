package com.bell_labs.drs.miro360;

import android.graphics.Color;

import org.gearvrf.GVRContext;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;

import java.util.Locale;

public class SliderSceneObject extends GVRTextViewSceneObject {
    private static final int TEXT_COLOR = Color.WHITE;
    private static final int BACKGROUND_COLOR = Color.BLACK;
    private static final float QUAD_X = 4.0f;
    private static final float QUAD_Y = 1.0f;
    private static final float POS_X = 0.0f;
    private static final float POS_Y = 2.0f;
    private static final float DEPTH = -10.0f;


    public SliderSceneObject(GVRContext gvrContext) {
        super(gvrContext, QUAD_X, QUAD_Y, "");
        setTextColor(TEXT_COLOR);
        setBackgroundColor(BACKGROUND_COLOR);
        setJustification(GVRTextViewSceneObject.justifyTypes.MIDDLE);
        getTransform().setPosition(POS_X, POS_Y, DEPTH);
        gvrContext.getMainScene().getMainCameraRig().addChildObject(this);
        setEnable(false);
    }

    public void setLevel(int score, String text) {
        setText(String.format(Locale.ENGLISH, "%02d", score) + " - " + text);
    }

}
