#version 330 core

uniform sampler2D inputTexture;
in vec2 fragTextureCoordinate;
out vec3 outColor;

void main() {
    outColor = texture(inputTexture, fragTextureCoordinate).rgb;
}
