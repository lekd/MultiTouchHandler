package com.example.lkduy.multitouchhandler;

import android.util.Log;

import java.io.DataOutputStream;

public class MjpegWriter {
    String boundary = "--boundary";
    DataOutputStream stream;
    public MjpegWriter(DataOutputStream stream)
    {
        this.stream = stream;
    }
    public  void writeHeader()
    {
        if(stream == null)
        {
            Log.i("MjpegWriter","Output stream not available");
            return;
        }
        try {
            stream.writeBytes("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: multipart/x-mixed-replace; boundary=" +
                    this.boundary +
                    "\r\n");
            stream.flush();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    public void writeJpegData(byte[] jpegData)
    {
        try {

            stream.writeBytes("\r\n" + this.boundary + "\r\nContent-type: image/jpeg\r\n" +
                    "Content-Length: " + jpegData.length + "\r\n\r\n");
            stream.write(jpegData,0,jpegData.length);
            stream.writeBytes("\r\n");
            stream.flush();
            Log.i("WritingMJPEGResult","Succeeded");
        }
        catch (Exception ex)
        {
            Log.i("WritingMJPEGResult",ex.getMessage());
            //ex.printStackTrace();
        }
    }
    public void close()
    {
        try {
            stream.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
