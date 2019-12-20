package com.github.webninjasi.sandboxgl;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.opengl.GLSurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {
    private GLSurfaceView gLView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fullscreen();

        gLView = new GameSurfaceView(this);

        setContentView(gLView);
    }

    @Override
    protected void onResume()
    {
        // Don't forget to RESUME the view when the app resumes
        super.onResume();
        gLView.onResume();
    }

    @Override
    protected void onPause()
    {
        // Don't forget to PAUSE the view when the app stops!
        // If you don't do this, the process will still be running,
        // totally screwing the performance and the battery life of the
        // device.
        super.onPause();
        gLView.onPause();
    }

    protected void fullscreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        /*
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
         */
    }
}
