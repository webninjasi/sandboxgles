#version 310 es

precision highp float;

float[] typeToMass = float[](0.0, 1.0, 2.0);

struct Particle {
    vec2 pos;
    vec2 vel;
    vec2 npos;
    vec2 nvel;
    float type;
    float obstacle;
};

layout (std430, binding = 0) buffer data { Particle particles[]; };
layout (std430, binding = 1) buffer dataMap { int particleMap[]; };

uniform float lightSpeed;
uniform float stepTime;
uniform int particlesCount;
uniform int width;
uniform int height;

layout (local_size_x = 8, local_size_y = 1, local_size_z = 1) in;
void main() {
    int idx = int(gl_GlobalInvocationID.x);
    if (idx > particlesCount)
        return;

    Particle p = particles[idx];

    // No physics applied to obstacles
    if (p.obstacle == 1.0) {
        return;
    }

    /*
    // Rainbow mode
    p.type += 0.1;
    if (p.type > 6.0)
        p.type = 1.0;
    */

    vec2 acceleration = vec2(0.0, 9.808); // gravity constant at Trabzon
    vec2 worldBoundary = vec2(width-1, height-1);

    // Calculate new velocity
    if (length(p.vel) < lightSpeed) {
        p.nvel = p.vel + acceleration * stepTime;
    }
    else if (length(p.nvel) > lightSpeed) {
        p.nvel = normalize(p.nvel) * lightSpeed;
    }

    // Calculate new position
    p.npos = p.pos + p.nvel * stepTime;

    // Apply world constraints
    p.npos.x = clamp(p.npos.x, 0.0, worldBoundary.x);
    p.npos.y = clamp(p.npos.y, 0.0, worldBoundary.y);

    // Get particle id at the new position
    ivec2 iNPos = ivec2(floor(p.npos + vec2(0.5, 0.5)));
    int mNIdx = iNPos.y * width + iNPos.x;
    int idx2 = particleMap[mNIdx] - 1;

    // Next position not marked yet
    if (idx2 != idx) {
        // Particle exists at the position
        if (idx2 != -1) {
            Particle p2 = particles[idx2];

            // Undo the position update
            p.npos = p.pos;

            // Collision with stationary particle
            p.nvel = (p.nvel + p2.vel) / 2.0;
        } else {
            // Calculate old position's map index
            ivec2 iPos = ivec2(floor(p.pos + vec2(0.5, 0.5)));
            int mIdx = iPos.y * width + iPos.x;

            // Try to remove particle id for the previous position
            if (mIdx != mNIdx) {
                atomicCompSwap(particleMap[mIdx], idx + 1, 0);
            }

            // Try to associate particle id with the new position
            atomicCompSwap(particleMap[mNIdx], 0, idx + 1);
        }
    }

    // Set new position/velocity
    p.pos = p.npos;
    p.vel = p.nvel;

    particles[idx] = p;
}
