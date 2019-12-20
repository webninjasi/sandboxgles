package com.github.webninjasi.sandboxgl;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.util.Pair;
import android.view.MotionEvent;

public class GameSurfaceView extends GLSurfaceView {
    private final GameRenderer renderer;

    private float mouseX, mouseY;
    private boolean mouseDown;

    public GameSurfaceView(Context context){
        super(context);

        // Create an OpenGL ES 3.1 context (int only)
        setEGLContextClientVersion(3);
        super.setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        renderer = new GameRenderer(this);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(renderer);

        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_DOWN:
                mouseDown = true;
                break;
            case MotionEvent.ACTION_UP:
                mouseDown = false;
                break;
        }

        return true;
    }

    public float getMouseX() {
        return mouseX;
    }

    public float getMouseY() {
        return mouseY;
    }

    public boolean isMouseDown() {
        return mouseDown;
    }
}
