package com.hackathon.westhill.hackathonbrailleinput;

import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Created by amish on 04/08/2016.
 */

public class HostWebSocketConnection {

    HostSelectionActivity selectionActivity;
    BrailleInputActivity inputActivity;
    HostWebSocketClient connection;
    Runnable eventOpen, eventError;
    Handler handler, inputHandler;
    public static int playerNumber;

    HostWebSocketConnection(HostSelectionActivity activity, URI uri) {
        this.selectionActivity = activity;
        this.connection = new HostWebSocketClient(uri);
        this.connection.connect();
        this.handler = new Handler(activity.getMainLooper());
    }

    public void send(String data) {
        this.connection.send(data);
    }

    public void bindInputActivity(BrailleInputActivity activity) {
        inputActivity = activity;
        inputHandler = new Handler(activity.getMainLooper());
    }

    public void close() {
        this.connection.close();
    }

    private class HostWebSocketClient extends WebSocketClient {

        public HostWebSocketClient(URI serverURI) {
            super(serverURI);
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            Log.d("braille.websocket", "socket open");
        }

        @Override
        public void onMessage(String message) {
            String[] args = message.split("\\s+");
            final String command = args[0];
            final String[] arguments = Arrays.copyOfRange(args, 1, args.length);
            Log.d("braille-message", message);
            if (command.equals("playerInfo")) {
                playerNumber = Integer.parseInt(arguments[0]);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        selectionActivity.onConnectionOpen();
                    }
                });
            } else if (command.equals("sentence")) {
                final String sent = TextUtils.join(" ", arguments);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        selectionActivity.tts.say(sent, TextToSpeech.QUEUE_ADD);
                    }
                });
                inputHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        inputActivity.setSentence(TextUtils.join(" ", arguments), true);
                    }
                });
            } else if (command.equals("win")) {
                final int winner = Integer.parseInt(arguments[0]);
                inputHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        inputActivity.waitingForNext(winner);
                    }
                });
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Log.d("braille.websocket", "socket closed");
            if(BrailleInputActivity.open) {
                inputActivity.finish();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(selectionActivity, "Lost connection to host", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        @Override
        public void onError(Exception ex) {
            Log.d("braille.websocket", "socket error");
            final Exception exc = ex;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    selectionActivity.onConnectionError(exc);
                }
            });
        }
    }
}
