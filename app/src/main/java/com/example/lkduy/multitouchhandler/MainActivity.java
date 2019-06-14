package com.example.lkduy.multitouchhandler;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
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
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2,
                                                            CustomTouchContainer.TouchEventListener,
                                                            StreamingServer.StreamingServerEventListener,
                                                            StreamingTask.StreamingEventListener{

    static {
        System.loadLibrary("opencv_java3");
    }
    private final String _TAG = "MainActivity:";
    private  final int STREAMING_PORT = 4040;
    private String serverAddress = "192.168.0.102:8080";
    private OkHttpClient client = new OkHttpClient();
    Context mainContext = this;
    private CameraBridgeViewBase mOpenCvCameraView;
    ImageView imvExtractionPreview;
    Thread streamingServerThread;
    MjpegWriterManager mjpegWriterManager;

    CustomWebSocketListener wsListener = null;
    CustomTouchContainer mainTouchContainer;
    HandExtractor handExtractor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //mOpenCvCameraView = (CameraBridgeViewBase)findViewById(R.id.main_openCVCamView);
        //mOpenCvCameraView.setCvCameraViewListener(this);

        /*TextView tvIP = (TextView)findViewById(R.id.tv_myAddress);
        tvIP.setText(String.format("%s:%d", Utilities.getIPAddress(this,true),STREAMING_PORT));
        imvExtractionPreview = (ImageView)findViewById(R.id.imv_handPreview);*/

        /*StreamingServer server = new StreamingServer(STREAMING_PORT);
        server.setEventListener(this);
        streamingServerThread = new Thread(server);
        streamingServerThread.start();
        mjpegWriterManager = new MjpegWriterManager();*/
        ShowServerIPInputDialog();

        mainTouchContainer = (CustomTouchContainer)findViewById(R.id.mainTouchContainer);
        mainTouchContainer.setTouchListener(this);
        handExtractor = new HandExtractor();
    }
    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            String TAG = new StringBuilder(_TAG).append("onManagerConnected").toString();

            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully");
                    if(mOpenCvCameraView != null)
                    {
                        mOpenCvCameraView.enableView();
                    }
                    break;
                default:
                    super.onManagerConnected(status);
            }
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        String TAG = new StringBuilder(_TAG).append("onResume").toString();
        if (!OpenCVLoader.initDebug()) {
            Log.i(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initiation");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, loaderCallback);
        } else {
            Log.i(TAG, "OpenCV library found inside package. Using it");
            //loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

    }
    @Override
    protected void onPause() {
        String TAG = new StringBuilder(_TAG).append("onPause").toString();
        Log.i(TAG, "Disabling a camera view");

        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }

        super.onPause();
    }
    @Override
    protected void onDestroy() {
        String TAG = new StringBuilder(_TAG).append("onDestroy").toString();
        Log.i(TAG, "Disabling a camera view");

        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
        //streamingServerThread.interrupt();
        if(wsListener != null){
            client.dispatcher().executorService().shutdown();
        }
        super.onDestroy();
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
                    startWebSocketConnection();
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
    void startWebSocketConnection(){
        String serverURL = String.format("ws://%s/main.html",serverAddress);
        Request wsRequest = new Request.Builder().url(serverURL).build();
        wsListener = new CustomWebSocketListener();
        WebSocket ws = client.newWebSocket(wsRequest,wsListener);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }
    boolean readyToSend = true;
    Mat cropped = null;
    Rect roi = null;
    Mat extractedFromFrame;
    float downSizeFactor = 2;
    float downscaleFactor4Streaming = 12;
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat originFrame = inputFrame.rgba();
        Mat rotated = new Mat();
        Core.flip(originFrame.t(),rotated,-1);
        if(roi == null){
            roi = new Rect(0,0,rotated.width(),rotated.height()/2);
        }
        cropped = new Mat(rotated,roi);
        cropped = Utilities.downSizeMat(cropped,downSizeFactor);
        extractedFromFrame = handExtractor.extractHandOnScreen(cropped,(int)(mainTouchContainer.getWidth()/downSizeFactor),(int)(mainTouchContainer.getHeight()/downSizeFactor));
        //originFrame.copyTo(rotated);
        if(extractedFromFrame != null){
            Bitmap frameBmp = Utilities.getBitmapOfMat(extractedFromFrame,true, 100);
            final Bitmap scaledDown = Utilities.downSizeBitmap(frameBmp,downscaleFactor4Streaming,downscaleFactor4Streaming);
            /*runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imvExtractionPreview.setImageBitmap(scaledDown);
                }
            });*/
            if(readyToSend)
            {
                StreamingTask streamingTask = new StreamingTask();
                streamingTask.setEventListener(this);
                streamingTask.execute(mjpegWriterManager, Bitmap.createBitmap(scaledDown));
                readyToSend = false;
            }
        }
        return originFrame;
    }

    @Override
    public void TouchEventArose(int eventType, List<TouchPointer> avaiPointers) {
        //String jsonStr = Utilities.getJSONStringOfTouchEvent(eventType,avaiPointers);
        //Log.i("EventJSON",jsonStr);
        //wsListener.sendMessage(jsonStr);
        byte[] byteData = Utilities.getByteDataOfTouchEvent(eventType,avaiPointers);
        wsListener.sendBytes(byteData);
    }
    @Override
    public void ClientConnectedEvent(Socket clientSock) {
        try
        {
            DataOutputStream sendStream = new DataOutputStream(clientSock.getOutputStream());
            DataInputStream recvStream = new DataInputStream(clientSock.getInputStream());
            MjpegWriter writer = new MjpegWriter(sendStream,recvStream);
            mjpegWriterManager.AddWriter(writer);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    @Override
    public void frameSentEvent(boolean sendingResult) {
        if(sendingResult){
            readyToSend = true;
        }
    }
}
