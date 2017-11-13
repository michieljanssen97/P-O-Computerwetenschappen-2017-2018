#version 330 core

flat in vec3 fragmentColor;
out vec3 outColor;

void main() {
    outColor = fragmentColor;
}
