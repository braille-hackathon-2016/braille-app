package com.hackathon.westhill.hackathonbrailleinput;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by amish on 04/08/2016.
 */
public class TextToSpeechManager {
    TextToSpeech tts;
    Context context;

    public TextToSpeechManager(Context context) {
        this.context = context;
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.UK);
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void beforeLollipop(String speech, int mode) {
        tts.speak(speech, mode, null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void afterLollipop(String speech, int mode) {
        tts.speak(speech, mode, null, null);
    }

    public void say(String text, int mode) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            afterLollipop(text, mode);
        } else {
            beforeLollipop(text, mode);
        }
    }
}
