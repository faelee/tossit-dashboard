package com.hoho.android.usbserial.examples;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import static java.lang.System.in;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.ArrayList;

//
@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivity extends AppCompatActivity { //implements FragmentManager.OnBackStackChangedListener
    private static final int READ_WAIT_MILLIS = 2000;
    // ArrayList that stores all the inflated device widgets
    private ArrayList<View> inflated = new ArrayList<View>();
//    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if(intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION) ||
//                    intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION) ||
//                    intent.getAction().equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION) ||
//                    intent.getAction().equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION) ||
//                    intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
//                setWifiIndicator();
//            }
//        }
//    };

    // Set the Device ID tag to the WiFi SSID of each device widget
    private void setWifiIndicator(View view) {
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        //getApplicationContext solves memory leak issues prior to Android N (must use Application Context to get wifi system service.
        WifiManager wifiMgr = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        TextView wifiSSID = (TextView)view.findViewById(R.id.image_view20ci);

        try {
            if (wifiMgr != null) {
                if (wifiMgr.isWifiEnabled()) {
                    //wifi is enabled.  toggle on wifi indicator.
                    String ssid = wifiMgr.getConnectionInfo().getSSID();
                    if (ssid != null) {
                        //Log.v(this.getClass().getSimpleName(), "SSID: " + ssid + "  Supplicant State: " + info.getSupplicantState());
                        wifiSSID.setText(ssid.substring(1, ssid.length() - 1));
                    }
                } else {
                    //wifi is disabled.  toggle off wifi indicator.
                    wifiSSID.setText("");
                }
            }
        } catch(Exception e) {
            //catching anything thrown in this block just so it doesn't crash the program unnecessarily
            e.printStackTrace();
        }
    }

    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher, as an instance variable.
//    private ActivityResultLauncher<String> requestPermissionLauncher =
//            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
//                if (isGranted) {
//                    Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_LONG).show(); //setWifiIndicator();
//                } else {
//                    Toast.makeText(MainActivity.this, "Empty", Toast.LENGTH_LONG).show();
//                }
//            });

    // Launch permission requests based on user-given permissions
    ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = result.getOrDefault(
                                ACCESS_FINE_LOCATION, false);
                        Boolean coarseLocationGranted = result.getOrDefault(
                                ACCESS_COARSE_LOCATION,false);
                        if (fineLocationGranted != null && fineLocationGranted) {
                            // Precise location access granted.
                            Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_LONG).show();
                        } else if (coarseLocationGranted != null && coarseLocationGranted) {
                            // Only approximate location access granted.
                            Toast.makeText(MainActivity.this, "NEED FINE", Toast.LENGTH_LONG).show();
                        } else {
                            // No location access granted.
                            Toast.makeText(MainActivity.this, "Empty", Toast.LENGTH_LONG).show();
                        }
                    }
            );

    // Set the card view for each device widget
    private void setCard(View view){
        ImageButton setting = view.findViewById(R.id.settingsci);
        Button status = view.findViewById(R.id.status);
        ImageButton sound = view.findViewById(R.id.sound);
        ImageButton light = view.findViewById(R.id.light);
        TextView display = view.findViewById(R.id.textView20ci);
        ImageButton plot = view.findViewById(R.id.plotsci);
        ImageButton terminal = view.findViewById(R.id.expandsci);
        TextView wifiSSID = view.findViewById(R.id.image_view20ci);
        // Data stores WiFi connection status for switching between Serial and WiFi modes
        TextView data = view.findViewById(R.id.data);
        int id = View.generateViewId();
        view.setId(id);

        // Set the onClickListener for the settings button to go to the Settings page for the device
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create an intent to switch to second activity upon clicking
                Intent intent = new Intent(MainActivity.this, Settings4.class);
                intent.putExtra("TITLE",wifiSSID.getText());
                intent.putExtra("ID",id);
                startActivity(intent);
            }
        });

        // Set the onClickListener for the plot button to go to the Plot site for the device
        plot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create an intent to switch to second activity upon clicking
                Intent browserX = new Intent(Intent.ACTION_VIEW, Uri.parse("http://10.42.0.1:5000"));
                startActivity(browserX);
            }
        });

        // Set the onClickListener for the status button to toggle the WiFi connection status of the device
        status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Switch status to "Disconnected" if it is "Connected" and vice versa
                if (status.getText().equals("CONNECTED")) {
                    status.setText(getString(R.string.disconnected));
                    status.setTextColor(Color.parseColor("#990000"));
                    status.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    // Display "INACTIVE" in grey if the device is disconnected
                    display.setText(getString(R.string.inactive));
                    display.setTextColor(Color.parseColor("#636363"));
                    data.setText(getString(R.string.disconnected));
                }
                else{
                    // Switch all other device statuses to "Disconnected" if this device is connected
                    for (View var : inflated)
                    {
                        Button stat = var.findViewById(R.id.status);
                        TextView disp = var.findViewById(R.id.textView20ci);
                        TextView data1 = var.findViewById(R.id.data);
                        stat.setText(getString(R.string.disconnected));
                        stat.setTextColor(Color.parseColor("#990000"));
                        stat.setBackgroundColor(Color.parseColor("#d3d3d3"));
                        disp.setText(getString(R.string.inactive));
                        disp.setTextColor(Color.parseColor("#636363"));
                        data1.setText(getString(R.string.disconnected));

                    }
                    status.setText(getString(R.string.connected));
                    display.setText(getString(R.string.clear));
                    display.setTextColor(Color.parseColor("#0f9d58"));
                    status.setTextColor(Color.parseColor("#0f9d58"));
                    status.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    data.setText(getString(R.string.connected));
                    // Set device ID to the SSID of the connected WiFi network
                    setWifiIndicator(view);
                }
            }
        });

        // Set the onClickListener for the sound button to toggle the sound status of the device
        sound.setOnClickListener(new View.OnClickListener() {
            int wificlick = 1;
            @Override
            public void onClick(View v) {
                wificlick += 1;
                if (wificlick % 2 == 1) {
                    sound.setImageResource(R.drawable.speaker);
                }
                else{
                    sound.setImageResource(R.drawable.mute);
                }
            }
        });

        // Set the onClickListener for the light button to toggle the light status of the device
        light.setOnClickListener(new View.OnClickListener() {
            int wificlick = 1;
            @Override
            public void onClick(View v) {
                wificlick += 1;
                if (wificlick % 2 == 1) {
                    light.setImageResource(R.drawable.lighton);
                }
                else{
                    light.setImageResource(R.drawable.lightoff);
                }
            }
        });

        // Set the onClickListener for the terminal button to go to the device's Terminal fragment
        terminal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create an intent to switch to second activity upon clicking
                Intent intent = new Intent(MainActivity.this, USBHomePage.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        IntentFilter intentFilter = new IntentFilter();
//        //intentFilter.addAction(ScannerService.ACTION_READ_SCANNER);
//        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
//        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
//        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
//        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
//        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
//        //registerReceiver(mReceiver, intentFilter);

        SwitchMaterial switch1 = findViewById(R.id.switch1);
        ImageButton wifiset = findViewById(R.id.wifiset);
        Button wificon = findViewById(R.id.wificon);
        ImageButton serset = findViewById(R.id.serset);
        Button sercon = findViewById(R.id.sercon);
        sercon.setBackgroundColor(Color.parseColor("#990000"));
        // Layout to inflate the views for each widget
        LinearLayout mainLayout1 = findViewById(R.id.inflater1);

        // Set the onClickListener for the WiFi Connect button to inflate a new device widget
        wificon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // If the app has the ACCESS_FINE_LOCATION permission, inflate a new device widget
                if (ContextCompat.checkSelfPermission(
                        MainActivity.this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    View myLayout1 = getLayoutInflater().inflate(R.layout.my_layout, mainLayout1, false);
                    mainLayout1.addView(myLayout1, 0); //new ViewGroup.LayoutParams(
                    //ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    inflated.add(myLayout1);
                    setCard(myLayout1);
                } else {
                    // Ask for the ACCESS_FINE_LOCATION and the ACCESS_COARSE_LOCATION permission
                    requestPermissionLauncher.launch(new String[] {
                            ACCESS_FINE_LOCATION,
                            ACCESS_COARSE_LOCATION});
                }
            }
        });

        // Set the onClickListener for the WiFi Settings button to pull up the device WiFi settings panel
        wifiset.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY);
                startActivity(intent);
            }
        });

        // Set the onClickListener for the Serial Connect button to disconnect/connect all device widgets
        sercon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If the Serial Connect button is set to connect, connect all device widgets
                if (sercon.getText().toString().equals("Serial Connect")) {
                    sercon.setText(R.string.serial_disconnect);
                    sercon.setBackgroundColor(Color.parseColor("#006d2d"));
//                    status.setText("CONNECTED");
//                    status.setTextColor(Color.parseColor("#0f9d58"));
//                    status.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    for (View var : inflated)
                    {
                        Button stat = var.findViewById(R.id.status);
                        TextView disp = var.findViewById(R.id.textView20ci);
                        stat.setText(getString(R.string.connected));
                        stat.setTextColor(Color.parseColor("#0f9d58"));
                        stat.setBackgroundColor(Color.parseColor("#d3d3d3"));
                        disp.setText(getString(R.string.clear));
                        disp.setTextColor(Color.parseColor("#0f9d58"));

                    }
                } // If the Serial Connect button is set to disconnect, disconnect all device widgets
                else{
                    sercon.setText(R.string.serial_connect);
                    sercon.setBackgroundColor(Color.parseColor("#990000"));
//                    status.setText("DISCONNECTED");
//                    status.setTextColor(Color.parseColor("#990000"));
//                    status.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    for (View var : inflated)
                    {
                        Button stat = var.findViewById(R.id.status);
                        TextView disp = var.findViewById(R.id.textView20ci);
                        stat.setText(getString(R.string.disconnected));
                        stat.setTextColor(Color.parseColor("#990000"));
                        stat.setBackgroundColor(Color.parseColor("#d3d3d3"));
                        disp.setText(getString(R.string.inactive));
                        disp.setTextColor(Color.parseColor("#636363"));
                    }
                }
            }
        });

        // Switch between WiFi and Serial modes by listening for switch's checked/unchecked state changes
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            // Responds to switch being checked/unchecked
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // If the switch is checked, switch to serial mode
                if (isChecked) {
                    // Disable WiFi button and reset Serial connection status for all device widgets
                    wifiset.setClickable(false);
                    wifiset.setAlpha(0.25F);
                    wificon.setAlpha(0.55F);
                    wificon.setClickable(false);
                    wificon.setBackgroundColor(Color.parseColor("#AEAEAE"));
                    serset.setClickable(true);
                    serset.setAlpha(0.55F);
                    sercon.setClickable(true);
                    sercon.setAlpha(1);
                    sercon.setText(getString(R.string.serial_connect));
                    sercon.setBackgroundColor(Color.parseColor("#990000"));
                    // Set all device widgets to "DISCONNECTED" status if the switch has
                    // just been toggled to serial mode
                    for(View var: inflated){
                        Button status = var.findViewById(R.id.status);
                        status.setClickable(false);
                        TextView disp = var.findViewById(R.id.textView20ci);
                        status.setClickable(false);
                        status.setText(getString(R.string.disconnected));
                        status.setTextColor(Color.parseColor("#990000"));
                        status.setBackgroundColor(Color.parseColor("#d3d3d3"));
                        disp.setText(getString(R.string.inactive));
                        disp.setTextColor(Color.parseColor("#636363"));
                    }
                } // If the switch is unchecked, switch to WiFi mode and reset connection status
                // for all device widgets to how it was before switching to serial mode
                else{
                    serset.setClickable(false);
                    serset.setAlpha(0.25F);
                    sercon.setClickable(false);
                    wificon.setBackgroundColor(Color.parseColor("#636363"));
                    wifiset.setClickable(true);
                    wifiset.setAlpha(0.55F);
                    wificon.setAlpha(1);
                    wificon.setClickable(true);
                    sercon.setText(getString(R.string.serial_connect));
                    sercon.setBackgroundColor(Color.parseColor("#990000"));
                    sercon.setAlpha(0.55F);
                    // Set all device widgets to their previous connection status for WiFi mode
                    for(View var: inflated){
                        Button stat = var.findViewById(R.id.status);
                        TextView data = var.findViewById(R.id.data);
                        stat.setClickable(true);
                        TextView disp = var.findViewById(R.id.textView20ci);
                        stat.setBackgroundColor(Color.parseColor("#d3d3d3"));
                        // TextView data stores the WiFi connection status for each device widget
                        if(data.getText().equals(getString(R.string.connected))){
                            stat.setText(getString(R.string.connected));
                            stat.setTextColor(Color.parseColor("#0f9d58"));
                            disp.setText(getString(R.string.clear));
                            disp.setTextColor(Color.parseColor("#0f9d58"));
                        }else{
                            stat.setText(getString(R.string.disconnected));
                            stat.setTextColor(Color.parseColor("#990000"));
                            disp.setText(getString(R.string.inactive));
                            disp.setTextColor(Color.parseColor("#636363"));
                        }
                    }
//                    boolean all = true;
//                    String ogstring = "";
//                    if(inflated.size() > 0) {
//                        TextView og = inflated.get(0).findViewById(R.id.status);
//                        ogstring = og.getText().toString();
//                    }
//                    for(View var: inflated){
//                        Button status = var.findViewById(R.id.status);
//                        status.setClickable(true);
//                        if (!status.getText().equals(ogstring)){
//                            all = false;    // if not all the statuses are the same, then all is false
//                        }
//                    }
//                    //Toast.makeText(MainActivity.this, String.valueOf(all), Toast.LENGTH_LONG).show();
//                    if(all){
//                        for(View var: inflated){
//                            Button status = var.findViewById(R.id.status);
//                            TextView disp = var.findViewById(R.id.textView20ci);
//                            status.setText("DISCONNECTED");
//                            status.setTextColor(Color.parseColor("#990000"));
//                            status.setBackgroundColor(Color.parseColor("#d3d3d3"));
//                            disp.setText("INACTIVE");
//                            disp.setTextColor(Color.parseColor("#636363"));}
//                    }
//                    if (ContextCompat.checkSelfPermission(
//                            MainActivity.this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//
//                    } else {
//                        // You can directly ask for the permission.
//                        // The registered ActivityResultCallback gets the result of this request.
//                        requestPermissionLauncher.launch(new String[] {
//                                ACCESS_FINE_LOCATION,
//                                ACCESS_COARSE_LOCATION});
//                    }
                }
            }
        });


        // Set the initial state to be in serial mode
