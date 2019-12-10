#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable
#extension GL_OES_EGL_image_external : enable
#extension GL_OES_EGL_image_external_essl3 : enable
precision highp float;
uniform samplerExternalOES u_texture;

@MATERIAL_UNIFORMS

in vec2 diffuse_coord;
out vec4 outColor;

void main()
{
    vec4 color = texture(u_texture, diffuse_coord);
    float cr = u_chroma.r*color.r + u_chroma.g*color.g + u_chroma.b*color.b;
    float alpha = cr - u_crth.x;
    float denom = u_crth.y;
    float ag = clamp(alpha/denom, 0.0, 1.0);
    float finalpha = max(ag, u_localmix);
    outColor = vec4(color.r, color.g, color.b, finalpha);
}