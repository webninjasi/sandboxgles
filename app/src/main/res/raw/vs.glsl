#version 310 es

uniform mat4 uMVPMatrix;
uniform float scale;

vec4[] typeToColor = vec4[](
    vec4(1.0, 0.0, 0.0, 1.0),
    vec4(0.0, 1.0, 0.0, 1.0),
    vec4(0.0, 0.0, 1.0, 1.0),
    vec4(0.0, 1.0, 1.0, 1.0),
    vec4(1.0, 0.0, 1.0, 1.0),
    vec4(1.0, 1.0, 0.0, 1.0),
    vec4(1.0, 1.0, 1.0, 1.0)
);

layout(location = 0) in vec2 pos;
layout(location = 1) in float typef;

out vec4 vColor;

void main() {
    int type = int(typef);

    if (type >= 0 && type < typeToColor.length()) {
        vColor = typeToColor[type];
    } else {
        vColor = typeToColor[0];
    }

    ivec2 iPos = ivec2(pos);
    gl_PointSize = scale;
	gl_Position = uMVPMatrix * vec4(pos * scale, 0.0, 1.0);
}
