package com.example.calocount.fragments.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.File;

import com.example.calocount.Constants;
import com.example.calocount.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        loadProfileData();

        return root;
    }

    private void loadProfileData() {
        SharedPreferences prefs = getContext().getSharedPreferences(Constants.SHARED_PREFS_NAME, getContext().MODE_PRIVATE);

        String username = prefs.getString(Constants.KEY_USERNAME, "User");
        String email = prefs.getString(Constants.KEY_EMAIL, "");
        String timeFormat = prefs.getString(Constants.KEY_TIME_FORMAT, "24-hour format");
        boolean soundEffects = prefs.getBoolean(Constants.KEY_SOUND_EFFECTS_ENABLED, false);

        binding.textViewName.setText(username);
        binding.textViewEmail.setText(email);
        binding.textViewTimeFormat.setText("Time Format: " + timeFormat);
        binding.textViewSoundEffects.setText("Sound Effects: " + (soundEffects ? "Enabled" : "Disabled"));

        loadProfileImage();
    }

    private void loadProfileImage() {
        SharedPreferences prefs = getContext().getSharedPreferences(Constants.SHARED_PREFS_NAME, getContext().MODE_PRIVATE);

        String imagePath = prefs.getString(Constants.KEY_PROFILE_IMAGE_PATH, "");
        String imageSource = prefs.getString(Constants.KEY_PROFILE_IMAGE_SOURCE, "none");

        if (!imagePath.isEmpty()) {
            if ("camera".equals(imageSource)) {
                // Load from internal storage (camera images)
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                    if (bitmap != null) {
                        binding.imageViewProfile.setImageBitmap(bitmap);
                    } else {
                        // Fallback to default avatar
                        binding.imageViewProfile.setImageResource(android.R.drawable.sym_def_app_icon);
                    }
                } else {
                    // File doesn't exist, show default avatar
                    binding.imageViewProfile.setImageResource(android.R.drawable.sym_def_app_icon);
                }
            } else if ("gallery".equals(imageSource)) {
                // Load from URI (gallery images)
                try {
                    Uri imageUri = Uri.parse(imagePath);
                    getContext().getContentResolver().takePersistableUriPermission(
                            imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    binding.imageViewProfile.setImageURI(imageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                    // If URI is no longer valid, show default avatar
                    binding.imageViewProfile.setImageResource(android.R.drawable.sym_def_app_icon);
                }
            }
        } else {
            // No image selected, show default avatar
            binding.imageViewProfile.setImageResource(android.R.drawable.sym_def_app_icon);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}