#version 330 core

uniform mat4 viewProjectionTransformation;
uniform vec3 pointA;
uniform vec3 pointB;
uniform vec3 color;

layout(location = 0) in vec3 vertexPosModelspace;

void main()
{
    if(vertexPosModelspace.z < 0){
        gl_Position = viewProjectionTransformation * vec4(pointB, 1.0);
    }else{
        gl_Position = viewProjectionTransformation * vec4(pointA, 1.0);
    }
}