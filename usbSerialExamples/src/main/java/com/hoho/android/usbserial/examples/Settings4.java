package com.hoho.android.usbserial.examples;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;

// Sample settings page for each device widget (not yet set to be a fragment so it can be
// individualized to each device widget, currently connected to all widgets, so
// fragment has to be implemented)
public class Settings4 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings4);

        // register all the features with their appropriate IDs
        TextView title = findViewById(R.id.textView);
        ImageButton back = findViewById(R.id.backB);
        Button delete = findViewById(R.id.delete);


        // Set the OnClickListener of the back button to go back to the Main Activity page
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(Settings4.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
        });

        // Set the title of the page to the device ID passed from the Main Activity page
        if (this.getIntent().getStringExtra("TITLE") != null) {
            title.setText(this.getIntent().getStringExtra("TITLE"));
        } else {
            title.setText("DEVICE ID");
        }

        // Get the device view ID passed from the Main Activity page
        int id = this.getIntent().getIntExtra("ID", -1);

        // Set the OnClickListener of the delete button to delete the device from the Main Activity page
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent(Settings4.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("Delete",true);
                // Pass the device ID to the Main Activity page to delete the device
                i.putExtra("ID",id);
                startActivity(i);
                finish();
            }
        });

    }

    // Restores the state of the page when the user returns
    @Override
    protected void onResume() {
        super.onResume();
        // Fetching the stored data from the SharedPreference
        SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);

        SwitchMaterial active = findViewById(R.id.activeswitch);
        SwitchMaterial light = findViewById(R.id.lightswitch);

        if(sh.getBoolean("active", false)) { // If the active switch is on
            active.setChecked(true);
        } else {
            active.setChecked(false);
        }

        if(sh.getBoolean("light", false)) { // If the light switch is on
            light.setChecked(true);
        } else {
            light.setChecked(false);
        }

    }

    // Store the data in the SharedPreference in the onPause() method
    // When the user closes the application onPause() will be called and data will be stored
    @Override
    protected void onPause() {
        super.onPause();
        // Creating a shared pref object in private mode to store data
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        // Write all the data entered by the user in SharedPreference and apply
        // Store the switch states in the SharedPreference
        SwitchMaterial active = findViewById(R.id.activeswitch);
        myEdit.putBoolean("active", active.isChecked());

        SwitchMaterial light = findViewById(R.id.lightswitch);
        myEdit.putBoolean("light", light.isChecked());

        myEdit.apply();
    }
}