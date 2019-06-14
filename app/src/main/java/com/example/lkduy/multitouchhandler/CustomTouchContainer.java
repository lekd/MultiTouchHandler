package com.example.lkduy.multitouchhandler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import 	android.os.Handler;

public class CustomTouchContainer extends View {
    Context context;
    Paint blackPaint;
    Paint bluePaint;
    Paint greenPaint;
    Paint highlightPaint;
    List<Point> pointersLocs = new ArrayList<Point>();
    int fingerVizRadius = 100;
    private VelocityTracker mVeloTracker = null;
    private TouchEventListener touchNotifier = null;
    public void setTouchListener(TouchEventListener listener){
        touchNotifier = listener;
    }
    public CustomTouchContainer(Context ctx) {
        super(ctx);
        context = ctx;
        Init();
    }
    public CustomTouchContainer(Context ctx,AttributeSet attrs){
        super(ctx,attrs);
        context = ctx;
        Init();
    }
    private void Init(){
        blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blackPaint.setColor(Color.BLACK);
        blackPaint.setStyle(Paint.Style.FILL);
        bluePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bluePaint.setColor(Color.BLUE);
        bluePaint.setStyle(Paint.Style.FILL);
        highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highlightPaint.setColor(Color.CYAN);
        //highlightPaint.setColor(Color.rgb(20,255,125));
        highlightPaint.setStyle(Paint.Style.FILL);
        greenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        greenPaint.setColor(Color.rgb(24,255,1));
        greenPaint.setStyle(Paint.Style.FILL);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(new Rect(0, 0, this.getWidth(), this.getHeight()), bluePaint);
        if (preTouchCount > 0) {
            for (int i = 0; i < pointersLocs.size(); i++) {
                Point pos = pointersLocs.get(i);
                canvas.drawCircle(pos.x, pos.y, fingerVizRadius, highlightPaint);
            }
        }
        super.onDraw(canvas);
    }
    int preTouchCount = 0;
    //Timer checkGhostPointerTimer;
    Handler ghostPointerRemover;
    @Override
    public boolean onTouchEvent( MotionEvent event){
        int action = event.getActionMasked();
        int eventType = -1;
        if(ghostPointerRemover != null)
        {
            ghostPointerRemover.removeCallbacks(removeGhostPointerRunnable);
            ghostPointerRemover = null;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                preTouchCount = event.getPointerCount();
                if(mVeloTracker == null){
                    mVeloTracker = VelocityTracker.obtain();
                }
                else{
                    mVeloTracker.clear();
                }
                mVeloTracker.addMovement(event);
                eventType = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                mVeloTracker.addMovement(event);
                preTouchCount = event.getPointerCount();
                detectGlobalTranslation(event,mVeloTracker);
                eventType = 1;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                if(preTouchCount == 1){
                    preTouchCount = 0;
                }
                else {
                    preTouchCount = event.getPointerCount();
                }
                mVeloTracker.addMovement(event);
                eventType = 2;
                break;
        }
        mVeloTracker.computeCurrentVelocity(1000);
        storeCurrentPointers(event);
        invalidate();
        List<TouchPointer> avaiPointers;
        if(preTouchCount > 0) {
            avaiPointers =getAvailablePointers(event, mVeloTracker, this.getWidth(), this.getHeight());
        }
        else{
            avaiPointers = new ArrayList<>();
        }
        if(touchNotifier != null){
            touchNotifier.TouchEventArose(eventType,avaiPointers);
            if(eventType == 2 && avaiPointers.size()==1){
                //checkGhostPointerTimer = new Timer();
                //checkGhostPointerTimer.schedule(removeGhostPointerTask,100);
                ghostPointerRemover = new Handler();
                ghostPointerRemover.postDelayed(removeGhostPointerRunnable,100);
            }
        }
        return true;
    }
    TimerTask removeGhostPointerTask = new TimerTask() {
        @Override
        public void run() {
            List<TouchPointer> avaiPointers = new ArrayList<>();
            touchNotifier.TouchEventArose(2,avaiPointers);
            preTouchCount = 0;
            pointersLocs = new ArrayList<>();

        }
    };
    Runnable removeGhostPointerRunnable = new Runnable() {
        @Override
        public void run() {
            List<TouchPointer> avaiPointers = new ArrayList<>();
            touchNotifier.TouchEventArose(2,avaiPointers);
            preTouchCount = 0;
            pointersLocs = new ArrayList<>();
            invalidate();
        }
    };
    void storeCurrentPointers(MotionEvent ev){
        pointersLocs.clear();
        for(int p=0; p<ev.getPointerCount(); p++){
            int pointerID = ev.getPointerId(p);
            Point pos = new Point();
            pos.x = (int)ev.getX(p);
            pos.y = (int)ev.getY(p);
            pointersLocs.add(pos);
        }
    }
    boolean detectGlobalTranslation(MotionEvent ev,VelocityTracker veloTracker){
        if(ev.getPointerCount()!=2){
            return false;
        }
        PointF p1Velo = new PointF();
        p1Velo.x = veloTracker.getXVelocity(ev.getPointerId(0));
        p1Velo.y = veloTracker.getYVelocity(ev.getPointerId(0));
        PointF p2Velo = new PointF();
        p2Velo.x = veloTracker.getXVelocity(ev.getPointerId(1));
        p2Velo.y = veloTracker.getYVelocity(ev.getPointerId(1));
        //Log.i("Velo1",String.format("%f - %f",p1Velo.x,p1Velo.y));
        //Log.i("Velo2",String.format("%f - %f",p2Velo.x,p2Velo.y));
        PointF sumVelo = new PointF();
        sumVelo.x = p1Velo.x + p2Velo.x;
        sumVelo.y = p1Velo.y + p2Velo.y;
        //Log.i("SumVelo",String.format("%f - %f",sumVelo.x,sumVelo.y));
        return  true;
    }
    List<TouchPointer> getAvailablePointers(MotionEvent event,VelocityTracker veloTracker, int containerW,int containerH){
        List<TouchPointer> avaiPointers = new ArrayList<>();
        for(int i=0;i<event.getPointerCount();i++){
            TouchPointer p = new TouchPointer();
            p.setPointerID(event.getPointerId(i));
            p.setRelX(event.getX(i)/containerW);
            p.setRelY(event.getY(i)/containerH);
            p.setRelVeloX(veloTracker.getXVelocity(event.getPointerId(i))/containerW);
            p.setRelVeloY(veloTracker.getYVelocity(event.getPointerId(i))/containerH);
            avaiPointers.add(p);
        }
        return avaiPointers;
    }
    public interface TouchEventListener{
        //Type of event: 0 - Down, 1 - Move, 2 - Up
        void TouchEventArose(int eventType, List<TouchPointer> avaiPointers);

    }
}
