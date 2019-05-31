package com.example.lkduy.multitouchhandler;

import java.nio.ByteBuffer;

public class TouchPointer {
    public int getPointerID() {
        return pointerID;
    }

    public void setPointerID(int pointerID) {
        this.pointerID = pointerID;
    }

    protected int pointerID;

    public float getRelX() {
        return relX;
    }

    public void setRelX(float relX) {
        this.relX = relX;
    }

    protected float relX;

    public float getRelY() {
        return relY;
    }

    public void setRelY(float relY) {
        this.relY = relY;
    }

    protected float relY;

    public float getRelVeloX() {
        return relVeloX;
    }

    public void setRelVeloX(float relVeloX) {
        this.relVeloX = relVeloX;
    }

    protected float relVeloX;

    public float getRelVeloY() {
        return relVeloY;
    }

    public void setRelVeloY(float relVeloY) {
        this.relVeloY = relVeloY;
    }

    protected float relVeloY;

    public TouchPointer(){
        pointerID = -1;
        relX = relY = 0;
        relVeloX = relVeloY = 0;
    }

    public static int BYTESIZE = 20;
    public byte[] getByteArray(){
        ByteBuffer buffer = ByteBuffer.allocate(BYTESIZE);
        byte[] valBuffer;
        valBuffer = Utilities.Integer2ByteBufferLITTLE_GENDIAN(pointerID);
        buffer.put(Utilities.Integer2ByteBufferLITTLE_GENDIAN(pointerID));
        buffer.put(Utilities.Float2ByteBufferLITTLE_GENDIAN(relX));
        buffer.put(Utilities.Float2ByteBufferLITTLE_GENDIAN(relY));
        buffer.put(Utilities.Float2ByteBufferLITTLE_GENDIAN(relVeloX));
        buffer.put(Utilities.Float2ByteBufferLITTLE_GENDIAN(relVeloY));
        return buffer.array();
    }

}
