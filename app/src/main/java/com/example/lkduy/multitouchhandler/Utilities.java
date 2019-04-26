package com.example.lkduy.multitouchhandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

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
            if(avaiPointers.size()>0){
                eventJSONObj.put("Pointers",jsonArray);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return eventJSONObj.toString();
    }
}
