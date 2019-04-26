package com.example.lkduy.multitouchhandler;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class MainActivity extends AppCompatActivity implements CustomTouchContainer.TouchEventListener {

    CustomTouchContainer mainTouchContainer;
    private OkHttpClient client = new OkHttpClient();
    private String serverAddress = "192.168.0.100:8080";
    CustomWebSocketListener wsListener = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainTouchContainer = (CustomTouchContainer)findViewById(R.id.mainTouchContainer);
        mainTouchContainer.setTouchListener(this);
        start();
    }
    void start(){
        String serverURL = String.format("ws://%s/main.html",serverAddress);
        Request wsRequest = new Request.Builder().url(serverURL).build();
        wsListener = new CustomWebSocketListener();
        WebSocket ws = client.newWebSocket(wsRequest,wsListener);
    }
    @Override
    protected void onDestroy(){
        if(wsListener != null){
            client.dispatcher().executorService().shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void TouchEventArose(int eventType, List<TouchPointer> avaiPointers) {
        String jsonStr = Utilities.getJSONStringOfTouchEvent(eventType,avaiPointers);
        Log.i("EventJSON",jsonStr);
        wsListener.sendMessage(jsonStr);
    }
}
