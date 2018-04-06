#version 330 core

uniform sampler2D tex;
uniform sampler1D colorMap;

in vec2 fragTextureCoordinate;
out vec4 outColor;

void main() {
    vec4 texColor = texture(tex, fragTextureCoordinate);
    float opacity = texColor.r;

    /*
        OpenGL transparancy uses a technique called 'alpha blending'. Every time a translucent pixel is drawn,
        the new color is combined with the value that was already in the buffer.
        The alpha value of the new color determines how much of the new color is visible versus the previous color.
        This method does not account for drawing order. Depending on which object is drawn first, different results may be produced.
        Furthermore, when the closest object is drawn first, the depth buffer marks all written pixels and any object behind it
        will not be drawn.

        The proper way to fix this would be to draw all opaque objects first, and then draw translucent objects in order.
        However, since we don't really need translucency and only use transparancy (transparency is binary), we can simply not draw
        any pixels that are transparent.

        See for more info: https://www.khronos.org/opengl/wiki/Transparency_Sorting
    */
    if(opacity < 0.1f){
        discard;
    }

    float colorIndex = texColor.g;
    vec4 color = texture(colorMap, colorIndex);
    outColor = vec4(color.rgb, opacity);
}
