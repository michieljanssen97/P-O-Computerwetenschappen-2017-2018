#version 330 core

uniform mat4 modelTransformation;
uniform mat4 viewProjectionTransformation;

layout(location = 0) in vec3 vertexPosModelspace;
layout(location = 1) in vec2 textureCoordinate;
layout(location = 2) in vec3 normalCoordinate;

out vec2 fragTextureCoordinate;

void main()
{
    fragTextureCoordinate = vec2(textureCoordinate.x, 1.0-textureCoordinate.y);
    gl_Position = viewProjectionTransformation * modelTransformation * vec4(vertexPosModelspace, 1.0);
}