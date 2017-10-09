#version 330 core

layout(location = 0) in vec3 vertexPos_modelspace;

void main()
{
    mat3 transformMatrix = mat3(vec3(1,0,0), vec3(0,-1,0), vec3(0,0,1));
    vec3 mirroredPos = transformMatrix * vertexPos_modelspace;
    gl_Position = vec4(mirroredPos, 1.0);
}