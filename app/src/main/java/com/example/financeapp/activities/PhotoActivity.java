package com.example.financeapp.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import com.example.financeapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;


import java.io.File;
import java.io.IOException;

import java.text.SimpleDateFormat;
import java.util.Date;


public class PhotoActivity extends Activity {
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    String currentPhotoPath;
    Uri photoUri;

    @Override
    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_transaction);
        //Button to take photo
        Button takePhotoButton = (Button)this.findViewById(R.id.photoButton);

        takePhotoButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                System.out.println("Button clicked");
                if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }else {
                    dispatchTakePictureIntent();
                }
            }
        });
    }

    //For taking and saving the picture
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Ensure there is a camera activity to handle the intent
        if(takePictureIntent.resolveActivity(getPackageManager()) != null) {
            //Create file where photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            }catch (IOException e){
                //Error occured while creating the file
                System.out.println("Exception in dispatch: " + e.toString());
            }
            //continue only if the file was created correctly
            if(photoFile != null){
                photoUri = FileProvider.getUriForFile(
                        this, "com.example.android.fileprovider", photoFile
                );
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == MY_CAMERA_PERMISSION_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();

            }else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        int rotationDegree = 90;

        if(requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK){
            //Get and set image from URI
            Bitmap bitmapImage = null;
            try {
                bitmapImage = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), photoUri);
            }catch (Exception e){
                System.out.println("Exception in result: " + e.toString());
            }

            //Firebase sdk kit
            InputImage image = InputImage.fromBitmap(bitmapImage, rotationDegree);
            TextRecognizer recognizer = TextRecognition.getClient();

            //Process text captured from image
            Task<Text> result = recognizer.process(image)
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text text) {
                            String resultText = text.getText();
                            //Comment output
                            System.out.println(resultText);
                            for(Text.TextBlock block : text.getTextBlocks()) {
                                String blockText = block.getText();
                                Point[] blockCornerPoints = block.getCornerPoints();
                                Rect blockFrame = block.getBoundingBox();

                                for(Text.Line line : block.getLines()) {
                                    String lineText = line.getText();
                                    Point[] lineCornerPoints = line.getCornerPoints();
                                    Rect lineFrame = line.getBoundingBox();

                                    for(Text.Element element : line.getElements()) {
                                        String elementText = element.getText();
                                        Point[] elementCornerPoints = element.getCornerPoints();
                                        Rect elementFrame = element.getBoundingBox();
                                        //Elements
                                    }
                                    //Line text
                                }
                                //Block text
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println("ML Kit Failure");
                        }
                    });
        }
    }

    //Create image file
    private File createImageFile() throws IOException {
        //Create image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        //save a file path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
