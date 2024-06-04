package com.hoho.android.usbserial.examples;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class USBHomePage extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        // Set up the action bar
        setSupportActionBar(toolbar);
        // Listen for changes in the back stack
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        // Inflate devices fragment if savedInstanceState is null
        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction().add(R.id.fragment, new DevicesFragment(), "devices").commit();
        // Else savedInstanceState is not null, so the activity is being recreated (e.g., after a rotation)
        // Call onBackStackChanged to handle what needs to be done when the back stack changes
        else
            onBackStackChanged();

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//                Intent intent = new Intent(MainActivity_1.this, MainActivity.class);
//                startActivity(intent);
//            }
//        });
    }

    // Handle the back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // If the back button is pressed, go back to the main activity
        if(id == android.R.id.home){
            Intent i= new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Handle the back stack
    @Override
    public void onBackStackChanged() {
        // Show the back button if the back stack has entries
        getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount()>0);
    }

    // Handle the back button
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // If a USB device is detected, inflate the terminal fragment
    @Override
    protected void onNewIntent(Intent intent) {
        if("android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(intent.getAction())) {
            TerminalFragment terminal = (TerminalFragment)getSupportFragmentManager().findFragmentByTag("terminal");
            if (terminal != null)
                terminal.status("USB device detected");
        }
        super.onNewIntent(intent);
    }

}
