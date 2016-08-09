package com.hackathon.westhill.hackathonbrailleinput;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.Map;
import android.os.Vibrator;

import org.w3c.dom.Text;

public class BrailleInputActivity extends AppCompatActivity {

    View circle1, circle2, circle3, circle4, circle5, circle6;
    Button button;
    HashMap<View, Boolean> states;
    Vibrator vibrator;
    public static boolean open = false;
    public static boolean paused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_braille_input);
        open = true;
        circle1 = findViewById(R.id.circle1);
        circle2 = findViewById(R.id.circle2);
        circle3 = findViewById(R.id.circle3);
        circle4 = findViewById(R.id.circle4);
        circle5 = findViewById(R.id.circle5);
        circle6 = findViewById(R.id.circle6);
        button = (Button) findViewById(R.id.submit_pattern_button);
        resetBoard();
        vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        HostSelectionActivity.webSocketConnection.bindInputActivity(this);
    }

    public void resetBoard() {
        states = new HashMap<View, Boolean>();
        states.put(circle1, false);
        states.put(circle2, false);
        states.put(circle3, false);
        states.put(circle4, false);
        states.put(circle5, false);
        states.put(circle6, false);
        View[] circles = {circle1, circle2, circle3, circle4, circle5, circle6};
        for(View circle : circles) {
            circle.setBackgroundResource(R.drawable.circle);
        }
        button.setText(getResources().getText(R.string.submit_braille_pattern));
        button.setEnabled(true);
        paused = false;
    }

    public void invertCircle(View circle) {
        if (states.get(circle)) {
            circle.setBackgroundResource(R.drawable.circle);
            states.put(circle, false);
            vibrator.vibrate(60);
        } else {
            circle.setBackgroundResource(R.drawable.selected_circle);
            states.put(circle, true);
            vibrator.vibrate(130);
        }
    }

    public void tapCircle(View circle) {
        if(!paused)
            invertCircle(circle);
    }

    public void submitBraillePattern(View view) {
        String serialised = "";
        Boolean[] values = {
                states.get(circle1),
                states.get(circle2),
                states.get(circle3),
                states.get(circle4),
                states.get(circle5),
                states.get(circle6)
        };
        for(Boolean circleState : values) {
            serialised += circleState ? "1" : "0";
        }
        String resolved = BrailleMap.resolve(serialised);
        if (resolved != null) {
            HostSelectionActivity.tts.say(resolved, TextToSpeech.QUEUE_ADD);
            HostSelectionActivity.webSocketConnection.send("pattern " + serialised + " " + resolved);
            button.setText(resolved);
            button.setEnabled(false);
            paused = true;
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    resetBoard();
                }
            }, 500);
        } else {
            HostSelectionActivity.tts.say("Unknown input", TextToSpeech.QUEUE_ADD);
        }
    }

    @Override
    public void finish() {
        super.finish();
        open = false;
        HostSelectionActivity.webSocketConnection.close();
    }
}
