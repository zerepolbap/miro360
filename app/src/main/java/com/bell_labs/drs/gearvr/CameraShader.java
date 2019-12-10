package com.bell_labs.drs.gearvr;

import android.content.Context;

import com.bell_labs.drs.miro360.R;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRShaderData;
import org.gearvrf.GVRShaderId;
import org.gearvrf.GVRShaderTemplate;
import org.gearvrf.utility.TextFile;

/**
 * A GLSL shader to implement Cr-based chroma-keying.
 */

public class CameraShader extends GVRShaderTemplate {

    public static final GVRShaderId ID = new GVRShaderId(CameraShader.class);

    public CameraShader(GVRContext gvrContext) {
        super("float3 u_color float u_opacity float3 u_chroma float2 u_crth float u_localmix ",
                "samplerExternalOES u_texture",
                "float3 a_position float2 a_texcoord", GLSLESVersion.VULKAN);
        Context context = gvrContext.getContext();
        setSegment("FragmentTemplate", TextFile.readTextFile(context, R.raw.cam_tex_frag));
        setSegment("VertexTemplate", TextFile.readTextFile(context, R.raw.cam_ver_frag));
    }

    protected void setMaterialDefaults(GVRShaderData material)
    {
        material.setVec3("u_chroma", 0.5f,  -0.418688f, - 0.081312f);
        material.setVec2("u_crth", 0.0625f, 0.0625f);
        material.setFloat("u_localmix", 1.0f);
        material.setVec3("u_color", 1, 1, 1);
        material.setFloat("u_opacity", 1);
    }
}
