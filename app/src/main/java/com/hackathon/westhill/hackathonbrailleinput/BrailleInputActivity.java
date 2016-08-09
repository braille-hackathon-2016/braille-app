package com.hackathon.westhill.hackathonbrailleinput;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import android.os.Vibrator;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.w3c.dom.Text;

public class BrailleInputActivity extends AppCompatActivity {

    View circle1, circle2, circle3, circle4, circle5, circle6;
    public static Button button;
    HashMap<View, Boolean> states;
    public static Vibrator vibrator;
    public static TextView sentence;
    public static boolean open = false;
    public static boolean paused = false;
    public static int progress = 0;
    private static String[] alphabet = {
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

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
        progress = 0;
        sentence = (TextView) findViewById(R.id.sentence_to_type);
        button = (Button) findViewById(R.id.submit_pattern_button);
        resetBoard();
        vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        HostSelectionActivity.webSocketConnection.bindInputActivity(this);
        getSupportActionBar().setTitle("Player " + (HostSelectionActivity.webSocketConnection.playerNumber+1));
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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
        for (View circle : circles) {
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

    public static void waitingForNext(int winner) {
        button.setEnabled(false);
        vibrator.vibrate(500);
        if (winner == HostSelectionActivity.webSocketConnection.playerNumber) {
            button.setText("you won");
            HostSelectionActivity.tts.say("you won that round", TextToSpeech.QUEUE_FLUSH);
        } else {
            button.setText("you lost");
            HostSelectionActivity.tts.say("you lost that round", TextToSpeech.QUEUE_FLUSH);
        }
        paused = true;
    }

    public static boolean stepString() {
        String sent = sentence.getText().toString();
        if (progress+1 >= sent.length()) {
            return true;
        }
        progress++;
        while (!Arrays.asList(alphabet).contains(String.valueOf(sent.charAt(progress)).toLowerCase())) {
            progress++;
        }
        setSentence(sent, false);
        return false;
    }

    public void tapCircle(View circle) {
        if (!paused)
            invertCircle(circle);
    }

    public static void setSentence(String s, boolean fromStart) {
        if (fromStart) {
            paused = false;
            button.setText("submit");
            button.setEnabled(true);
            progress = 0;
        }
        String joined = "";
        if (progress > 0)
            joined += s.substring(0, progress);
        joined += "<font color=\"#EE0000\">"+s.charAt(progress)+"</font>";
        joined += s.substring(progress+1);
        sentence.setText(Html.fromHtml(joined));
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
        for (Boolean circleState : values) {
            serialised += circleState ? "1" : "0";
        }
        String resolved = BrailleMap.resolve(serialised);
        if (resolved != null && !resolved.toLowerCase().equals(String.valueOf(sentence.getText().toString().toLowerCase().charAt(progress)))) {
            HostSelectionActivity.tts.say("Incorrect " + resolved, TextToSpeech.QUEUE_ADD);
        } else if (resolved != null) {
            HostSelectionActivity.tts.say(resolved, TextToSpeech.QUEUE_ADD);
            HostSelectionActivity.webSocketConnection.send("progress " + progress);
            button.setText(resolved);
            button.setEnabled(false);
            paused = true;
            if (stepString()) { }
            resetBoard();
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

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "BrailleInput Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.hackathon.westhill.hackathonbrailleinput/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "BrailleInput Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.hackathon.westhill.hackathonbrailleinput/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
