package com.github.webninjasi.sandboxgl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.res.Resources;
import android.opengl.GLES31;
import android.opengl.GLSurfaceView;
import android.util.Pair;

public class GameRenderer implements GLSurfaceView.Renderer {
    private GameWorld gWorld;
    private GameSurfaceView surfaceView;

    private long lastRenderTime;
    private int particleScale;
    private int lastType;

    public GameRenderer(GameSurfaceView sv){
        super();

        particleScale = 5;
        gWorld = new GameWorld(1000/particleScale, 1000/particleScale, 100, this, particleScale);
        surfaceView = sv;
        lastRenderTime = 0;

        lastType = 1;
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES31.glClearColor(0f, 0f, 0f, 1.0f);

        gWorld.initialize();
    }

    public void onDrawFrame(GL10 unused) {
        // TODO fps optimizations

        long currentTime = System.nanoTime();
        double deltaTime = (currentTime - lastRenderTime) / 1000 / 1000;
        deltaTime = deltaTime / 1000.0;
        if (lastRenderTime == 0) {
            lastRenderTime = currentTime;
            return;
        }

        float width = surfaceView.getWidth();
        float height = surfaceView.getHeight();
        float[] uScreen =
        {
            2f/width,   0f,         0f, 0f,
            0f,        -2f/height,  0f, 0f,
            0f,         0f,         0f, 0f,
            -1f,        1f,         0f, 1f
        };

        // Create new particle(s)
        if (surfaceView.isMouseDown()) {
            int x = (int) surfaceView.getMouseX();
            int y = (int) surfaceView.getMouseY();

            gWorld.createParticle(Pair.create(x / particleScale, y / particleScale), lastType);
            lastType++;
            if (lastType > 6) lastType = 1;
        }

        gWorld.onUpdate(uScreen, deltaTime);

        lastRenderTime = currentTime;
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES31.glViewport(0, 0, width, height);
    }

    public Resources getResources() {
        return surfaceView.getResources();
    }

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES31.glCreateShader(type);
        GLES31.glShaderSource(shader, shaderCode);
        GLES31.glCompileShader(shader);
        return shader;
    }
}
