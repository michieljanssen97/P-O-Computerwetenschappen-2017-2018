#version 330 core

in vec3 vertexPos_modelspace;
in vec2 texture_coordinate;
in vec3 normal_coordinate;
uniform mat4 mvp;

varying vec3 color;

void main()
{
    color = vertexPos_modelspace;
    gl_Position = mvp * vec4(vertexPos_modelspace, 1.0);
}