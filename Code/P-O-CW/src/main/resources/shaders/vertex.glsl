#version 330 core

in vec3 vertexPos_modelspace;
uniform mat4 mvp;

void main()
{
    gl_Position = mvp * vec4(vertexPos_modelspace, 1.0);
}