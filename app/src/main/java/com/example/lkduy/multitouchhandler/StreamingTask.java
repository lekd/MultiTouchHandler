package com.example.lkduy.multitouchhandler;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Debug;
import android.util.Log;

public class StreamingTask extends AsyncTask<Object,Object,Boolean> {

    public void setEventListener(StreamingEventListener eventListener) {
        this.eventListener = eventListener;
    }

    StreamingEventListener eventListener;
    @Override
    protected Boolean doInBackground(Object... objects) {
        MjpegWriterManager writerManager = (MjpegWriterManager)objects[0];
        Bitmap currentFrame = (Bitmap)objects[1];
        try
        {
            byte[] bmpData = Utilities.getBytesFromBitmap(currentFrame,true,10);
            writerManager.StreamFrameData(bmpData);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    protected void onPostExecute(Boolean result) {
        if(eventListener != null)
        {
            eventListener.frameSentEvent(result);
        }
    }

    public interface  StreamingEventListener
    {
        void frameSentEvent(boolean sendingResult);
    }
}
