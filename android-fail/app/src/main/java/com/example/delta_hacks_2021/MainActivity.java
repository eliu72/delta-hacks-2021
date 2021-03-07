package com.example.delta_hacks_2021;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Imports the Google Cloud client library



public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView imageView;
    private TextView textResult;
    private Bitmap bitmap;
    String currentPhotoPath;
    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.imageView = (ImageView)this.findViewById(R.id.imageView1);
        this.textResult = (TextView)this.findViewById(R.id.textResult);

        this.bitmap = BitmapFactory.decodeResource(
                getApplicationContext().getResources(),
                R.drawable.icon
        );


        Vision.Builder visionBuilder = new Vision.Builder(
                new NetHttpTransport(),
                new AndroidJsonFactory(),
                null);

        visionBuilder.setVisionRequestInitializer(
                new VisionRequestInitializer("AIzaSyDoaKaDJ3jIQ6Q64sgeFc9OdC44UQi9Brs"));

        Vision vision = visionBuilder.build();



        Button photoButton = (Button) this.findViewById(R.id.button_capture);
        photoButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // take the picture and save it locally
                // auto calls onActivityResult() after successful picture
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                dispatchTakePictureIntent();

            }
        });


        Button detectTextButton = (Button) this.findViewById(R.id.detect_text_button);
        detectTextButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
                if(!textRecognizer.isOperational())
                    Log.e("ERROR", "Detector dependencies are noy yet available");
                else{
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items = textRecognizer.detect(frame);
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i=0; i<items.size(); i++){
                        TextBlock item = items.valueAt(i);
                        stringBuilder.append(item.getValue());
                        stringBuilder.append(("\n"));

                    }
                    textResult.setText(stringBuilder.toString());
                }

            }
        });


//        Bitmap myBitmap = BitmapFactory.decodeFile(currentPhotoPath);
//        System.out.println("this is IT: " + currentPhotoPath);
//        imageView.setImageBitmap(myBitmap);
    }




    // take picture method
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                System.out.println("Error saving file");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
//        // return extras
//        return takePictureIntent.getExtras();
    }


    // method that creates an image file
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public BatchAnnotateImagesResponse annotateImage(){
        try {
            GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
            HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

            Vision.Builder builder = new Vision.Builder
                    (httpTransport, jsonFactory, credential);
            Vision vision = builder.build();

            List<Feature> featureList = new ArrayList<>();
            Feature labelDetection = new Feature();
            labelDetection.setType("LABEL_DETECTION");
            labelDetection.setMaxResults(10);
            featureList.add(labelDetection);

            Feature textDetection = new Feature();
            textDetection.setType("TEXT_DETECTION");
            textDetection.setMaxResults(10);
            featureList.add(textDetection);

            List<AnnotateImageRequest> imageList = new ArrayList<>();
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();
            Image base64EncodedImage = getBase64EncodedJpeg(bitmap);
            annotateImageRequest.setImage(base64EncodedImage);
            annotateImageRequest.setFeatures(featureList);
            imageList.add(annotateImageRequest);

            BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                    new BatchAnnotateImagesRequest();
            batchAnnotateImagesRequest.setRequests(imageList);

            Vision.Images.Annotate annotateRequest =
                    vision.images().annotate(batchAnnotateImagesRequest);
            annotateRequest.setDisableGZipContent(true);
            Log.d(TAG, "Sending request to Google Cloud");

            BatchAnnotateImagesResponse response = annotateRequest.execute();
            return response;

        } catch (GoogleJsonResponseException e) {
            Log.e(TAG, "Request error: " + e.getContent());
        } catch (IOException e) {
            Log.d(TAG, "Request error: " + e.getMessage());
        }
        return null;
    }

}
