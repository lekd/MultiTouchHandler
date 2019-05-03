package com.example.lkduy.multitouchhandler;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class MainActivity extends AppCompatActivity implements CustomTouchContainer.TouchEventListener {

    CustomTouchContainer mainTouchContainer;
    private OkHttpClient client = new OkHttpClient();
    private String serverAddress = "192.168.0.102:8080";
    Context mainContext = this;
    CustomWebSocketListener wsListener = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainTouchContainer = (CustomTouchContainer)findViewById(R.id.mainTouchContainer);
        mainTouchContainer.setTouchListener(this);
        ShowServerIPInputDialog();
    }
    @Override
    public  void onStart()
    {
        super.onStart();

    }
    void ShowServerIPInputDialog(){
        final Dialog serverAddressDlg = new Dialog(mainContext);
        serverAddressDlg.setContentView(R.layout.server_address_dlg);
        Button btnSave = serverAddressDlg.findViewById(R.id.serverDlg_btnSave);
        final EditText addressEditText = serverAddressDlg.findViewById(R.id.serverDlg_editAddress);
        btnSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!addressEditText.getText().toString().isEmpty()){
                    serverAddress = addressEditText.getText().toString();
                    startNetworkConnection();
                    serverAddressDlg.dismiss();
                }
            }
        });
        serverAddressDlg.setCancelable(false);
        Window dlgWindow = serverAddressDlg.getWindow();
        dlgWindow.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
        dlgWindow.setGravity(Gravity.CENTER);
        serverAddressDlg.show();

    }
    void startNetworkConnection(){
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
