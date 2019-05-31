package com.example.lkduy.multitouchhandler;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MjpegWriter {
    String boundary = "--boundary";
    static int FPS = 20;
    DataOutputStream sendingStream;
    DataInputStream receivStream;
    double frameInterval;
    boolean _isActive;
    public boolean IsActive(){
        return _isActive;
    }

    public MjpegWriter(DataOutputStream sendStream,DataInputStream recvStream)
    {
        this.sendingStream = sendStream;
        this.receivStream = recvStream;
        frameInterval = 1000.0/FPS;
        _isActive = true;
        //new Thread(clientListener).start();
    }
    Runnable clientListener = new Runnable() {
        @Override
        public void run() {
            while (_isActive){
                try {
                    String line = receivStream.readUTF();
                    if(!line.isEmpty()){
                        Log.i("RECV",line);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    };
    public  void writeHeader()
    {
        if(sendingStream == null)
        {
            Log.i("MjpegWriter","Output stream not available");
            return;
        }
        try {
            sendingStream.writeBytes("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: multipart/x-mixed-replace; boundary=" +
                    this.boundary +
                    "\r\n");
            sendingStream.flush();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    long prevTimeStamp = 0;
    double elapseTime = 0;
    public void writeJpegData(byte[] jpegData)
    {
        long curTimeStamp = System.currentTimeMillis();
        elapseTime += (curTimeStamp - prevTimeStamp);
        prevTimeStamp = curTimeStamp;
        if(prevTimeStamp != 0 && elapseTime<frameInterval){
            return;
        }
        try {

            sendingStream.writeBytes("\r\n" + this.boundary + "\r\nContent-type: image/png\r\n" +
                    "Content-Length: " + jpegData.length + "\r\n\r\n");
            sendingStream.write(jpegData,0,jpegData.length);
            sendingStream.writeBytes("\r\n");
            sendingStream.flush();
            //Log.i("WritingMJPEGResult","Succeeded");
            Log.i("MJPEGWriter","Still sending");
        }
        catch (Exception ex)
        {
            Log.i("MJPEGWriter",ex.getMessage());
            //ex.printStackTrace();
        }
        elapseTime = elapseTime % frameInterval;
    }
    public void close()
    {
        try {
            _isActive = false;
            sendingStream.close();
            receivStream.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
