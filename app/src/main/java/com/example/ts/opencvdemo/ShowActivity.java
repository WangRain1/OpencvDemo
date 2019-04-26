/*
 * Copyright (c) 2019. Parrot Faurecia Automotive S.A.S. All rights reserved.
 */

package com.example.ts.opencvdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ShowActivity extends AppCompatActivity {
    private static final String TAG = "ShowActivity";
    public static final String TYPE = "COME_TYPE";

    ImageView src, dst;
    int mOption = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show);
        src = findViewById(R.id.src);
        dst = findViewById(R.id.dst);
        switch (getIntent().getExtras().getInt(TYPE)){
            case 0:
                lineFliter(mOption);
                break;
            case 1:
//                blurEdgeCheck();
                cannyEdgeCheck();
                break;
        }
    }

    /**
     * 线性滤波
     */
    private void lineFliter(int option){
        Mat mMat;
        Mat mDst;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.weide);
        mMat = new Mat(bitmap.getHeight(),bitmap.getWidth(),CvType.CV_8UC4);
        mDst = new Mat(bitmap.getHeight(),bitmap.getWidth(),CvType.CV_8UC4);
        switch (option){
            case 0:
                //blur
                Utils.bitmapToMat(bitmap,mMat);
                Imgproc.medianBlur(mMat,mDst,23);
//                Imgproc.blur(mMat,mDst,new Size(8,8));
                Bitmap dstBitmap = Bitmap.createBitmap(mDst.cols(),mDst.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mDst,dstBitmap);
                src.setImageBitmap(bitmap);
                dst.setImageBitmap(dstBitmap);
                break;
            case 1:
                //锐化
                Mat sharpen = new Mat(3,3,CvType.CV_16SC1);
                sharpen.put(0,0,
                        0,-1,0,
                        -1,5-1,
                        0,-1,0);
                Utils.bitmapToMat(bitmap,mMat);
                Imgproc.filter2D(mMat,mDst,mMat.depth(),sharpen);
                Bitmap dstBp = Bitmap.createBitmap(mDst.cols(),mDst.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mDst,dstBp);
                src.setImageBitmap(bitmap);
                dst.setImageBitmap(dstBp);
                break;
            case 2:
                //膨胀
                Utils.bitmapToMat(bitmap,mMat);
                //膨胀/锐化:把亮的像素扩张，黑色消除
//                Mat structure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(6,6));
//                Imgproc.dilate(mMat,mDst,structure);
                //腐蚀：把暗地像素扩张
                Mat structure = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,new Size(6,6));
                Imgproc.erode(mMat,mDst,structure);
                Bitmap dstBp1 = Bitmap.createBitmap(mDst.cols(),mDst.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mDst,dstBp1);
                src.setImageBitmap(bitmap);
                dst.setImageBitmap(dstBp1);
                break;
            case 3:
                //伐值化
                Utils.bitmapToMat(bitmap,mMat);
                Imgproc.cvtColor(mMat,mDst,Imgproc.CV_CHAIN_CODE);
                Imgproc.threshold(mDst,mDst,100,255,Imgproc.THRESH_TOZERO);
                Bitmap dstBp2 = Bitmap.createBitmap(mDst.cols(),mDst.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mDst,dstBp2);
                src.setImageBitmap(bitmap);
                dst.setImageBitmap(dstBp2);
                break;
            case 4:
                //自适应伐值化---> 检测图像边缘。
                Utils.bitmapToMat(bitmap,mMat);
                Imgproc.cvtColor(mMat,mDst,Imgproc.COLOR_BGR2GRAY);
                //流程必须是从： mMat ->  mDst -> mDst,因为要逐步对 mDst 进行修改
                Imgproc.adaptiveThreshold(mDst,mDst,255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                        Imgproc.THRESH_BINARY,3,0);
                Bitmap dstBp3 = Bitmap.createBitmap(mDst.cols(),mDst.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mDst,dstBp3);
                src.setImageBitmap(bitmap);
                dst.setImageBitmap(dstBp3);
                break;
        }
    }

    //高斯差分-边缘检测
    private void blurEdgeCheck() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.gx);
        Mat matSrc = new Mat();
        Mat matGray = new Mat();
        Mat matBlur1 = new Mat();
        Mat matBlur2 = new Mat();
        Utils.bitmapToMat(bitmap,matSrc);
        Imgproc.cvtColor(matSrc,matGray,Imgproc.COLOR_BGR2GRAY);

        Imgproc.GaussianBlur(matGray,matBlur1,new Size(5,5),3);
        Imgproc.GaussianBlur(matGray,matBlur2,new Size(11,11),3);

        Mat resultMat = new Mat();
        Core.absdiff(matBlur1,matBlur2,resultMat);
        //在此Scalar 代表单通道的像素值，multiply方法就是让mat的每个像素点 和 scalar相乘
        Core.multiply(resultMat,new Scalar(100),resultMat);
        Imgproc.threshold(resultMat,resultMat,100,255,Imgproc.THRESH_BINARY_INV);

        Bitmap dstBitmap = Bitmap.createBitmap(resultMat.cols(),resultMat.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(resultMat,dstBitmap);
        src.setImageBitmap(bitmap);
        dst.setImageBitmap(dstBitmap);
    }

    //canny-边缘检测
    private void cannyEdgeCheck() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.weide);
        Mat matSrc = new Mat();
        Mat matGray = new Mat();
        Mat matEdge = new Mat();
        Utils.bitmapToMat(bitmap,matSrc);
        Imgproc.cvtColor(matSrc,matGray,Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(matSrc,matEdge,10,100);
        Bitmap dstBitmap = Bitmap.createBitmap(matEdge.cols(),matEdge.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matEdge,dstBitmap);
        src.setImageBitmap(bitmap);
        dst.setImageBitmap(dstBitmap);
    }
}
