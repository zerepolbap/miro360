/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gearvrf;

import android.content.Context;

import org.gearvrf.utility.TextFile;



public class FixedGVROESVerticalStereoShader extends GVRShader
{
    public static final GVRShaderId ID = new GVRShaderId(FixedGVROESVerticalStereoShader.class);

    public FixedGVROESVerticalStereoShader(GVRContext gvrContext)
    {
        super("float3 u_color float u_opacity ",
                "samplerExternalOES u_texture",
                "float3 a_position float2 a_texcoord", GLSLESVersion.VULKAN);
        Context context = gvrContext.getContext();
        setSegment("FragmentTemplate", TextFile.readTextFile(context, com.bell_labs.drs.miro360.R.raw.fixed_oes_vertical_frag));
        setSegment("VertexTemplate", TextFile.readTextFile(context, R.raw.pos_tex_ubo));
    }

    protected void setMaterialDefaults(GVRShaderData material)
    {
        material.setVec3("u_color", 1, 1, 1);
        material.setFloat("u_opacity", 1);
    }
}