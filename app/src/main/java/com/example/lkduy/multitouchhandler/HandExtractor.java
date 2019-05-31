package com.example.lkduy.multitouchhandler;

import android.os.Environment;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;

public class HandExtractor {
    private Mat unwrappingMat = null;
    Scalar[] skinColorThresholds = new Scalar[2];
    public HandExtractor(){
        unwrappingMat = loadUnwrapMatrix();
        //skinColorThresholds[0] = new Scalar(10,132,77,255);
        skinColorThresholds[0] = new Scalar(5,122,77,255);
        //skinColorThresholds[1] = new Scalar(255,172,127,255);
        skinColorThresholds[1] = new Scalar(255,190,140,255);
    }
    Mat screenArea = null;
    Mat yCrCbScreenMat = null;
    Mat skinMask = null;
    Mat screenBkgRemoved = null;
    public  Mat extractHandOnScreen(Mat src,int outputW,int outputH){
        if(screenArea == null){
            screenArea =new Mat(outputW,outputH,src.type());
        }
        if(unwrappingMat != null){
            screenArea.setTo(new Scalar(0,0,0,255));
            Imgproc.warpPerspective(src,screenArea,unwrappingMat, new Size(outputW,outputH));
            skinMask = detectSkin(screenArea);
            if(screenBkgRemoved == null) {
                screenBkgRemoved = new Mat(screenArea.rows(), screenArea.cols(), src.type());
            }
            //Core.bitwise_not(skinMask,skinMask);
            screenBkgRemoved.setTo(new Scalar(0,0,0,0));
            screenBkgRemoved.setTo(new Scalar(224,172,105,255),skinMask);
            //screenArea.copyTo(screenBkgRemoved,skinMask);
            return  screenBkgRemoved;
        }
        else{
            screenArea = null;
        }
        return  screenArea;
    }
    Mat detectSkin(Mat detectedScreenArea){
        if(yCrCbScreenMat == null) {
            yCrCbScreenMat = new Mat(detectedScreenArea.rows(), detectedScreenArea.cols(), detectedScreenArea.type());
        }
        yCrCbScreenMat.setTo(new Scalar(0,0,0,255));
        Imgproc.cvtColor(detectedScreenArea,yCrCbScreenMat, Imgproc.COLOR_RGB2YCrCb);
        if(skinMask == null){
            skinMask = new Mat(detectedScreenArea.height(), detectedScreenArea.width(), CvType.CV_8UC1);
        }
        skinMask.setTo(new Scalar(0,0,0,255));
        Core.inRange(yCrCbScreenMat, skinColorThresholds[0], skinColorThresholds[1],skinMask);
        Imgproc.dilate(skinMask, skinMask,Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9, 9)));
        Imgproc.erode(skinMask, skinMask,Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9, 9)));
        //Imgproc.GaussianBlur(skinMask,skinMask, new Size(9,9),4);
        return  skinMask;
    }
    Mat loadUnwrapMatrix(){
        File sdCard = Environment.getExternalStorageDirectory();
        File configFolder = new File(sdCard.getAbsolutePath() + "/MirrorTablet");
        File configFile = new File(configFolder, "Calib.config");
        if(!configFile.exists()){
            return  null;
        }
        try {
            BufferedReader fileReader = new BufferedReader(new FileReader(configFile));
            Point[] cornersInFrame = new Point[5];
            Point[] cornersOnScreen = new Point[5];
            String header;
            while((header = fileReader.readLine()) != null){
                if(header.contentEquals("FRAME_POINTS") || header.contentEquals("SCREEN_POINTS")){
                    String line;
                    for(int i=0; i < 5; i++){
                        line = fileReader.readLine();
                        line.trim();
                        String[] coordinatesStrs = line.split(";");
                        if(header.contentEquals("FRAME_POINTS")){
                            cornersInFrame[i] = new Point(Double.parseDouble(coordinatesStrs[0]),Double.parseDouble(coordinatesStrs[1]));
                        }
                        else if (header.contentEquals("SCREEN_POINTS")){
                            cornersOnScreen[i] = new Point(Double.parseDouble(coordinatesStrs[0]),Double.parseDouble(coordinatesStrs[1]));
                        }
                    }
                }
            }
            unwrappingMat = computeScreenUnwrappingMatrix(cornersInFrame,cornersOnScreen);
            String unwrapMatStr = "";
            for(int i=0; i < unwrappingMat.rows(); i++){
                for(int j=0; j < unwrappingMat.cols(); j++) {
                    double[] val = unwrappingMat.get(i,j);
                    unwrapMatStr += String.format("%f;", val[0]);
                }
            }
            Log.i("UnwrapMat",unwrapMatStr);
        }catch (Exception ex){};
        return  unwrappingMat;
    }
    Mat computeScreenUnwrappingMatrix(Point[] cornersInFrame, Point[] cornersOnScreen){
        Mat src_Mat = new Mat(4,1, CvType.CV_32FC2);
        src_Mat.put((int)(cornersInFrame[0].x),(int)(cornersInFrame[0].y),
                cornersInFrame[1].x, cornersInFrame[1].y,
                cornersInFrame[2].x, cornersInFrame[2].y,
                cornersInFrame[3].x, cornersInFrame[3].y,
                cornersInFrame[4].x, cornersInFrame[4].y);

        Mat dst_Mat =new Mat(4, 1, CvType.CV_32FC2);
        dst_Mat.put((int)(cornersOnScreen[0].x),(int)(cornersOnScreen[0].y),
                cornersOnScreen[1].x,cornersOnScreen[1].y,
                cornersOnScreen[2].x,cornersOnScreen[2].y,
                cornersOnScreen[3].x,cornersOnScreen[3].y,
                cornersOnScreen[4].x,cornersOnScreen[4].y);
        return Imgproc.getPerspectiveTransform(src_Mat,dst_Mat);
    }
}
