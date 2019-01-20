package com.example.facedetectionapp.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Canvas;

import com.example.facedetectionapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import org.bouncycastle.util.Arrays;

import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int RC_FACE_DETECTION = 9001;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Button detect = (Button) findViewById(R.id.detect_face_button);

        detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });
    }
    public void openCamera(){
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }else {
            Log.i(TAG, String.valueOf((rc == PackageManager.PERMISSION_GRANTED)));
            Toast.makeText(this,"Please give camera permission", Toast.LENGTH_LONG).show();
        }

//        Toast.makeText(this,"Starting activity",Toast.LENGTH_LONG).show();
//        Intent intent = new Intent(this, FaceDetectionActivity.class);
//        //intent.putExtra(FaceDetectionActivity.AutoFocus, true);
//        //intent.putExtra(BarcodeCaptureActivity.UseFlash, false);
//        startActivityForResult(intent, RC_FACE_DETECTION);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            Log.i("BitmapSize", String.valueOf(imageBitmap.getHeight())
                    +" "+String.valueOf(imageBitmap.getWidth()));
            //mImageView.setImageBitmap(imageBitmap);
            Log.i(TAG, ">>>>>>>>>>>>>> Got Bitmap");

            ImageView output_image = findViewById(R.id.output_image);
            output_image.setImageBitmap(imageBitmap);

            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
            FirebaseVisionFaceDetectorOptions options =
                    new FirebaseVisionFaceDetectorOptions.Builder()
                            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                            .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                            .setLandmarkMode(
                                    FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                            .setClassificationMode(
                                    FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                            .setMinFaceSize(0.15f)
                            .enableTracking()
                            .build();

            Canvas canvas = new Canvas();
            canvas.setBitmap(imageBitmap);
            FrameLayout main_layout = findViewById(R.id.main_activity_frame);

            FirebaseVisionFaceDetector detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
            Task<List<FirebaseVisionFace>> result =
                    detector.detectInImage(image)
                            .addOnSuccessListener(
                                    new OnSuccessListener<List<FirebaseVisionFace>>() {
                                        @Override
                                        public void onSuccess(List<FirebaseVisionFace> faces) {
                                            // Task completed successfully
                                            // [START_EXCLUDE]
                                            // [START get_face_info]
                                            Paint dot = new Paint();
                                            dot.setColor(Color.BLUE);
                                            dot.setStyle(Paint.Style.FILL);
                                            dot.setStrokeWidth(2.0F);
                                            Paint line = new Paint();
                                            line.setColor(Color.RED);
                                            line.setStyle(Paint.Style.STROKE);
                                            line.setStrokeWidth(2.0F);
                                            boolean smile = false;
                                            boolean wink = false;
                                            for (FirebaseVisionFace face : faces) {

                                                int i = 0;
                                                FirebaseVisionFaceContour faceContours = face.getContour(FirebaseVisionFaceContour.FACE);
                                                List contours = faceContours.getPoints();
                                                Iterator firebasePoint2 = ((Iterable)contours).iterator();
                                                float init_x = 0;
                                                float init_y = 0;
                                                if (firebasePoint2.hasNext())
                                                {
                                                    FirebaseVisionPoint init = (FirebaseVisionPoint) firebasePoint2.next();
                                                    init_x = init.getX();
                                                    init_y = init.getY();
                                                }
                                                else
                                                {
                                                    Toast.makeText(getBaseContext(), "No points to make a contour", Toast.LENGTH_SHORT).show();
                                                }

                                                for (Iterator firebasePoint = ((Iterable)contours).iterator(); firebasePoint.hasNext(); i++)
                                                {

                                                    FirebaseVisionPoint point = (FirebaseVisionPoint)firebasePoint.next();
                                                    float x_1 = point.getX();
                                                    float y_1 = point.getY();
                                                    if (firebasePoint.hasNext())
                                                    {
                                                        FirebaseVisionPoint nextPoint = (FirebaseVisionPoint) firebasePoint2.next();
                                                        float x_2 = nextPoint.getX();
                                                        float y_2 = nextPoint.getY();
                                                        Log.i(TAG,"x1y1>>>>" + String.valueOf(x_1) +" "+ String.valueOf(y_1)+" "+ String.valueOf(x_2)+" "+String.valueOf(y_2));
                                                        canvas.drawLine(x_1, y_1, x_2, y_2, line);

                                                    }
                                                    else
                                                    {
                                                        canvas.drawLine(x_1, y_1, init_x, init_y, line);
                                                    }
                                                    canvas.drawCircle(x_1, y_1,2.0F,dot);

                                                }
                                                output_image.setImageBitmap(Bitmap.createScaledBitmap(imageBitmap, 800, 900, false));

                                                Rect bounds = face.getBoundingBox();
                                                float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                                                float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees

                                                // Lower Lip
                                                faceContours = face.getContour(FirebaseVisionFaceContour.LOWER_LIP_BOTTOM);
                                                contours = faceContours.getPoints();
                                                firebasePoint2 = ((Iterable)contours).iterator();
                                                if (firebasePoint2.hasNext())
                                                {
                                                    FirebaseVisionPoint init = (FirebaseVisionPoint) firebasePoint2.next();
                                                }
                                                else
                                                {
                                                    Toast.makeText(getBaseContext(), "No points to make a contour", Toast.LENGTH_SHORT).show();
                                                }

                                                for (Iterator firebasePoint = ((Iterable)contours).iterator(); firebasePoint.hasNext(); i++)
                                                {

                                                    FirebaseVisionPoint point = (FirebaseVisionPoint)firebasePoint.next();
                                                    float x_1 = point.getX();
                                                    float y_1 = point.getY();
                                                    if (firebasePoint.hasNext())
                                                    {
                                                        FirebaseVisionPoint nextPoint = (FirebaseVisionPoint) firebasePoint2.next();
                                                        float x_2 = nextPoint.getX();
                                                        float y_2 = nextPoint.getY();
                                                        Log.i(TAG,"x1y1>>>>" + String.valueOf(x_1) +" "+ String.valueOf(y_1)+" "+ String.valueOf(x_2)+" "+String.valueOf(y_2));
                                                        canvas.drawLine(x_1, y_1, x_2, y_2, line);

                                                    }
                                                    else
                                                    {
                                                        //canvas.drawLine(x_1, y_1, init_x, init_y, line);
                                                    }
                                                    canvas.drawCircle(x_1, y_1,2.0F,dot);

                                                }
                                                output_image.setImageBitmap(Bitmap.createScaledBitmap(imageBitmap, 800, 900, false));



                                                // Upper Lip Top
                                                faceContours = face.getContour(FirebaseVisionFaceContour.UPPER_LIP_TOP);
                                                contours = faceContours.getPoints();
                                                firebasePoint2 = ((Iterable)contours).iterator();
                                                if (firebasePoint2.hasNext())
                                                {
                                                    FirebaseVisionPoint init = (FirebaseVisionPoint) firebasePoint2.next();
                                                }
                                                else
                                                {
                                                    Toast.makeText(getBaseContext(), "No points to make a contour", Toast.LENGTH_SHORT).show();
                                                }

                                                for (Iterator firebasePoint = ((Iterable)contours).iterator(); firebasePoint.hasNext(); i++)
                                                {

                                                    FirebaseVisionPoint point = (FirebaseVisionPoint)firebasePoint.next();
                                                    float x_1 = point.getX();
                                                    float y_1 = point.getY();
                                                    if (firebasePoint.hasNext())
                                                    {
                                                        FirebaseVisionPoint nextPoint = (FirebaseVisionPoint) firebasePoint2.next();
                                                        float x_2 = nextPoint.getX();
                                                        float y_2 = nextPoint.getY();
                                                        Log.i(TAG,"x1y1>>>>" + String.valueOf(x_1) +" "+ String.valueOf(y_1)+" "+ String.valueOf(x_2)+" "+String.valueOf(y_2));
                                                        canvas.drawLine(x_1, y_1, x_2, y_2, line);

                                                    }
                                                    else
                                                    {
                                                        //canvas.drawLine(x_1, y_1, init_x, init_y, line);
                                                    }
                                                    canvas.drawCircle(x_1, y_1,2.0F,dot);

                                                }
                                                output_image.setImageBitmap(Bitmap.createScaledBitmap(imageBitmap, 800, 900, false));

                                                // Left Eyebrow
                                                faceContours = face.getContour(FirebaseVisionFaceContour.LEFT_EYEBROW_TOP);
                                                contours = faceContours.getPoints();
                                                firebasePoint2 = ((Iterable)contours).iterator();
                                                if (firebasePoint2.hasNext())
                                                {
                                                    FirebaseVisionPoint init = (FirebaseVisionPoint) firebasePoint2.next();
                                                }
                                                else
                                                {
                                                    Toast.makeText(getBaseContext(), "No points to make a contour", Toast.LENGTH_SHORT).show();
                                                }

                                                for (Iterator firebasePoint = ((Iterable)contours).iterator(); firebasePoint.hasNext(); i++)
                                                {

                                                    FirebaseVisionPoint point = (FirebaseVisionPoint)firebasePoint.next();
                                                    float x_1 = point.getX();
                                                    float y_1 = point.getY();
                                                    if (firebasePoint.hasNext())
                                                    {
                                                        FirebaseVisionPoint nextPoint = (FirebaseVisionPoint) firebasePoint2.next();
                                                        float x_2 = nextPoint.getX();
                                                        float y_2 = nextPoint.getY();
                                                        Log.i(TAG,"x1y1>>>>" + String.valueOf(x_1) +" "+ String.valueOf(y_1)+" "+ String.valueOf(x_2)+" "+String.valueOf(y_2));
                                                        canvas.drawLine(x_1, y_1, x_2, y_2, line);

                                                    }
                                                    else
                                                    {
                                                        //canvas.drawLine(x_1, y_1, init_x, init_y, line);
                                                    }
                                                    canvas.drawCircle(x_1, y_1,2.0F,dot);

                                                }
                                                output_image.setImageBitmap(Bitmap.createScaledBitmap(imageBitmap, 800, 900, false));


                                                //Right Eyebrow
                                                faceContours = face.getContour(FirebaseVisionFaceContour.RIGHT_EYEBROW_TOP);
                                                contours = faceContours.getPoints();
                                                firebasePoint2 = ((Iterable)contours).iterator();
                                                if (firebasePoint2.hasNext())
                                                {
                                                    FirebaseVisionPoint init = (FirebaseVisionPoint) firebasePoint2.next();
                                                }
                                                else
                                                {
                                                    Toast.makeText(getBaseContext(), "No points to make a contour", Toast.LENGTH_SHORT).show();
                                                }

                                                for (Iterator firebasePoint = ((Iterable)contours).iterator(); firebasePoint.hasNext(); i++)
                                                {

                                                    FirebaseVisionPoint point = (FirebaseVisionPoint)firebasePoint.next();
                                                    float x_1 = point.getX();
                                                    float y_1 = point.getY();
                                                    if (firebasePoint.hasNext())
                                                    {
                                                        FirebaseVisionPoint nextPoint = (FirebaseVisionPoint) firebasePoint2.next();
                                                        float x_2 = nextPoint.getX();
                                                        float y_2 = nextPoint.getY();
                                                        Log.i(TAG,"x1y1>>>>" + String.valueOf(x_1) +" "+ String.valueOf(y_1)+" "+ String.valueOf(x_2)+" "+String.valueOf(y_2));
                                                        canvas.drawLine(x_1, y_1, x_2, y_2, line);

                                                    }
                                                    else
                                                    {
                                                        //canvas.drawLine(x_1, y_1, init_x, init_y, line);
                                                    }
                                                    canvas.drawCircle(x_1, y_1,2.0F,dot);

                                                }
                                                output_image.setImageBitmap(Bitmap.createScaledBitmap(imageBitmap, 800, 900, false));


                                                // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                                                // nose available):

                                                FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
                                                if (leftEar != null) {
                                                    FirebaseVisionPoint leftEarPos = leftEar.getPosition();
                                                    Log.i(TAG, "LeftEar >>>>>>>>>>>>>>>>>>");
                                                }

                                                // If classification was enabled:
                                                if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                                    float smileProb = face.getSmilingProbability();
                                                    if (smileProb > 0.5)
                                                    {
                                                        smile = true;
                                                    }
                                                    Log.i(TAG, "Smile >>>>>>>>>>>>>>>>>>");
                                                    Log.i(TAG, String.valueOf(smileProb));

                                                }
                                                if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                                    float rightEyeOpenProb = face.getRightEyeOpenProbability();
                                                    Log.i(TAG, "RightEYE >>>>>>>>>>>>>>>>>>");
                                                    Log.i(TAG, String.valueOf(rightEyeOpenProb));
                                                    if (rightEyeOpenProb < 0.3)
                                                    {
                                                        wink = true;
                                                    }
                                                }

                                                if (face.getLeftEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY)
                                                {
                                                    float leftEyeOpenProb = face.getLeftEyeOpenProbability();
                                                    if (leftEyeOpenProb < 0.3)
                                                    {
                                                        wink = true;
                                                    }
                                                }

                                                // If face tracking was enabled:
                                                if (face.getTrackingId() != FirebaseVisionFace.INVALID_ID) {
                                                    int id = face.getTrackingId();
                                                    Log.i(TAG, "TrackingID >>>>>>>>>>>>>>>>>>");
                                                    Log.i(TAG, String.valueOf(id));

                                                }
                                            }

                                            TextView tv = findViewById(R.id.smiles);
                                            if (smile)
                                            {
                                                tv.setText("Smiles : Yes");
                                            }
                                            else
                                            {
                                                tv.setText("Smiles : NO");
                                            }
                                            tv.setVisibility(View.VISIBLE);

                                            tv = findViewById(R.id.winks);
                                            if (wink)
                                            {
                                                tv.setText("Winks : Yes");
                                            }
                                            else
                                            {
                                                tv.setText("Winks : NO");
                                            }
                                            tv.setVisibility(View.VISIBLE);



                                            // [END get_face_info]
                                            // [END_EXCLUDE]
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            // ...
                                            Log.i(TAG, "Failure >>>>>>>>>>>>>>>>>>");

                                        }
                                    });

            Log.i(TAG, result.getClass().getName());

        }
    }
}
