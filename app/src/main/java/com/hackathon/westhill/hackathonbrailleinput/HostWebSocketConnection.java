package com.hackathon.westhill.hackathonbrailleinput;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by amish on 04/08/2016.
 */

public class HostWebSocketConnection {

    HostSelectionActivity selectionActivity;
    BrailleInputActivity inputActivity;
    HostWebSocketClient connection;
    Runnable eventOpen, eventError;
    Handler handler;

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
            handler.post(new Runnable() {
                @Override
                public void run() {
                    selectionActivity.onConnectionOpen();
                }
            });
        }

        @Override
        public void onMessage(String message) {

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
