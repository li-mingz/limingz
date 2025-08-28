#version 150 core

in vec3 Position;
in vec4 Color;
in vec2 UV;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float Time;

out vec4 vertexColor;
out vec2 texCoord;

void main() {
    // 添加轻微脉动动画
    float pulse = 0.95 + 0.05 * sin(Time * 2.0);
    vec3 pos = Position * vec3(pulse, pulse, 1.0);

    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
    vertexColor = Color;
    texCoord = UV;
}