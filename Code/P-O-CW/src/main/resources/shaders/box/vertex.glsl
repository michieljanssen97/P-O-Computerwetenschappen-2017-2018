#version 330 core

uniform mat4 modelTransformation;
uniform mat4 viewProjectionTransformation;
uniform vec3 color;

layout(location = 0) in vec3 vertexPosModelspace;
layout(location = 2) in vec3 normalCoordinate;

flat out vec3 fragmentColor;

void main()
{
    //Convert the vertex normal from object-space to world space.
    //The 'w' component of the normal is set to 0 to disable translation as the normal is a vector and not a position.
    vec3 fragWorldNormal = (modelTransformation * vec4(normalCoordinate, 0.0)).xyz;

    //Could be faster by replacing this with a transformation of the normal to a 'color-vectorspace' where the axis coordinates
    //are the RGB values of the colors that is mapped to the direction the normal is facing. This will do for now though.
    if(length(fragWorldNormal - vec3(1, 0, 0)) < 1E-5) { // +X
        fragmentColor = 0.85f * color;
    } else if(length(fragWorldNormal - vec3(0, 1, 0)) < 1E-5){ // +Y
        fragmentColor = color;
    } else if(length(fragWorldNormal - vec3(0, 0, 1)) < 1E-5){ // +Z
        fragmentColor = 0.70f * color;
    } else if(length(fragWorldNormal - vec3(-1, 0, 0)) < 1E-5){ // -X
        fragmentColor = 0.30f * color;
    } else if(length(fragWorldNormal - vec3(0, -1, 0)) < 1E-5){ // -Y
        fragmentColor = 0.15f * color;
    } else if(length(fragWorldNormal - vec3(0, 0, -1)) < 1E-5){ // -Z
        fragmentColor = 0.45f * color;
    }

    //Set the position of the vertex to the input position after transformation to camera view. ('w' = 1.0 to enable translation)
    gl_Position = viewProjectionTransformation * modelTransformation * vec4(vertexPosModelspace, 1.0);
}