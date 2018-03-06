#version 330 core

uniform sampler2D inputTexture;
uniform vec2 textureScale;

in vec2 fragTextureCoordinate;
out vec4 outColor;

void main() {
    vec2 scaledTexCoord = vec2(mod(fragTextureCoordinate.x/textureScale.x, 1.0), mod(fragTextureCoordinate.y/textureScale.y, 1.0));
    outColor = texture(inputTexture, scaledTexCoord);
}