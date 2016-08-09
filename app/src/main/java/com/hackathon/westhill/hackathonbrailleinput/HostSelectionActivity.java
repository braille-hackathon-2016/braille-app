package com.hackathon.westhill.hackathonbrailleinput;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.net.URI;
import java.net.URISyntaxException;

public class HostSelectionActivity extends AppCompatActivity {

    ProgressBar hostConnectProgressBar;
    EditText hostAddressInput;
    Button hostConnectButton;
    public static HostWebSocketConnection webSocketConnection;
    public static TextToSpeechManager tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_selection);
        hostConnectProgressBar = (ProgressBar) findViewById(R.id.host_connect_progress_bar);
        hostAddressInput = (EditText) findViewById(R.id.host_address_input_field);
        hostConnectButton = (Button) findViewById(R.id.host_connect_button);
        tts = new TextToSpeechManager(getApplicationContext());
        BrailleMap.init();
    }

    public void setStateConnecting() {
        hostAddressInput.setEnabled(false);
        hostConnectButton.setEnabled(false);
        hostConnectProgressBar.setVisibility(View.VISIBLE);
    }

    public void setStateIdle() {
        hostAddressInput.setEnabled(true);
        hostConnectButton.setEnabled(true);
        hostConnectProgressBar.setVisibility(View.GONE);
    }

    public void onConnectionOpen() {
        Toast.makeText(HostSelectionActivity.this, "Connected!", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(HostSelectionActivity.this, BrailleInputActivity.class);
        HostSelectionActivity.this.startActivity(i);
        setStateIdle();
    }

    public void onConnectionError(Exception ex) {
        Toast.makeText(HostSelectionActivity.this, "Couldn't connect: " + ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        setStateIdle();
    }

    public void connectToHost(View button) {
        setStateConnecting();
        String address = hostAddressInput.getText().toString();
        URI uri;
        try {
            uri = new URI("ws://" + address + ":25543");
        } catch (URISyntaxException exception) {
            setStateIdle();
            Toast.makeText(HostSelectionActivity.this, "Please enter a valid address", Toast.LENGTH_SHORT).show();
            return;
        }

        this.webSocketConnection = new HostWebSocketConnection(HostSelectionActivity.this, uri);
    }
}
