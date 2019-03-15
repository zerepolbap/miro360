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
