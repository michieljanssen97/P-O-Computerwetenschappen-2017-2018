#version 330 core

flat in vec3 fragmentColor;
out vec4 outColor;

void main() {
    outColor = vec4(fragmentColor, 1.0);
}
