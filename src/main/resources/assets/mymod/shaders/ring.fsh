#version 150 core

in vec4 vertexColor;
in vec2 texCoord;
out vec4 FragColor;

uniform vec4 Color;
uniform float Time;

void main() {
    // 计算与圆心的距离
    float dist = length(texCoord - vec2(0.5));

    // 环状渐变 (0.4-0.5为环区域)
    float ring = smoothstep(0.4, 0.41, dist) *
                 smoothstep(0.5, 0.49, dist);

    // 边缘发光效果
    float glow = pow(ring, 4.0) * 0.5;

    // 动态流光
    float flow = sin(texCoord.x * 10.0 + Time * 3.0) * 0.1;

    // 最终颜色合成
    vec3 finalColor = Color.rgb * (ring + glow) + vec3(0.3, 0.6, 1.0) * flow;
    FragColor = vec4(finalColor, ring * Color.a);
}