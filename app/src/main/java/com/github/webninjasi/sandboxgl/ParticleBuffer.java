package com.github.webninjasi.sandboxgl;

import android.opengl.GLES31;
import android.util.Pair;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class ParticleBuffer {
    private ByteBuffer buffer;
    private ByteBuffer mapBuffer;
    private int bufferSize;
    private int mapBufferSize;
    private int id;
    private int mapid;

    private int capacity;
    private int width;
    private int height;
    private int count;

    public ParticleBuffer(int cap, int maxX, int maxY) {
        capacity = cap;
        width = maxX;
        height = maxY;
        bufferSize = capacity * Particle.ByteSize;
        mapBufferSize = width * height * 4;
        count = 0;

        buffer = ByteBuffer.allocateDirect(bufferSize);
        buffer.order(ByteOrder.nativeOrder());
        buffer.position(0);

        mapBuffer = ByteBuffer.allocateDirect(mapBufferSize);
        mapBuffer.order(ByteOrder.nativeOrder());
        mapBuffer.position(0);

        int[] bb = new int[1];
        GLES31.glGenBuffers(1, bb, 0);
        id = bb[0];

        int[] bb2 = new int[1];
        GLES31.glGenBuffers(1, bb2, 0);
        mapid = bb2[0];

        GLES31.glBindBuffer(GLES31.GL_SHADER_STORAGE_BUFFER, id);
        GLES31.glBufferData(GLES31.GL_SHADER_STORAGE_BUFFER, bufferSize, buffer, GLES31.GL_STATIC_DRAW);
        GLES31.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, 0, id);

        GLES31.glBindBuffer(GLES31.GL_SHADER_STORAGE_BUFFER, mapid);
        GLES31.glBufferData(GLES31.GL_SHADER_STORAGE_BUFFER, mapBufferSize, mapBuffer, GLES31.GL_STATIC_DRAW);
        GLES31.glBindBufferBase(GLES31.GL_SHADER_STORAGE_BUFFER, 1, mapid);

        for (int i=0; i<width; i++) {
            add(new Particle(Pair.create(i, 0), Pair.create(0.0f,0.0f), 0, 1));
            add(new Particle(Pair.create(i, height-1), Pair.create(0.0f,0.0f), 0, 1));
        }

        for (int i=1; i<height-1; i++) {
            add(new Particle(Pair.create(0, i), Pair.create(0.0f,0.0f), 0, 1));
            add(new Particle(Pair.create(width-1, i), Pair.create(0.0f,0.0f), 0, 1));
        }
    }

    public void add(Particle p) {
        if (count >= capacity)
            return;

        // Check boundaries
        Pair<Integer, Integer> pos = p.getPos();
        if (pos.first >= width || pos.second >= height || pos.first < 0 || pos.second < 0)
            return;
        if (!p.isObstacle() && (pos.first == width-1 || pos.second == height-1 || pos.first == 0 || pos.second == 0))
            return;

        int offset;

        // Calculate map offset
        offset = (pos.second * width + pos.first) * 4;

        // Prevent creating over another particle
        GLES31.glBindBuffer(GLES31.GL_SHADER_STORAGE_BUFFER, mapid);
        ByteBuffer mb = (ByteBuffer) GLES31.glMapBufferRange(GLES31.GL_SHADER_STORAGE_BUFFER, offset, 4, GLES31.GL_MAP_READ_BIT);
        mb.order(ByteOrder.nativeOrder());
        int idx = mb.getInt();
        GLES31.glUnmapBuffer(GLES31.GL_SHADER_STORAGE_BUFFER);

        if (idx > 0) {
            return;
        }

        // Write particle id to map
        mapBuffer.position(offset);
        mapBuffer.putInt(count + 1);

        // Send new data to GPU
        mapBuffer.position(offset);
        GLES31.glBindBuffer(GLES31.GL_SHADER_STORAGE_BUFFER, mapid);
        GLES31.glBufferSubData(GLES31.GL_SHADER_STORAGE_BUFFER, offset, 4, mapBuffer);


        // Find next particle slot
        offset = count * Particle.ByteSize;

        // Write particle data
        buffer.position(offset);
        p.writeToBuffer(buffer);

        // Send new data to GPU
        buffer.position(offset);
        GLES31.glBindBuffer(GLES31.GL_SHADER_STORAGE_BUFFER, id);
        GLES31.glBufferSubData(GLES31.GL_SHADER_STORAGE_BUFFER, offset, Particle.ByteSize, buffer);

        count ++;
    }

    public int getId() {
        return id;
    }

    public int getCount() {
        return count;
    }
}
