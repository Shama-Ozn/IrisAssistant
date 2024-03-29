package com.example.irisassistant;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private final int SPLASH_DISPLAY_LENGTH = 5000; // Duration of wait in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        VideoView videoView = findViewById(R.id.splashVideoView);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splashv);
        videoView.setVideoURI(videoUri);

        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(false); // Change this as needed
            videoView.start();
        });

        // This handler ensures that we wait SPLASH_DISPLAY_LENGTH milliseconds after video completion before proceeding
        new Handler().postDelayed(this::startMainActivity, SPLASH_DISPLAY_LENGTH);
    }

    private void startMainActivity() {
        if (!isFinishing()) {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close this activity
        }
    }
}
