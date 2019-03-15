package com.bell_labs.drs.miro360;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;

import com.bell_labs.drs.miro360.config.Scale;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IPickEvents;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;

import java.util.ArrayList;
import java.util.List;

public class QuestionnaireScene {
    public class PickHandler implements IPickEvents
    {
        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            sceneObj.getRenderData().getMaterial().setVec4("diffuse_color", PICKED_COLOR_R, PICKED_COLOR_G, PICKED_COLOR_B, PICKED_COLOR_A);
            try {
                currentId = Integer.parseInt(sceneObj.getName());
            }
            catch (NumberFormatException e) {
                currentId = RESPONSE_NONE;
            }
        }
        public void onExit(GVRSceneObject sceneObj)
        {
            sceneObj.getRenderData().getMaterial().setVec4("diffuse_color", UNPICKED_COLOR_R, UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);
            currentId = RESPONSE_NONE;
        }
        public void onNoPick(GVRPicker picker) { }
        public void onPick(GVRPicker picker) { }
        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) { }
    }

    private static final String TAG = "QuestionnaireScene";

    private static final float UNPICKED_COLOR_R = 0.7f;
    private static final float UNPICKED_COLOR_G = 0.7f;
    private static final float UNPICKED_COLOR_B = 0.7f;
    private static final float UNPICKED_COLOR_A = 1.0f;
    private static final float PICKED_COLOR_R = 1.0f;
    private static final float PICKED_COLOR_G = 0.0f;
    private static final float PICKED_COLOR_B = 0.0f;
    private static final float PICKED_COLOR_A = 1.0f;
    private static final int TEXT_COLOR = Color.WHITE;
    private static final float QUAD_X = 4.0f;
    private static final float QUAD_Y = 1.0f;
    private static final float DEPTH = -10.0f;
    private static final float ANGLE = 30;
    private static final float TITLE_X = 0.0f;
    private static final float TITLE_Y = 1.5f;
    private static final float TITLE_Z = -10.0f;
    private static final float TITLE_SIZE = 20.0f;

    public static int RESPONSE_NONE = -1;

    private GVRActivity mActivity;
    private Resources mRes;
    private Scale mScale;
    private GVRContext mGVRContext = null;
    private List<GVRSceneObject> mObjects = new ArrayList<GVRSceneObject>();
    private GVRTextViewSceneObject mTitle = null;
    private GVRSceneObject mCursor;
    private IPickEvents mPickHandler = new PickHandler();
    private GVRPicker mPicker;
    private int currentId = RESPONSE_NONE;
    private boolean mEnabled = true;

    QuestionnaireScene(GVRContext gvrContext) {
        mActivity = gvrContext.getActivity();
        mGVRContext = gvrContext;
        mRes = mActivity.getResources();
        mScale = Scale.Create(Scale.ACR, mRes);

        GVRScene mainScene = mGVRContext.getMainScene();


        mainScene.getEventReceiver().addListener(mPickHandler);
        mPicker = new GVRPicker(gvrContext, mainScene);

        //Create cursor
        mCursor = new GVRSceneObject(mGVRContext,
                mGVRContext.createQuad(1f, 1f),
                mGVRContext.getAssetLoader().loadTexture(new GVRAndroidResource(mGVRContext, R.raw.cursor)));
        mCursor.getRenderData().setDepthTest(false);
        mCursor.getRenderData().setRenderingOrder(100000);
        mCursor.getTransform().setPosition(0.0f, 0.0f, -5.0f);
        mainScene.getMainCameraRig().addChildObject(mCursor);

        /*
         * Adding Boards
         */
        GVRSceneObject object = getTextBoard(5);
        mainScene.addSceneObject(object);
        mObjects.add(object);

        object = getTextBoard(4);
        mainScene.addSceneObject(object);
        mObjects.add(object);

        object = getTextBoard(3);
        mainScene.addSceneObject(object);
        mObjects.add(object);

        object = getTextBoard(2);
        mainScene.addSceneObject(object);
        mObjects.add(object);

        object = getTextBoard(1);
        mainScene.addSceneObject(object);
        mObjects.add(object);

    }

    public void enable() {
        enable(mScale, false);
    }

    public void enable(Scale scale, boolean center) {
        enable(scale, center, null);
    }

    public void enable(Scale scale, boolean center, String heading) {
        mScale = scale;
        float offset = 0;
        if(center) {
            float[] lookAt = mGVRContext.getMainScene().getMainCameraRig().getLookAt();
            float currentYaw = (float)(Math.atan2(lookAt[0], lookAt[2]) * 180 / Math.PI);
            offset = currentYaw + 180;
            Log.d(TAG, "Current Yaw is: " + currentYaw);
        }
        for(GVRSceneObject object : mObjects) {
            int level = Integer.parseInt(object.getName());
            GVRTextViewSceneObject textObj = (GVRTextViewSceneObject) object.getChildByIndex(0);
            textObj.setText(mScale.scoreText(level));
            object.getTransform().reset();
            object.getTransform().setPosition(0.0f, 0.0f, DEPTH);
            object.getTransform().rotateByAxisWithPivot((level-3)*ANGLE + offset,
                    0, 1, 0, 0, 0, 0);
        }
        if(heading != null) {
            mTitle = new GVRTextViewSceneObject(mGVRContext, "title", heading, GVRTextViewSceneObject.DEFAULT_FONT,
                    GVRTextViewSceneObject.justifyTypes.MIDDLE, 1.0f, TITLE_SIZE, GVRTextViewSceneObject.fontStyleTypes.PLAIN);
            mTitle.getTransform().setPosition(TITLE_X, TITLE_Y, TITLE_Z);
            mTitle.setTextColor(Color.BLACK);
            if(center)
                mTitle.getTransform().rotateByAxisWithPivot(offset, 0, 1, 0, 0, 0, 0);
            mGVRContext.getMainScene().addSceneObject(mTitle);
        }
        setEnable(true);
    }

    public void disable() {
        if(mTitle != null) {
            mTitle.setEnable(false);
            mGVRContext.getMainScene().removeSceneObject(mTitle);
            mTitle = null;
        }
        setEnable(false);
    }

    private void setEnable(boolean enable) {
        mCursor.setEnable(enable);
        for(GVRSceneObject object : mObjects)
            object.setEnable(enable);
        mEnabled = enable;
    }

    public int getResponse() {
        return mEnabled ? currentId : RESPONSE_NONE;
    }

    private GVRSceneObject getColorBoard(float width, float height) {
        GVRMaterial material = new GVRMaterial(mGVRContext, GVRShaderType.Phong.ID);
        material.setVec4("diffuse_color", UNPICKED_COLOR_R,
                UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);
        GVRSceneObject board = new GVRSceneObject(mGVRContext, width, height);
        board.getRenderData().setMaterial(material);
        return board;
    }

    private GVRSceneObject getTextBoard(int id) {
        GVRSceneObject board = getColorBoard(QUAD_X, QUAD_Y);
        board.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        GVRTextViewSceneObject textObj = new GVRTextViewSceneObject(mGVRContext, QUAD_X, QUAD_Y, mScale.scoreText(id));
        textObj.setJustification(GVRTextViewSceneObject.justifyTypes.MIDDLE);
        textObj.setTextColor(TEXT_COLOR);
        board.addChildObject(textObj);
        board.getTransform().setPosition(0.0f, 0.0f, DEPTH);
        board.getTransform().rotateByAxisWithPivot((id-3)*ANGLE, 0, 1, 0, 0, 0, 0);
        board.attachComponent(new GVRMeshCollider(mGVRContext, false));
        board.setName(""+id);
        return board;
    }

}
