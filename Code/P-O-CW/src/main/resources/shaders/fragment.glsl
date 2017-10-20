#version 330 core

out vec3 outColor;
varying vec3 color;

void main() {
    //outColor = vec3(1.0, 0.3, 0.5);
    outColor = color;
}
