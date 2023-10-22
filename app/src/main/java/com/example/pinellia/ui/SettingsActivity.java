package com.example.pinellia.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.pinellia.R;
import com.example.pinellia.databinding.ActivityBrowseHistoryBinding;
import com.example.pinellia.databinding.ActivitySettingsBinding;

import java.io.File;
import java.util.Set;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();
        setContentView(root);

        // Enable action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding.textVersion.setText(getAppVersion());

        binding.buttonClearCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClearCacheClicked();
            }
        });

        binding.buttonContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Implement logic to contact support
                // For example, you can launch an email intent to contact support.
            }
        });

        // Calculate and display the data size
        calculateAndDisplayDataSize();
    }

    // Call this function when the user taps the clear cache button
    private void onClearCacheClicked() {
        // Specify the directory where your images are stored
        String directoryPath = getExternalFilesDir(null).getAbsolutePath();

        clearImagesInDirectory(directoryPath);

        // Notify the user
        Toast.makeText(this, "Cache cleared.", Toast.LENGTH_SHORT).show();
    }

    // Function to clear images in the external directory
    private void clearImagesInDirectory(String directoryPath) {
        File directory = new File(directoryPath);

        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".jpg")) {
                        boolean deleted = file.delete();
                        if (deleted) {
                            // File was successfully deleted
                            Log.d("SettingsActivity", "File deleted: " + file.getName());
                        }
                    }
                }
            }
        }
    }

    // Calculate and display the data size
    private void calculateAndDisplayDataSize() {
        String directoryPath = getExternalFilesDir(null).getAbsolutePath();
        long dataSize = calculateDirectorySize(new File(directoryPath));

        // Convert dataSize to human-readable format (e.g., MB, GB)
        String dataSizeFormatted = formatSize(dataSize);

        // Display the data size in your TextView
        binding.textViewImageDataSize.setText(dataSizeFormatted);
    }

    // Method to calculate the size of a directory and its contents
    private long calculateDirectorySize(File directory) {
        long length = 0;
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    length += file.length();
                } else if (file.isDirectory()) {
                    length += calculateDirectorySize(file);
                }
            }
        }

        return length;
    }

    // Method to format data size in human-readable format
    private String formatSize(long size) {
        if (size <= 0) {
            return "0 B";
        }

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    // Method to get the app version
    private String getAppVersion() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "N/A";
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle the back button click in the action bar
        onBackPressed();
        return true;
    }
}