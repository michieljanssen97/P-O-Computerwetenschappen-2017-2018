#version 330 core

uniform mat4 viewProjectionTransformation;

layout(location = 0) in vec3 vertexPosModelspace;

void main()
{
    gl_Position = viewProjectionTransformation * vec4(vertexPosModelspace, 1.0);
}