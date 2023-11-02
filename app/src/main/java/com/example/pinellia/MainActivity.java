package com.example.pinellia;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.pinellia.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "AppPreferences";
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_recognition, R.id.navigation_selfcare, R.id.navigation_favourites)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // Check if the dialog should be shown
        if (shouldShowDialog()) {
            // Create and show the AlertDialog
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setTitle("User Acknowledgement")
                    .setMessage("This app is designed for informational purposes and as a supplement to your wellness journey. While it provides insights into Traditional Chinese Medicine (TCM) and herbal remedies, it is not a substitute for professional medical advice, diagnosis, or treatment. Always consult with a qualified healthcare provider for any health-related concerns.")
                    .setPositiveButton("I understand", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Handle the "OK" button click
                            dialog.dismiss(); // Dismiss the dialog
                            markDialogAsShown(); // Mark the dialog as shown
                        }
                    })
                    .setCancelable(false) // Make the dialog non-cancelable
                    .create();

            alertDialog.show(); // Show the AlertDialog
        }

    }

    private boolean shouldShowDialog() {
        // Use SharedPreferences to check if the dialog should be shown
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return !sharedPreferences.getBoolean("dialog_shown", false);
    }

    private void markDialogAsShown() {
        // Use SharedPreferences to mark the dialog as shown
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("dialog_shown", true);
        editor.apply();
    }

}