package com.example.irisassistant;

import android.app.Application;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class MyApp extends Application {

    private static OkHttpClient okHttpClient;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize OkHttpClient globally with a logging interceptor
        initializeOkHttpClient();
    }

    private void initializeOkHttpClient() {
        // Create a logging interceptor
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Initialize OkHttpClient with interceptor
        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                // Consider adding a cache, timeouts, and other customizations here
                .build();
    }

    public static OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }
}