//        switch1.setChecked(true);
//        wifiset.setClickable(false);
//        wificon.setClickable(false);
//        serset.setClickable(true);
//        sercon.setClickable(true);
//        wificon.setAlpha(0.55F);
//        status.setClickable(false);
       // wificon.setBackgroundColor(Color.parseColor("#AEAEAE"));

//        // Find all available drivers from attached devices.
//        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
//        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
//        if (availableDrivers.isEmpty()) {
//            // Toast.makeText(MainActivity.this, "Empty", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        // Open a connection to the first available driver.
//        UsbSerialDriver driver = availableDrivers.get(0);
//        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
//        if (connection == null) {
//            // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
//            // Toast.makeText(MainActivity.this, "Null", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        UsbSerialPort port = driver.getPorts().get(0); // Most devices have just one port (port 0)
//        try{
//            port.open(connection);
//            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
//            byte[] buffer = new byte[8192];
//            String len = String.valueOf(port.read(buffer, READ_WAIT_MILLIS));
//            Toast.makeText(MainActivity.this, len, Toast.LENGTH_LONG).show();
//        } catch (IOException e){
//            Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_LONG).show();
//        }
    }


    // Restores the state of the page when the user returns
    @Override
    protected void onResume() {
        super.onResume();
        // Fetching the stored data from the SharedPreference
        SharedPreferences sh = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        LinearLayout mainLayout1 = findViewById(R.id.inflater1);
        SwitchMaterial switch1 = findViewById(R.id.switch1);
        ImageButton wifiset = findViewById(R.id.wifiset);
        Button wificon = findViewById(R.id.wificon);
        ImageButton serset = findViewById(R.id.serset);
        Button sercon = findViewById(R.id.sercon);
        boolean switch1bool = sh.getBoolean("switch1", false);
        boolean serconbool = sh.getBoolean("sercon", false);
        ArrayList<String> stati = new ArrayList<String>();

        int mViewsCount = sh.getInt("mViewsCount", 0);;
//        Toast.makeText(MainActivity.this, "new"+mViewsCount, Toast.LENGTH_SHORT).show();
        int id = this.getIntent().getIntExtra("ID", -1);
        // Check if the user is deleting a device widget from intent extras
        boolean deletebool = this.getIntent().getBooleanExtra("Delete", false);

        // If the device widgets are not already displayed, re-inflate them
        if(inflated.size() < mViewsCount) {
            for (int i = 1; i <= mViewsCount; i++) {
                // Inflate the device widget if it is not designated to be deleted
                if(!(deletebool && sh.getInt("ID"+i,-2) == id)) {
                    View myLayout1 = getLayoutInflater().inflate(R.layout.my_layout, mainLayout1, false);
                    mainLayout1.addView(myLayout1, 0);
                    inflated.add(myLayout1);
                    setCard(myLayout1);
                    Button stat = myLayout1.findViewById(R.id.status);
                    TextView wifiSSID = myLayout1.findViewById(R.id.image_view20ci);
                    TextView disp = myLayout1.findViewById(R.id.textView20ci);
                    TextView data = myLayout1.findViewById(R.id.data);
//                    Toast.makeText(MainActivity.this, "wifi"+i, Toast.LENGTH_SHORT).show();
                    wifiSSID.setText(sh.getString("wifi" + i, ""));
 //                   Toast.makeText(MainActivity.this, ""+i, Toast.LENGTH_SHORT).show();

                    // ArrayList stati stores whether each device is connected in WiFi mode or not
                    if (sh.getString("" + i, "").equals("CONNECTED")) {
                        stati.add(getString(R.string.connected));
                    } else {
                        stati.add(getString(R.string.disconnected));
                    }

                    // Store the previous connection status for WiFi mode for each device widget
                    if (sh.getString("data" + i, "").equals("CONNECTED")) {
                        data.setText(getString(R.string.connected));
                    } else {
                        data.setText(getString(R.string.disconnected));
                    }
                }
            }
        }
        else{ // If the device widgets are already displayed, update what each widget displays
            for (int i = 1; i <= mViewsCount; i++) {
                // Update the device widget if it is not designated to be deleted
                if(!(deletebool && sh.getInt("ID"+i,-2) == id)) {
                    View myLayout1 = inflated.get(i-1);
                    Button stat = myLayout1.findViewById(R.id.status);
                    TextView wifiSSID = myLayout1.findViewById(R.id.image_view20ci);
                    TextView disp = myLayout1.findViewById(R.id.textView20ci);
                    TextView data = myLayout1.findViewById(R.id.data);
//                    Toast.makeText(MainActivity.this, "wifi"+i, Toast.LENGTH_SHORT).show();
                    wifiSSID.setText(sh.getString("wifi" + i, ""));
                    //                   Toast.makeText(MainActivity.this, ""+i, Toast.LENGTH_SHORT).show();

                    // ArrayList stati stores whether each device is connected in WiFi mode or not
                    if (sh.getString("" + i, "").equals("CONNECTED")) {
                        stati.add(getString(R.string.connected));
                    } else {
                        stati.add(getString(R.string.disconnected));
                    }

                    // Store the previous connection status for WiFi mode for each device widget
                    if (sh.getString("data" + i, "").equals("CONNECTED")) {
                        data.setText(getString(R.string.connected));
                    } else {
                        data.setText(getString(R.string.disconnected));
                    }
                }
            }
        }

        // Set the serial mode to be connected or disconnected based on stored data
        if(serconbool){
            sercon.setText(getString(R.string.serial_disconnect));
            sercon.setBackgroundColor(Color.parseColor("#006d2d"));
            for(int i = 1; i <= inflated.size(); i++){
                View var = inflated.get(i-1);
                Button status = var.findViewById(R.id.status);
                TextView disp = var.findViewById(R.id.textView20ci);
                status.setText(getString(R.string.connected));
                status.setTextColor(Color.parseColor("#0f9d58"));
                status.setBackgroundColor(Color.parseColor("#d3d3d3"));
                disp.setText(getString(R.string.clear));
                disp.setTextColor(Color.parseColor("#0f9d58"));
            }
        }
        else{
            sercon.setText(getString(R.string.serial_connect));
            sercon.setBackgroundColor(Color.parseColor("#990000"));
            for(int i = 1; i <= inflated.size(); i++){
                View var = inflated.get(i-1);
                Button status = var.findViewById(R.id.status);
                TextView disp = var.findViewById(R.id.textView20ci);
                status.setText(getString(R.string.disconnected));
                status.setTextColor(Color.parseColor("#990000"));
                status.setBackgroundColor(Color.parseColor("#d3d3d3"));
                disp.setText(getString(R.string.inactive));
                disp.setTextColor(Color.parseColor("#636363"));
            }
        }

        // Set the mode to be serial or WiFi based on stored data
        if(switch1bool){ // Serial mode
            wifiset.setClickable(false);
            wifiset.setAlpha(0.25F);
            wificon.setClickable(false);
            wificon.setAlpha(0.55F);
            wificon.setBackgroundColor(Color.parseColor("#AEAEAE"));
            serset.setClickable(true);
            serset.setAlpha(0.55F);
            sercon.setClickable(true);
            switch1.setChecked(true);
            for(int i = 1; i <= inflated.size(); i++){
                View var = inflated.get(i-1);
                Button status = var.findViewById(R.id.status);
                status.setClickable(false);
            }
        }
        else { // WiFi mode
            serset.setClickable(false);
            serset.setAlpha(0.25F);
            sercon.setClickable(false);
            wificon.setBackgroundColor(Color.parseColor("#636363"));
            sercon.setAlpha(0.55F);
            wifiset.setClickable(true);
            wifiset.setAlpha(0.55F);
            wificon.setClickable(true);
            switch1.setChecked(false);
            for(int i = 1; i <= inflated.size(); i++){
                View var = inflated.get(i-1);
                Button status = var.findViewById(R.id.status);
                TextView data = var.findViewById(R.id.data);
                status.setClickable(true);
                // Set the device widgets to their previous connection status for WiFi mode
                if(stati.size() > 0) {
                    if (stati.get(i - 1).equals("CONNECTED")) {
                        status.setText(getString(R.string.connected));
                        status.setTextColor(Color.parseColor("#0f9d58"));
                        status.setBackgroundColor(Color.parseColor("#d3d3d3"));
                        data.setText(getString(R.string.connected));
                    } else {
                        status.setText(getString(R.string.disconnected));
                        status.setTextColor(Color.parseColor("#990000"));
                        status.setBackgroundColor(Color.parseColor("#d3d3d3"));
                        data.setText(getString(R.string.disconnected));
                    }
                }
            }
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
        int mViewsCount = 0;
        for(View view : inflated)
        {
            mViewsCount++;
            Button stat = view.findViewById(R.id.status);
            TextView data = view.findViewById(R.id.data);
            // Store order in inflated, WiFi SSID, device ID (to know if device has same ID as
            // one to be deleted), and WiFi connection status for each device widget
            myEdit.putString(""+mViewsCount, stat.getText().toString());
            TextView wifiSSID = view.findViewById(R.id.image_view20ci);
            myEdit.putString("wifi"+mViewsCount, wifiSSID.getText().toString());
            myEdit.putInt("ID"+mViewsCount, view.getId());
            myEdit.putString("data"+mViewsCount, data.getText().toString());
        }

        // Store the number of device widgets in the SharedPreference
        myEdit.putInt("mViewsCount", mViewsCount);
        //Toast.makeText(MainActivity.this, ""+mViewsCount, Toast.LENGTH_SHORT).show();

        // Store the switch state in the SharedPreference
        SwitchMaterial switch1 = findViewById(R.id.switch1);
        myEdit.putBoolean("switch1", switch1.isChecked());

        // Store the serial connection status in the SharedPreference
        Button sercon = findViewById(R.id.sercon);
        if(sercon.getText().toString().equals("Serial Disconnect")){
            myEdit.putBoolean("sercon", true);
        }
        else{
            myEdit.putBoolean("sercon", false);
        }

        myEdit.apply();
    }

}