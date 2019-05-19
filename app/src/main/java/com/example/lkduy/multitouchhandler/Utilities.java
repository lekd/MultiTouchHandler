package com.example.lkduy.multitouchhandler;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.List;

import static android.content.Context.WIFI_SERVICE;

public class Utilities {
    public static String getJSONStringOfTouchEvent(int eventType, List<TouchPointer> avaiPointers){
        JSONObject eventJSONObj = new JSONObject();
        try {
            eventJSONObj.put("EventType",eventType);
            eventJSONObj.put("PointerCount",avaiPointers.size());
            JSONArray jsonArray = new JSONArray();
            for(int i=0;i<avaiPointers.size();i++){
                JSONObject pointerJSObj = new JSONObject();
                TouchPointer pointer = avaiPointers.get(i);
                pointerJSObj.put("ID",pointer.getPointerID());
                pointerJSObj.put("RelX", pointer.getRelX());
                pointerJSObj.put("RelY",pointer.getRelY());
                pointerJSObj.put("RelVeloX",pointer.getRelVeloX());
                pointerJSObj.put("RelVeloY",pointer.getRelVeloY());
                jsonArray.put(pointerJSObj);
            }
            //if(avaiPointers.size()>0){
                eventJSONObj.put("AvaiPointers",jsonArray);
            //}
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return eventJSONObj.toString();
    }
    public  static Bitmap getBitmapOfMat(Mat img, boolean preserveTransparency){
        Bitmap bmp = null;
        try {
            if(preserveTransparency) {
                bmp = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
            }
            else {
                bmp = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.RGB_565);
            }
            Utils.matToBitmap(img, bmp);
        } catch (CvException e) {
            Log.d("SAVING IMAGE", e.getMessage());
        }
        return bmp;
    }
    public static  Bitmap downSizeBitmap(Bitmap src,float downscaleFactorW,float downscaleFactorH)
    {
        return Bitmap.createScaledBitmap(src,(int)(src.getWidth()*1.0/downscaleFactorW),(int)(src.getHeight()*1.0/downscaleFactorH),true);
    }
    public  static byte[] getBytesFromBitmap(Bitmap bitmap, boolean preserveTransparency){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if(preserveTransparency){
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
        }else {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        }
        return stream.toByteArray();
    }
    public static String getIPAddress(Context ctx, boolean useIPv4) {
        WifiManager wifiManager = (WifiManager) ctx.getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endianif needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e("WIFIIP", "Unable to get host address.");
            ipAddressString = null;
        }

        return ipAddressString;
    }
}
