 package com.example.yourselfie;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Arrays;
import java.util.Map;

 public class MainActivity extends AppCompatActivity {

     private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};
     TextView userName;

     PreviewView previewView;
    GoogleSignInClient googleClient;
    GoogleSignInOptions googleOptions;

    private Button cameraButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraButton = findViewById(R.id.camera_button);
        userName = findViewById(R.id.userName);
        googleOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleClient = GoogleSignIn.getClient(this, googleOptions);
        GoogleSignInAccount gAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (gAccount != null){
            String gName = gAccount.getDisplayName();
            userName.setText(gName);
        }

        ActivityResultLauncher<String[]> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> permissions) {
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
                }
        );

        cameraButton.setOnClickListener(view -> {
         activityResultLauncher.launch(REQUIRED_PERMISSIONS);
     });
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

                 // Bind use cases to camera
                 cameraProvider.bindToLifecycle(this, cameraSelector, preview);
             } catch (Exception e) {
                 Log.e(TAG, "Use case binding failed", e);
             }
         }, ContextCompat.getMainExecutor(this));
     }



 }