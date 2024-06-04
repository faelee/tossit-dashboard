package com.hoho.android.usbserial.examples;

import android.app.AlertDialog;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Fragment that displays a list of USB devices
public class DevicesFragment extends ListFragment {

    // Adapter for holding devices found through scanning.
    static class ListItem {
        UsbDevice device;
        int port;
        UsbSerialDriver driver;

        ListItem(UsbDevice device, int port, UsbSerialDriver driver) {
            this.device = device;
            this.port = port;
            this.driver = driver;
        }
    }

    private final ArrayList<ListItem> listItems = new ArrayList<>();
    private ArrayAdapter<ListItem> listAdapter;
    private int baudRate = 19200;
    private boolean withIoManager = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        listAdapter = new ArrayAdapter<ListItem>(getActivity(), 0, listItems) {
            @NonNull
            @Override
            // Returns View that displays the data at the specified position in the data set
            public View getView(int position, View view, @NonNull ViewGroup parent) {
                ListItem item = listItems.get(position);
                // If the view is null, inflate the view
                if (view == null)
                    view = getActivity().getLayoutInflater().inflate(R.layout.device_list_item, parent, false);
                TextView text1 = view.findViewById(R.id.text1);
                TextView text2 = view.findViewById(R.id.text2);
                // If the driver is null, set the text to "<no driver>"
                // If the driver has only one port, set the text to the driver's class name
                // Otherwise, set the text to the driver's class name and the port number
                if(item.driver == null)
                    text1.setText("<no driver>");
                else if(item.driver.getPorts().size() == 1)
                    text1.setText(item.driver.getClass().getSimpleName().replace("SerialDriver",""));
                else
                    text1.setText(item.driver.getClass().getSimpleName().replace("SerialDriver","")+", Port "+item.port);
                // Set the text to the device's vendor and product IDs
                text2.setText(String.format(Locale.US, "Vendor %04X, Product %04X", item.device.getVendorId(), item.device.getProductId()));
                return view;
            }
        };
    }

    // After the fragment's activity has been created and view hierarchy has been instantiated, sets up fragment UI
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(null);
        View header = getActivity().getLayoutInflater().inflate(R.layout.device_list_header, null, false);
        getListView().addHeaderView(header, null, false);
        setEmptyText("<no USB devices found>");
        ((TextView) getListView().getEmptyView()).setTextSize(18);
        setListAdapter(listAdapter);
    }

    // Inflate the menu; this adds items to the action bar if it is present
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_devices, menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    // This method is called when the user selects an item from the options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // If the user selects the refresh option, call the refresh method
        if(id == R.id.refresh) {
            refresh();
            return true;
        } // If the user selects the baud rate option, display a dialog with the available baud rates
        else if (id ==R.id.baud_rate) {
            final String[] values = getResources().getStringArray(R.array.baud_rates);
            int pos = java.util.Arrays.asList(values).indexOf(String.valueOf(baudRate));
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Baud rate");
            builder.setSingleChoiceItems(values, pos, (dialog, which) -> {
                baudRate = Integer.parseInt(values[which]);
                dialog.dismiss();
            });
            builder.create().show();
            return true;
        } // If the user selects the read mode option, display a dialog with the available read modes
        else if (id ==R.id.read_mode) {
            final String[] values = getResources().getStringArray(R.array.read_modes);
            int pos = withIoManager ? 0 : 1; // read_modes[0]=event/io-manager, read_modes[1]=direct
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Read mode");
            builder.setSingleChoiceItems(values, pos, (dialog, which) -> {
                withIoManager = (which == 0);
                dialog.dismiss();
            });
            builder.create().show();
            return true;
        } // If the user selects an option that is not recognized, call the super class's onOptionsItemSelected method
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    // Refreshes the list of USB devices
    void refresh() {
        UsbManager usbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
        UsbSerialProber usbDefaultProber = UsbSerialProber.getDefaultProber();
        UsbSerialProber usbCustomProber = CustomProber.getCustomProber();
        listItems.clear();
        // For each USB device, probe the device with the default prober
        for(UsbDevice device : usbManager.getDeviceList().values()) {
            UsbSerialDriver driver = usbDefaultProber.probeDevice(device);
            // If the driver is null, probe the device with the custom prober
            if(driver == null) {
                driver = usbCustomProber.probeDevice(device);
                List<UsbSerialDriver> drivers = usbCustomProber.findAllDrivers(usbManager);
                if (drivers.size() > 0){
                    driver = drivers.get(0);
                }
            }
            // If the driver is not null, add a new item to listItems with the device, port, and driver
            if(driver != null) {
                for(int port = 0; port < driver.getPorts().size(); port++)
                    listItems.add(new ListItem(device, port, driver));
            } else {
                listItems.add(new ListItem(device, 0, null));
            }
        }
        // Notify the adapter that the data set has changed
        listAdapter.notifyDataSetChanged();
    }

    // Called when the user clicks on an item in the list
    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        ListItem item = listItems.get(position-1);
        if(item.driver == null) {
            Toast.makeText(getActivity(), "no driver", Toast.LENGTH_SHORT).show();
        } // If the driver is not null, replace the current fragment with a new TerminalFragment with item information
        else {
            Bundle args = new Bundle();
            args.putInt("device", item.device.getDeviceId());
            args.putInt("port", item.port);
            args.putInt("baud", baudRate);
            args.putBoolean("withIoManager", withIoManager);
            Fragment fragment = new TerminalFragment();
            fragment.setArguments(args);
            getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, "terminal").addToBackStack(null).commit();
        }
    }

}
