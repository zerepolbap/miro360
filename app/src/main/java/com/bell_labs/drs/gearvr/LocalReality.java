package com.bell_labs.drs.gearvr;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.util.Log;
import android.util.Size;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;

/**
 * Local Reality: show locally-captured reality in front of the user.
 */

public class LocalReality {

    public static final String TAG = LocalReality.class.getSimpleName();

    public static final float[] CHROMA_RED = {0.5f,  -0.418688f, -0.081312f};

    // Configurable information
    MeshData mesh_data = new MeshData();
    int resolution_width = 720;
    int resolution_height = 720;
    float aspect_ratio = 1.0f;
    float scale = 200.0f;
    float z_position = -100.0f;
    float min_chroma = 0.0625f;
    float chroma_range = 0.0625f;
    float[] chroma_transform = CHROMA_RED;
    float local_mix = 1.0f;

    // Internal variables
    private GVRActivity mActivity;
    private GVRScene mMainScene;
    private SurfaceTexture mCameraTexture;
    private GVRSceneObject mCameraObject;
    private Camera2Helper mCameraHelper;
    private boolean mIsRecording = false;


    public GVRSceneObject init(GVRActivity activity, GVRContext gvrContex) {
        return init(activity, gvrContex, true);
    }

    public GVRSceneObject init(GVRActivity activity, GVRContext gvrContext, boolean addToMainScene) {
        Log.d(TAG, "Initializing");
        mActivity = activity;
        mMainScene = gvrContext.getMainScene();

        GVRExternalTexture effectTexture = new GVRExternalTexture(gvrContext);
        mCameraTexture = new SurfaceTexture(effectTexture.getId());

        GVRMesh mesh = null;
        if(mesh_data != null && !mesh_data.isEmpty()) {
            mesh = new GVRMesh(gvrContext);
            mesh.setVertices(mesh_data.vertices);
            mesh.setNormals(mesh_data.normals);
            mesh.setTexCoords(mesh_data.texcoords);
            mesh.setIndices(mesh_data.triangles);
        }
        else {
            mesh = gvrContext.createQuad(1.0f, 1.0f);
        }

        mCameraObject = new GVRSceneObject(gvrContext, mesh, effectTexture, CameraShader.ID);

        mCameraObject.getRenderData().setAlphaBlend(true);
        // By default, the source blend function is GL_ONE and the destination blend function is GL_ONE_MINUS_SRC_ALPHA.
        // In order to achieve the transparent effect you desire, the source blending function should be GL_SRC_ALPHA
        // and the destination blending function should be GL_ONE_MINUS_SRC_ALPHA.
        mCameraObject.getRenderData().setAlphaBlendFunc(0x0302, 0x0303); // Magic happens !!
        mCameraObject.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
        refreshCameraObjectSettings();

        if(addToMainScene) {
            Log.d(TAG, "Add to main scene");
            mMainScene.getMainCameraRig().addChildObject(mCameraObject);
        }

        gvrContext.registerDrawFrameListener(new GVRDrawFrameListener() {
            @Override
            public void onDrawFrame(float v) {
                mCameraTexture.updateTexImage();
            }
        });

        start();
        Log.d(TAG, "Started");
        return mCameraObject;
    }

    public void refreshCameraObjectSettings() {
        mCameraObject.getRenderData().getMaterial().setVec2("u_crth", min_chroma, chroma_range);
        mCameraObject.getRenderData().getMaterial().setVec3("u_chroma",
                chroma_transform[0], chroma_transform[1], chroma_transform[2]);
        mCameraObject.getRenderData().getMaterial().setFloat("u_localmix", local_mix);

        Log.i(TAG, "Chroma transform: (" + chroma_transform[0] + ", " +
                chroma_transform[1] + ", " + chroma_transform[2] + ") * (R,G,B)");
        Log.i(TAG, "Chroma thresholds: (" + min_chroma + "+" +
                chroma_range + ")");

        mCameraObject.getTransform().setScale(scale*aspect_ratio, scale, 1.0f);
        mCameraObject.getTransform().setPosition(0.0f, 0.0f, z_position);
    }

    public void start() {
        if(mMainScene == null) // Not initialized yet
            return;
        Log.d(TAG, "Starting");
        try {
            mCameraHelper = new Camera2Helper(mActivity, 0);
            Size previewSize = mCameraHelper.setPreferredSize(resolution_width, resolution_height);
            mCameraTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            mCameraHelper.startCapture(mCameraTexture);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        Log.d(TAG, "Stopping");
        if (mCameraHelper != null) {
            mCameraHelper.closeCamera();
        }
        mCameraHelper = null;
    }

    public void startRecording(String tag) {
        if (mCameraHelper != null) {
            if (!mIsRecording)
                mCameraHelper.startRecordingVideo(tag);
            mIsRecording = true;
        }
    }

    public void stopRecording() {
        if (mCameraHelper != null) {
            if (mIsRecording)
                mCameraHelper.stopRecordingVideo();
            mIsRecording = false;
        }
    }

    public boolean isRecording() {
        return mIsRecording;
    }

    public void setLocalMix(float newLocalMix) {
        if(newLocalMix != local_mix) {
            Log.v(TAG, "Set u_localmix uniform to " + newLocalMix);
            mCameraObject.getRenderData().getMaterial().setFloat("u_localmix", newLocalMix);
            local_mix = newLocalMix;
        }
    }

    public boolean isEnabled() {
        return mCameraObject.isEnabled();
    }

    public void setEnable(boolean enable) {
        mCameraObject.setEnable(enable);
    }

}
