package com.hoho.android.usbserial.examples;

import static android.app.PendingIntent.getActivity;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.hardware.usb.UsbManager;

import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.FtdiSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.util.List;

/**
 * add devices here, that are not known to DefaultProber
 *
 * if the App should auto start for these devices, also
 * add IDs to app/src/main/res/xml/device_filter.xml
 *
 * CustomProber stores the custom devices and their drivers
 */
class CustomProber {

    static UsbSerialProber getCustomProber() {
        ProbeTable customTable = new ProbeTable();
        //each product is associated with a driver class
        customTable.addProduct(557,2008, CdcAcmSerialDriver.class);
        customTable.addProduct(11914,5, CdcAcmSerialDriver.class);
        customTable.addProduct(11914,10, CdcAcmSerialDriver.class);
        customTable.addProduct(0x1234, 0x0001, FtdiSerialDriver.class); // e.g. device with custom VID+PID
        customTable.addProduct(0x1234, 0x0002, FtdiSerialDriver.class); // e.g. device with custom VID+PID
        return new UsbSerialProber(customTable);
    }

}
