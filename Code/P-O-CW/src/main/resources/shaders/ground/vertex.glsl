#version 330 core

uniform mat4 modelTransformation;
uniform mat4 viewProjectionTransformation;

layout(location = 0) in vec3 vertexPosModelspace;
layout(location = 2) in vec3 normalCoordinate;

void main()
{
    //Set the position of the vertex to the input position after transformation to camera view. ('w' = 1.0 to enable translation)
    gl_Position = viewProjectionTransformation * modelTransformation * vec4(vertexPosModelspace, 1.0);
}