#version 330 core

uniform sampler2D inputTexture;
in vec2 fragTextureCoordinate;
layout(location = 0) out vec4 outColor;

void main() {
    //red for opacity, green for color index
    vec4 texColor = texture(inputTexture, fragTextureCoordinate);
    outColor = vec4(texColor.a, texColor.r, 0.0, 1.0);
}
