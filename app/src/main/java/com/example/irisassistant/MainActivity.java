package com.example.irisassistant;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.speech.tts.UtteranceProgressListener;
import java.util.HashMap;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final int SPEECH_REQUEST_CODE = 0;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private TextToSpeech textToSpeech;
    private TextView tvWeatherInfo;
    private WeatherService weatherService; // Declare WeatherService

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnSpeak = findViewById(R.id.btnSpeak);
        tvWeatherInfo = findViewById(R.id.tvWeatherInfo);

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "This Language is not supported");
                } else {
                    // Greet the user and automatically start voice recognition
                    HashMap<String, String> params = new HashMap<>();
                    params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");

                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String utteranceId) {
                            // Do nothing
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            Log.d("TTS", "Speech done for ID: " + utteranceId);
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                Log.d("TTS", "Starting voice recognition...");
                                runOnUiThread(() -> startVoiceRecognitionActivity());
                            }, 500); // Adjust delay as needed
                        }



                        @Override
                        public void onError(String utteranceId) {
                            // Handle errors
                        }
                    });

                    speakOut("Here's Iris, your daily assistant. How can I help you today?");
                }
            } else {
                Log.e("TTS", "Initialization Failed!");
            }
        });

        btnSpeak.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
            } else {
                startVoiceRecognitionActivity();
            }
        });

        weatherService = new WeatherService(); // Initialize WeatherService
    }


    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Please say something");

        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Your device does not support speech recognition", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            // Check if the recognized text contains the specific command
            if (spokenText.toLowerCase().contains("iris") && spokenText.toLowerCase().contains("weather")) {
                // Assume the command is about asking for weather, fetch the weather data
                fetchWeatherData("YourLocationHere"); // Replace "YourLocationHere" with actual location data
            } else {
                // Handle other commands or show a message
                tvWeatherInfo.setText("Sorry, I didn't understand that.");
                speakOut("Sorry, I didn't understand that.");
            }
        }
    }


    private void fetchWeatherData(String location) {
        weatherService.fetchWeatherData(location, new WeatherService.WeatherServiceCallback() {
            @Override
            public void onWeatherInfoReceived(String weatherInfo) {
                runOnUiThread(() -> {
                    tvWeatherInfo.setText(weatherInfo);
                    speakOut(weatherInfo);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> tvWeatherInfo.setText(message));
            }
        });
    }

    private void speakOut(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecognitionActivity();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
