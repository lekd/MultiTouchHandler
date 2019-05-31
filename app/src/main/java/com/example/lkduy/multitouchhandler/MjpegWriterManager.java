package com.example.lkduy.multitouchhandler;

import java.util.ArrayList;

public class MjpegWriterManager {
    ArrayList<MjpegWriter> writers = new ArrayList<>();
    public MjpegWriterManager()
    {

    }
    public void AddWriter(MjpegWriter writer)
    {
        writer.writeHeader();
        writers.add(writer);
        /*if(writers.size()==0) {
            writers.add(writer);
        }
        else{
            writers.get(0).close();
            writers.set(0,writer);
        }*/
    }
    public void StreamFrameData(byte[] jpegData)
    {
        for(int i=0; i<writers.size();i++)
        {
            if(writers.get(i).IsActive()) {
                writers.get(i).writeJpegData(jpegData);
            }
        }
    }
}
