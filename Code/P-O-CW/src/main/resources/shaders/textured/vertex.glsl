#version 330 core

uniform mat4 mvp;

in vec3 vertexPosModelspace;
in vec2 textureCoordinate;
in vec3 normalCoordinate;

out vec2 fragTextureCoordinate;

void main()
{
    fragTextureCoordinate = textureCoordinate;
    gl_Position = mvp * vec4(vertexPosModelspace, 1.0);
}