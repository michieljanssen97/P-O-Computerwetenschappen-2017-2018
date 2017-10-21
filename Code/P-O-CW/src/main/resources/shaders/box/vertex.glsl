#version 330 core

uniform mat4 mvp;

in vec3 vertexPosModelspace;
in vec3 normalCoordinate;

out vec3 fragNormal;

void main()
{
    fragNormal = (mvp * vec4(vertexPosModelspace, 1.0)).xyz;
    gl_Position = mvp * vec4(vertexPosModelspace, 1.0);
}