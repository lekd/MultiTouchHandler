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
    }
    public void StreamFrameData(byte[] jpegData)
    {
        for(int i=0; i<writers.size();i++)
        {
            writers.get(i).writeJpegData(jpegData);
        }
    }
}
