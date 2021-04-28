package com.example.financeapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class MyCameraActivity extends Activity
{
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private FirebaseFunctions mFunctions;


    //OCR testing
    String url = "https://api.ocr.space/parse/image";

    private String mApiKey = "Hello world";
    private String mImage;


    String currentPhotoPath;
    Uri photoUri;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_takephoto);
        this.imageView = (ImageView)this.findViewById(R.id.imageView1);
        Button photoButton = (Button) this.findViewById(R.id.button1);
        mFunctions = FirebaseFunctions.getInstance();
        photoButton.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v)
            {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }
                else
                {
                    //Original code
//                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                    startActivityForResult(cameraIntent, CAMERA_REQUEST);

                    //Saving the uri and the full image
                    dispatchTakePictureIntent();
                }
            }
        });
    }

    //for taking and saving a picture
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Ensure that theres a camera activity to handle the intent
        if(takePictureIntent.resolveActivity(getPackageManager()) != null) {
            //Create file where photo should go
            File photoFile = null;
            try{
                photoFile = createImageFile();
            }catch (IOException e){
                //Error occured while creating the file
                System.out.println("Exception in dispatch: " + e.toString());
            }
            //Continue only if the file was created successfully
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
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
//                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                //dispatchTakePictureIntent();
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int rotationDegree = 90;


        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {

            //Photo right here
//            Bitmap photo = (Bitmap) data.getExtras().get("data");
//            imageView.setImageBitmap(photo); //displays photo as bitmap into imageView

            //Get and set image from URI
            Bitmap bitmapImage = null;
            try{
                bitmapImage = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), photoUri);
            }catch (Exception e){
                System.out.println("Exception in result: " + e.toString());
            }

            if(bitmapImage != null){
                System.out.println("NOT NULL!");
                imageView.setImageBitmap(bitmapImage);
            }



            //Cloud API test
//            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//            photo.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//            byte[] imageBytes = byteArrayOutputStream.toByteArray();
//            final String base64encoded = Base64.encodeToString(imageBytes, Base64.NO_WRAP);

            //OCR
//            System.out.println("HERE I AM TRYING OCR");
//            makeSendPost(base64encoded);

//            try{
//                String response = sendPost(mApiKey, base64encoded);
//                System.out.println("Response from OCR: " + response);
//            }catch (Exception e){
//                System.out.println(e.toString());
//            }


            //***************************************************
            //This is the firebase function code
            //Create jason request to cloud vision
//            JsonObject request = new JsonObject();
//            //Add image to request
//            JsonObject image = new JsonObject();
//            image.add("content", new JsonPrimitive(base64encoded));
//            request.add("image", image);
//            //Add features to the request
//            JsonObject feature = new JsonObject();
//            feature.add("type", new JsonPrimitive("DOCUMENT_TEXT_DETECTION")); //"DOCUMENT_TEXT_DETECTION"
//            JsonArray features = new JsonArray();
//            features.add(feature);
//            request.add("features", features);
//
//            //invoke the function
//            System.out.println("Here: Call to annotateImage");
//            annotateImage(request.toString())
//                    .addOnCompleteListener(new OnCompleteListener<JsonElement>() {
//                        @Override
//                        public void onComplete(@NonNull Task<JsonElement> task) {
//
//                            if(!task.isSuccessful()){
//                                //task failed with an exception
//                                System.out.println("*********************ERROR****************************");
//                                System.out.println(task.getException().toString());
//                            } else {
//                                //Task completed successfully
//
//                                JsonObject annotation = task.getResult().getAsJsonArray().get(0).getAsJsonObject().get("fullTextAnnotation").getAsJsonObject();
//                                System.out.format("%nComplete annotation: %n");
//                                System.out.format("%s%n", annotation.get("text").getAsString());
//                            }
//                        }
//                    });


            //Code for firebase sdk kit
            //InputImage image = InputImage.fromBitmap(photo, rotationDegree); //from original photo

            InputImage image = InputImage.fromBitmap(bitmapImage, rotationDegree);
            TextRecognizer recognizer = TextRecognition.getClient();

            //Process text captured from image
            Task<Text> result = recognizer.process(image)
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text text) {
                            String resultText = text.getText();
                            System.out.println("Here");
                            System.out.println(resultText);
                            for(Text.TextBlock block : text.getTextBlocks()) {
                                String blockText = block.getText();
                                Point[] blockCornerPoints = block.getCornerPoints();
                                Rect blockFrame = block.getBoundingBox();
                                for (Text.Line line : block.getLines()) {
                                    String lineText = line.getText();
                                    Point[] lineCornerPoints = line.getCornerPoints();
                                    Rect lineFrame = line.getBoundingBox();

                                    for (Text.Element element : line.getElements()) {
                                        String elementText = element.getText();
                                        Point[] elementCornerPoints = element.getCornerPoints();
                                        Rect elementFrame = element.getBoundingBox();
                                        //System.out.println("Element Text: " + elementText);
                                    }
                                    //System.out.println("Line Text: " + lineText);
                                }
                                System.out.println("Block text: " + blockText);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

        }
    }

    //Create image file
    private File createImageFile() throws IOException {
        //Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        //save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

//Firebase function annotate image
    private Task<JsonElement> annotateImage(String requestJson) {
        System.out.println("In annotateImage method");
        return mFunctions
                .getHttpsCallable("annotateImage")
                .call(requestJson)
                .continueWith(new Continuation<HttpsCallableResult, JsonElement>() {
                    @Override
                    public JsonElement then(@NonNull Task<HttpsCallableResult> task)  {
                        return JsonParser.parseString(new Gson().toJson(task.getResult().getData()));
                    }
                });
    }

    public void makeSendPost(final String base64) {
        System.out.println("Inside makeSendPost");

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    String response = sendPost(mApiKey, base64);
                    System.out.println("Response from OCR: " + response);
                }catch (Exception e){
                    System.out.println("makeSendPost: Exception");
                    System.out.println(e.toString());
                }
            }
        });

        thread.start();

    }

    //Free OCR API testing
    private String sendPost(String apiKey, String image) throws Exception {
        URL obj = new URL(url);
        HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();

        //add request header
        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        JSONObject postDataParams = new JSONObject();

        postDataParams.put("apiKey", apiKey);
        //postDataParams.put("isOverlayRequired", isOverlayRequired);
        postDataParams.put("base64image", "data:image/bmp;base64," + image);
        postDataParams.put("filetype", "bmp");
        postDataParams.put("scale", "true");
        postDataParams.put("isTable", "true");
        //postDataParams.put("isCreateSearchablePdf", "true");
        //postDataParams.put("OCREngine", "2");
        //postDataParams.put("language", language);

        //send post request
        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(getPostDataString(postDataParams));
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null){
            response.append(inputLine);
        }
        in.close();

        //return result

        return String.valueOf(response);
    }

    //OCR get text
    public String getPostDataString(JSONObject params) throws Exception {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()) {
            String key = itr.next();
            Object value = params.get(key);

            if(first){
                first = false;
            }else {
                result.append("&");
            }

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }
        System.out.println("getPostDataString: " + result.toString());
        return result.toString();
    }

}

