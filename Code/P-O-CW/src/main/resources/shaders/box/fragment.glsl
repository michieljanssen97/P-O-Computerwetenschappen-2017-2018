#version 330 core

in vec3 fragNormal;
out vec3 outColor;

void main() {
    outColor = fragNormal;
}
