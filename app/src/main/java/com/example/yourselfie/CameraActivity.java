package com.example.yourselfie;

import static android.content.ContentValues.TAG;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CameraActivity extends AppCompatActivity implements SmileyAdapter.OnItemClickListener {
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};
    ImageCapture imageCapture;
    PreviewView previewView;
    ImageView photoView;
    SmileyAdapter smileyAdapter;
    RecyclerView smileyRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.previewView);
        photoView = findViewById(R.id.photo);

        smileyRecyclerView = findViewById(R.id.filterRecyclerView);
        smileyRecyclerView.setLayoutManager( new CenteredLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        // Add item decoration
        int itemSpacing = getResources().getDimensionPixelSize(R.dimen.filter_item_spacing);
        Context context = this;
        smileyRecyclerView.addItemDecoration(new FilterItemDecoration(context, itemSpacing));

        // Creates the list of images
        List<Integer> filterImages = new ArrayList<>();
        filterImages.add(R.drawable.grey_smiley);
        filterImages.add(R.drawable.red_smiley);
        filterImages.add(R.drawable.yellow_smiley);
        filterImages.add(R.drawable.green_smiley);
        filterImages.add(R.drawable.blue_smiley);


        smileyAdapter = new SmileyAdapter(this, filterImages);
        smileyAdapter.setItemClickListener(this);
        smileyRecyclerView.setAdapter(smileyAdapter);

        SnapHelper snapHelper = new GravitySnapHelper(Gravity.CENTER);
        snapHelper.attachToRecyclerView(smileyRecyclerView);


        ActivityResultLauncher<String[]> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    // Handle Permission granted/rejected
                    boolean permissionGranted = true;
                    for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                        String key = entry.getKey();
                        boolean value = entry.getValue();
                        if (Arrays.asList(REQUIRED_PERMISSIONS).contains(key) && !value) {
                            permissionGranted = false;
                        }
                    }
                    if (!permissionGranted) {
                        Toast.makeText(getApplicationContext(),
                                "Permission request denied",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        startCamera();
                    }
                }
        );

        activityResultLauncher.launch(REQUIRED_PERMISSIONS);
    }
//Overriding the onclick from the interface inside the SmileyAdapter class
    @Override
    public void onItemClick(int position) {
        takePhoto(position);
        Toast.makeText(this, "Clicked position: " + position, Toast.LENGTH_SHORT).show();
    }

    private void startCamera() {

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                // Used to bind the lifecycle of cameras to the lifecycle owner
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Select front camera as a default
                CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

                // Unbind use cases before rebinding
                cameraProvider.unbindAll();

                imageCapture =
                        new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                .setTargetRotation(previewView.getDisplay().getRotation())
                                .build();

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (Exception e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto(int position) {


        //saving position to database

        // Creates time stamped name and MediaStore entry.
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/YourSelfie");

        // Creates output options object which contains file + metadata
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(
                getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
                .build();

        // Takes ans saves the photo to the given uri
        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onError(@NonNull ImageCaptureException exc) {
                        Log.e(TAG, "Photo capture failed: " + exc.getMessage(), exc);
                    }

                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        String msg = "Photo capture succeeded: " + output.getSavedUri();
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, msg);
                        ImageDecoder.Source source = ImageDecoder.createSource(getApplicationContext().getContentResolver(), output.getSavedUri());
                        try {
                            Bitmap bitmap = ImageDecoder.decodeBitmap(source);
                            photoView.setImageBitmap(bitmap);
                            Log.d(TAG, "SETTING IMAGE TO VIEW *******************");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
    }
    public class FilterItemDecoration extends RecyclerView.ItemDecoration {

        private int itemSpacing;
        private int centerPosition;

        FilterItemDecoration(Context context, int itemSpacing) {
            this.itemSpacing = dpToPx(context, itemSpacing);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);

            int position = parent.getChildAdapterPosition(view);
            int itemCount = parent.getAdapter().getItemCount();

            // Calculate the center position
            centerPosition = itemCount / 2;
            System.out.println(centerPosition);

            if (position == centerPosition - 2) {
                // First image
                int spacing = (parent.getWidth() - view.getWidth() - 4 * itemSpacing) / 2;
                outRect.left = spacing;
                outRect.right = itemSpacing;
            } else if (position == centerPosition + 2) {
                // Last image
                int spacing = (parent.getWidth() - view.getWidth() - 4 * itemSpacing) / 2;
                outRect.left = itemSpacing;
                outRect.right = spacing;
            } else if (position == centerPosition) {
                // Center image
                outRect.left = itemSpacing;
                outRect.right = itemSpacing;
            } else {
                // Other images
                outRect.left = itemSpacing;
                outRect.right = itemSpacing;
            }
        }

        private int dpToPx(Context context, int dp) {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        }
    }
}