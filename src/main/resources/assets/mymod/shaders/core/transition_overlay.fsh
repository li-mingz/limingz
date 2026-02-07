#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float Radius;
uniform float Softness;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec4 normal;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }

    float dist = vertexDistance;

    // Discard pixels outside the radius (showing the original world)
    if (dist > Radius) {
        discard;
    }

    // Apply softness at the edge
    float edge = smoothstep(Radius, Radius - Softness, dist);
    color.a *= edge;

    fragColor = color;
}
