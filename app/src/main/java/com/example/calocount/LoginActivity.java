package com.example.calocount;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import com.example.calocount.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private static final String PROFILE_IMAGE_FILENAME = "profile_image.jpg";
    private ActivityLoginBinding binding;
    private SharedPreferences sharedPreferences;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if already logged in
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS_NAME, MODE_PRIVATE);
        if (isAlreadyLoggedIn()) {
            navigateToMainActivity();
            return;
        }
        
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupActivityResultLaunchers();

        setupSpinner();
        setupImageButtons();
        setupLoginButton();

        loadExistingProfileImage();
    }
    
    private boolean isAlreadyLoggedIn() {
        return sharedPreferences.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
    }
    
    private void setupSpinner() {
        String[] timeFormats = {"12-hour format", "24-hour format"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, timeFormats);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTimeFormat.setAdapter(adapter);
    }
    
    private void setupLoginButton() {
        binding.buttonLogin.setOnClickListener(v -> {
            if (validateInput()) {
                saveUserData();
                navigateToMainActivity();
            }
        });
    }

    private void setupImageButtons() {
        binding.buttonCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        binding.buttonGallery.setOnClickListener(v -> {
            openGallery();
        });
    }

    private void setupActivityResultLaunchers() {
        // Camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            binding.imageViewProfile.setImageBitmap(imageBitmap);
                            saveImageToInternalStorage(imageBitmap);
                        }
                    }
                });

        // Gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            try {
                                binding.imageViewProfile.setImageURI(selectedImageUri);

                                getContentResolver().takePersistableUriPermission(
                                        selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                saveGalleryImagePath(selectedImageUri.toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Error loading image from gallery", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });

        // Camera permission launcher
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(takePictureIntent);
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(pickPhotoIntent);
    }

    private void saveImageToInternalStorage(Bitmap bitmap) {
        try {
            File file = new File(getFilesDir(), PROFILE_IMAGE_FILENAME);
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Constants.KEY_PROFILE_IMAGE_SOURCE, "camera");
            editor.putString(Constants.KEY_PROFILE_IMAGE_PATH, file.getAbsolutePath());
            editor.apply();

            Toast.makeText(this, "Profile image saved!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveGalleryImagePath(String uriString) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.KEY_PROFILE_IMAGE_SOURCE, "gallery");
        editor.putString(Constants.KEY_PROFILE_IMAGE_PATH, uriString);
        editor.apply();

        Toast.makeText(this, "Profile image selected!", Toast.LENGTH_SHORT).show();
    }

    private void loadExistingProfileImage() {
        String imagePath = sharedPreferences.getString(Constants.KEY_PROFILE_IMAGE_PATH, "");
        String imageSource = sharedPreferences.getString(Constants.KEY_PROFILE_IMAGE_SOURCE, "none");

        if (!imagePath.isEmpty()) {
            if ("camera".equals(imageSource)) {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                    if (bitmap != null) {
                        binding.imageViewProfile.setImageBitmap(bitmap);
                    }
                }
            } else if ("gallery".equals(imageSource)) {
                try {
                    Uri imageUri = Uri.parse(imagePath);
                    binding.imageViewProfile.setImageURI(imageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                    binding.imageViewProfile.setImageResource(android.R.drawable.sym_def_app_icon);
                }
            }
        }
    }

    private boolean validateInput() {
        String name = binding.editTextName.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();
        String email = binding.editTextEmail.getText().toString().trim();
        
        if (name.isEmpty()) {
            binding.editTextName.setError("Name is required");
            binding.editTextName.requestFocus();
            return false;
        }
        
        if (password.isEmpty()) {
            binding.editTextPassword.setError("Password is required");
            binding.editTextPassword.requestFocus();
            return false;
        }
        
        if (email.isEmpty()) {
            binding.editTextEmail.setError("Email is required");
            binding.editTextEmail.requestFocus();
            return false;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editTextEmail.setError("Please enter a valid email");
            binding.editTextEmail.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void saveUserData() {
        String name = binding.editTextName.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();
        String email = binding.editTextEmail.getText().toString().trim();
        String timeFormat = binding.spinnerTimeFormat.getSelectedItem().toString();
        boolean soundEffects = binding.checkBoxSoundEffects.isChecked();
        
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.KEY_USERNAME, name);
        editor.putString(Constants.KEY_PASSWORD, password);
        editor.putString(Constants.KEY_EMAIL, email);
        editor.putString(Constants.KEY_TIME_FORMAT, timeFormat);
        editor.putBoolean(Constants.KEY_SOUND_EFFECTS_ENABLED, soundEffects);
        //editor.putString(Constants.KEY_PROFILE_IMAGE_SOURCE, "none");
        //editor.putString(Constants.KEY_PROFILE_IMAGE_PATH, "");
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
        editor.apply();
        
        Toast.makeText(this, "Welcome, " + name + "!", Toast.LENGTH_SHORT).show();
    }
    
    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // IMPORTANT: Prevents going back to login screen
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}