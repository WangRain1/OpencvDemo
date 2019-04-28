/*
 * Copyright (c) 2019. Parrot Faurecia Automotive S.A.S. All rights reserved.
 */

package com.example.ts.opencvdemo;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "CameraActivity";

    CascadeClassifier cascadeClassifier;
    JavaCameraView mCameraView;

    Mat grayscaleImage;
    private int absoluteFaceSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.camera);
        mCameraView = findViewById(R.id.camera);
        mCameraView.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener() {
            @Override
            public void onCameraViewStarted(int width, int height) {
                grayscaleImage = new Mat(height, width, CvType.CV_8UC4);
                absoluteFaceSize = (int) (height * 0.2);
            }

            @Override
            public void onCameraViewStopped() {

            }

            @Override
            public Mat onCameraFrame(Mat aInputFrame) {
                Imgproc.cvtColor(aInputFrame, grayscaleImage, Imgproc.COLOR_RGBA2RGB);
                MatOfRect faces = new MatOfRect();
                if (cascadeClassifier != null) {
                    cascadeClassifier.detectMultiScale(grayscaleImage, faces, 1.1, 2, 2,
                            new Size(absoluteFaceSize, absoluteFaceSize), new Size());
                }
                Rect[] facesArray = faces.toArray();
                for (int i = 0; i <facesArray.length; i++)
                    Imgproc.rectangle(aInputFrame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);
                return aInputFrame;
            }
        });

        initClassifier();
    }


    private void initClassifier() {
        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
        File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(mCascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCameraView.enableView();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mCameraView){
            mCameraView.disableView();
        }
    }
}
