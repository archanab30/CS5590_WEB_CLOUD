package com.example.tirup.assignment3;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ibm.watson.developer_cloud.android.library.camera.CameraHelper;
import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyImagesOptions;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ImageClassification;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassification;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.VisualClassifier;

import java.io.File;

/**
 * Created by gangi on 7/13/2018.
 */

public class MlActivity extends AppCompatActivity{

    // Used for Camera
    CameraHelper cameraHelper;

    // To expose features of Image using Watson Vision API we will use VisualRecognition service
    VisualRecognition visualRecognition;

    TextView detectedObjects;
    TextView visualDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ml);

        detectedObjects = findViewById(R.id.visualoutput);
        visualDetails = findViewById(R.id.visualrecognitiontext);

        // Initializing VisualRecognition
        visualRecognition = new VisualRecognition(VisualRecognition.VERSION_DATE_2016_05_20, getString(R.string.watson_api_key));
        // Initializing CameraHelper
        cameraHelper = new CameraHelper(this);
    }

    // On Click of Take Picture
    public void takePicture(View view) {
        cameraHelper.dispatchTakePictureIntent();
    }

    // To gain access to picture taken, we will be overwriting 'onActivityResult' method
    //  and looking for Results whose request code is Request_Image_Capture.
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CameraHelper.REQUEST_IMAGE_CAPTURE) {
            // Extracting Picture in form of BitMap Object
            final Bitmap bitmap = cameraHelper.getBitmap(resultCode);
            final File file = cameraHelper.getFile(resultCode);

            // Setting the Preview
            ImageView imageView = findViewById(R.id.imagepreview);
            imageView.setImageBitmap(bitmap);
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    VisualClassification response =
                            visualRecognition.classify(
                                    new ClassifyImagesOptions.Builder()
                                            .images(file)
                                            .build()
                            ).execute();

                    ImageClassification classification =
                            response.getImages().get(0);

                    VisualClassifier classifier =
                            classification.getClassifiers().get(0);

                    final StringBuffer output = new StringBuffer();
                    for(VisualClassifier.VisualClass object: classifier.getClasses()) {
                        // Iterating VisualClassifier Classes and getting the output
                        // if score is > 70%
                        if(object != null && object.getScore() != null &&
                                object.getScore() > 0.7f)
                            output.append("<")
                                    .append(object.getName())
                                    .append("> ");
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Visual Recognition Output
                            if(output != null && output.toString() != null
                                    && output.toString().length() > 0) {
                                detectedObjects.setText(output);
                                visualDetails.setText("Visual Recognition Details : ");
                            }else{
                                detectedObjects.setText("No Visual Recognition Details found :( ");
                            }
                        }
                    });
                }
            });
        }
    }
}