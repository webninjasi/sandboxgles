package com.github.webninjasi.sandboxgl;

import android.util.Pair;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

public class Particle {
    private Pair<Integer, Integer> pos;
    private Pair<Float, Float> vel;
    private int type;
    private int obstacle;

    public static int Size = 2 + 2 + 2 + 2 + 1 + 1;
    public static int ByteSize = Size*4;

    public Particle(Pair<Integer, Integer> pos, Pair<Float, Float> vel, int type, int obstacle) {
        this.pos = pos;
        this.vel = vel;
        this.type = type;
        this.obstacle = obstacle;
    }

    /*
    public static Particle readFromBuffer(FloatBuffer buffer) {
        float f[] = new float[Size];
        buffer.get(f, 0, Size);

        Pair<Integer, Integer> pos = Pair.create((int) f[0], (int) f[1]);
        Pair<Float, Float> vel = Pair.create(f[2], f[3]);

        return new Particle(pos, vel, (int) f[8], (int) f[9]);
    }
    */

    public void writeToBuffer(ByteBuffer buffer) {
        // pos
        buffer.putFloat(pos.first);
        buffer.putFloat(pos.second);

        // vel
        buffer.putFloat(vel.first);
        buffer.putFloat(vel.second);

        // npos
        buffer.putFloat(0);
        buffer.putFloat(0);

        // nvel
        buffer.putFloat(0);
        buffer.putFloat(0);

        // type
        buffer.putFloat(type);

        // obstacle
        buffer.putFloat(obstacle);
    }

    public Pair<Integer, Integer> getPos() {
        return pos;
    }

    public void setPos(Pair<Integer, Integer> pos) {
        this.pos = pos;
    }

    public Pair<Float, Float> getVel() {
        return vel;
    }

    public void setVel(Pair<Float, Float> vel) {
        this.vel = vel;
    }

    public int getType() {
        return type;
    }

    public boolean isObstacle() {
        return obstacle == 1;
    }
}
