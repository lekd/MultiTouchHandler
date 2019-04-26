package com.example.lkduy.multitouchhandler;

import android.util.Log;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class CustomWebSocketListener extends WebSocketListener{
    private String TAG = "CustomWebSocketClient";
    private WebSocket mWebSocket = null;
    @Override
    public void onOpen(WebSocket webSocket, Response response){
        Log.i(TAG,"Connection opened");
        mWebSocket = webSocket;
    }
    public void onMessage(WebSocket webSocket,String text){

    }
    public void onClosing(WebSocket webSocket,int code,String reason){
        webSocket.close(1000, null);
        webSocket.cancel();
    }
    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        //TODO: stuff
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        //TODO: stuff
    }
    public void sendMessage(String msg){
        if(mWebSocket != null){
            mWebSocket.send(msg);
        }
    }
}
