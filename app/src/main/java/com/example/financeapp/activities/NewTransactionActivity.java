package com.example.financeapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;

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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.financeapp.R;
import com.example.financeapp.utilities.Transaction;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class NewTransactionActivity extends HomePageActivity {
    private EditText amount, source, description;
    private Button submit;
    private FirebaseAuth mAuth;
    private RadioGroup radioGroup;

    //Image capture
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    String currentPhotoPath;
    Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_new_transaction);
        //navigation
        DrawerLayout drawerLayout;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener((NavigationView.OnNavigationItemSelectedListener) this);
        //
        submit = (Button) findViewById(R.id.submitButton);
        amount = (EditText) findViewById(R.id.amountText);
        source = (EditText) findViewById(R.id.sourceText);
        description = (EditText) findViewById(R.id.descriptionText);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        mAuth = FirebaseAuth.getInstance();
        submit.setOnClickListener(this);

        //Image capture
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

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.submitButton:
                submitTransaction();
                break;
        }
    }

    private void submitTransaction() {
        String amountGiven = amount.getText().toString();
        String sourceGiven = source.getText().toString();
        String descriptionGiven = description.getText().toString();
        String clientId = mAuth.getCurrentUser().getUid();
        String earnedOrSpent = "";

        if(amountGiven.isEmpty()){
            amount.setError("Please provide the amount");
            amount.requestFocus();
            return;
        }

        if(sourceGiven.isEmpty()){
            source.setError("Please provide the source");
            source.requestFocus();
            return;
        }

        if(descriptionGiven.isEmpty()){
            descriptionGiven = "";
        }

        int selectedRadioButton = radioGroup.getCheckedRadioButtonId();

        if(selectedRadioButton == R.id.radioSpending){
            Log.d("NewTransactionActivity", "Clicked on Spending");
            earnedOrSpent = "Spent";
        }
        else{
            Log.d("NewTransactionActivity", "Clicked on Earning");
            earnedOrSpent = "Earned";
        }

        storeTransactionToDatabase(clientId, earnedOrSpent, amountGiven, sourceGiven, descriptionGiven);

    }

    private void storeTransactionToDatabase(final String clientId, final String earnedOrSpent, final String amountGiven, String sourceGiven, String descriptionGiven) {
        Transaction transaction = new Transaction(clientId, earnedOrSpent, amountGiven, sourceGiven, descriptionGiven);
        try {
            String transactionId = UUID.randomUUID().toString();
            DatabaseReference database = FirebaseDatabase.getInstance().getReference("Transactions/" + transactionId);
            database.setValue(transaction).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        updateCurrentBalance(earnedOrSpent, clientId, amountGiven);
                        Log.d("NewTransactionActivity", "Successfully added Transaction to Database");
                    } else {
                        Log.d("NewTransactionActivity", "Failed to add Transaction to Database");
                    }
                }
            });
        }catch(Exception e){
            Log.d("NewTransactionActivity", e.toString());
        }
    }

    private void updateCurrentBalance(final String earnedOrSpent, String clientId, final String amountGiven) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Clients/" + clientId);

        try{
            final String[] currentValue = new String[1];
            ref.child("currentBalance").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful()){
                        currentValue[0] = String.valueOf(task.getResult().getValue());
                        Double newValue = 0.0;
                        if(earnedOrSpent.equalsIgnoreCase("spent")){
                            newValue = Double.parseDouble(currentValue[0]) - Double.parseDouble(amountGiven);
                        }else{
                            newValue = Double.parseDouble(currentValue[0]) + Double.parseDouble(amountGiven);
                        }

                        updateBalanceInDatabase(ref, newValue);
                        Log.d("Firebase", currentValue[0]);
                    }
                }
            });
        }catch(Exception e){
            Log.d("NewTransactionActivity", e.toString());
        }

    }

    private void updateBalanceInDatabase(DatabaseReference ref, final double newValue){
        ref.child("currentBalance").setValue(newValue).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d("Firebase", "Added New Value "+ newValue + " ");
                    startActivity(new Intent(NewTransactionActivity.this, HomePageActivity.class));
                }
            }
        });
    }

    //Image capture
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
        super.onActivityResult(requestCode, resultCode, data);
        int rotationDegree = 90;

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            //Get and set image from URI
            Bitmap bitmapImage = null;
            try {
                bitmapImage = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), photoUri);
            } catch (Exception e) {
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
                            //System.out.println(resultText);
                            for (Text.TextBlock block : text.getTextBlocks()) {
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
                                        //Elements
                                    }
                                    //Line text
                                    System.out.println("Line text: " + lineText);
                                }
                                //Block text
                                //System.out.println("Block text: " + blockText);
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