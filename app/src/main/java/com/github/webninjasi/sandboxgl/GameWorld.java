package com.github.webninjasi.sandboxgl;

import android.util.Pair;
import android.opengl.GLES31;

public class GameWorld {
    private int width;
    private int height;

    GameRenderer renderer;

    private int mProgram;
    private int computeProgram;

    private int maxParticleCount;
    private double lightSpeed;
    private double accStep;

    private ParticleBuffer pb;
    private int vertexBufferId;

    private int particleScale;

    public GameWorld(int _width, int _height, int maxSpeed, GameRenderer _renderer, int scale) {
        width = _width;
        height = _height;
        particleScale = scale;
        maxParticleCount = width * height;
        lightSpeed = maxSpeed;
        renderer = _renderer;
        accStep = 0.0;
    }

    public void initialize() {
        pb = new ParticleBuffer(maxParticleCount, width, height);

        int[] buffers = new int[1];
        GLES31.glGenBuffers(1, buffers, 0);
        vertexBufferId = buffers[0];

        GLES31.glBindVertexArray(vertexBufferId);
        GLES31.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, 0, pb.getId());
        GLES31.glBindBuffer(GLES31.GL_ARRAY_BUFFER, pb.getId());
        GLES31.glVertexAttribPointer(0, 2, GLES31.GL_FLOAT, false, Particle.ByteSize, 0);
        GLES31.glVertexAttribPointer(1, 1, GLES31.GL_FLOAT, false, Particle.ByteSize, 4 * 2 * 4);
        GLES31.glBindVertexArray(0);

        GLES31.glEnableVertexAttribArray(0);
        GLES31.glEnableVertexAttribArray(1);

        String vertexShaderCode = Utils.readInputStream(renderer.getResources().openRawResource(R.raw.vs));
        String fragmentShaderCode = Utils.readInputStream(renderer.getResources().openRawResource(R.raw.fs));
        String computeShaderCode = Utils.readInputStream(renderer.getResources().openRawResource(R.raw.cs));

        // Compile shaders
        int vertexShader = GameRenderer.loadShader(GLES31.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = GameRenderer.loadShader(GLES31.GL_FRAGMENT_SHADER, fragmentShaderCode);
        int computeShader = GameRenderer.loadShader(GLES31.GL_COMPUTE_SHADER, computeShaderCode);

        // Create draw program
        mProgram = GLES31.glCreateProgram();
        GLES31.glAttachShader(mProgram, vertexShader);
        GLES31.glAttachShader(mProgram, fragmentShader);
        GLES31.glLinkProgram(mProgram);

        // Create physics programs
        computeProgram = GLES31.glCreateProgram();
        GLES31.glAttachShader(computeProgram, computeShader);
        GLES31.glLinkProgram(computeProgram);
    }

    public void onUpdate(float[] uScreen, double dt) {
        // Step physics
        stepPhysics(dt);

        // Redraw background color
        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT);

        // Draw Particles
        draw(uScreen);
    }

    public void stepPhysics(double stepTime) {
        GLES31.glUseProgram(computeProgram);

        // Calculate new positions
        double step = 1.0 / lightSpeed;
        int particleCount = pb.getCount();
        //int workgroups = maxParticleCount / 8 + 1;
        int workgroups = particleCount / 8 + 1;

        accStep += stepTime;

        if (accStep >= step) {
            int lightSpeedId = GLES31.glGetUniformLocation(computeProgram, "lightSpeed");
            int stepTimeId = GLES31.glGetUniformLocation(computeProgram, "stepTime");
            int particlesCountId = GLES31.glGetUniformLocation(computeProgram, "particlesCount");
            int widthId = GLES31.glGetUniformLocation(computeProgram, "width");
            int heightId = GLES31.glGetUniformLocation(computeProgram, "height");

            GLES31.glUniform1f(lightSpeedId, (float) lightSpeed);
            GLES31.glUniform1i(particlesCountId, particleCount);
            GLES31.glUniform1i(widthId, width);
            GLES31.glUniform1i(heightId, height);

            for (; accStep >= step; accStep -= step) {
                GLES31.glUniform1f(stepTimeId, (float) step);

                GLES31.glDispatchCompute(workgroups, 1, 1);
                GLES31.glMemoryBarrier(GLES31.GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT | GLES31.GL_SHADER_STORAGE_BARRIER_BIT);
            }

        }
    }

    public void draw(float[] mvpMatrix) {
        GLES31.glUseProgram(mProgram);

        int vPMatrixHandle = GLES31.glGetUniformLocation(mProgram, "uMVPMatrix");
        int scaleHandle = GLES31.glGetUniformLocation(mProgram, "scale");
        GLES31.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0);
        GLES31.glUniform1f(scaleHandle, (float) particleScale);

        GLES31.glBindVertexArray(vertexBufferId);
        GLES31.glDrawArrays(GLES31.GL_POINTS, 0, pb.getCount());

        GLES31.glBindVertexArray(0);
    }

    public void createParticle(Pair<Integer, Integer> p, int type) {
        pb.add(new Particle(Pair.create(p.first, p.second), Pair.create(0.0f,0.0f), type, 0));
    }
}
