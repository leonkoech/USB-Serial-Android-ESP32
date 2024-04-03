package com.DefaultCompany.VRAware;
import static androidx.core.content.ContextCompat.getSystemService;
import static androidx.core.content.ContextCompat.registerReceiver;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.strictmode.UntaggedSocketViolation;
import android.util.Log;
import android.content.Context;
import android.content.pm.PackageManager;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class usbconnection {
    private static final String TAG = "USBConnection";
    private static final int TIMEOUT = 1000;

    private UsbManager usbManager;
    private Context context;

    private  UsbEndpoint endpointIn;
    private UsbInterface usbInterface;

    public void setContext(Context ctx) {
        context = ctx;
        usbManager = (context != null) ? (UsbManager) context.getSystemService(Context.USB_SERVICE) : null;
    }

    public boolean isContextSet() {
        return context != null;
    }

//    public boolean get(){return usbManager!=null; }

    public String testConnection() {
        return "output";
    }
    public String[] getConnectedDevices() {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        String[] connectedDevices = new String[deviceList.size()];
        int i = 0;
        for (UsbDevice device : deviceList.values()) {
            connectedDevices[i++] = device.getDeviceName();
        }
        return connectedDevices;
    }
    public UsbDevice[] getConnectedDevicesObject() {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        UsbDevice[] connectedDevices = new UsbDevice[deviceList.size()];
        int i = 0;
        for (UsbDevice device : deviceList.values()) {
            connectedDevices[i++] = device;
        }
        return connectedDevices;
    }
    public UsbDevice findUsbDeviceByName(String deviceName) {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

        for (UsbDevice device : deviceList.values()) {
            if (device.getDeviceName().equals(deviceName)) {
                return device;
            }
        }

        return null; // Device not found
    }
    public boolean sendData(byte[] data, UsbDevice usbDevice) {
        UsbDeviceConnection usbConnection = usbManager.openDevice(usbDevice);
        if (usbConnection == null) {
            Log.e(TAG, "Failed to open USB device connection");
            return false;
        }

        UsbInterface usbInterface = usbDevice.getInterface(0);
        if (!usbConnection.claimInterface(usbInterface, true)) {
            Log.e(TAG, "Failed to claim interface");
            usbConnection.close();
            return false;
        }

        UsbEndpoint endpointOut = null;
        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            UsbEndpoint endpoint = usbInterface.getEndpoint(i);
            if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK && endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                endpointOut = endpoint;
                break;
            }
        }

        if (endpointOut == null) {
            Log.e(TAG, "Could not find OUT endpoint");
            usbConnection.releaseInterface(usbInterface);
            usbConnection.close();
            return false;
        }

        int bytesSent = usbConnection.bulkTransfer(endpointOut, data, data.length, TIMEOUT);
        if (bytesSent >= 0) {
            Log.d(TAG, "Sent " + bytesSent + " bytes");
            return true;
        } else {
            Log.e(TAG, "Failed to write to USB device");
            usbConnection.releaseInterface(usbInterface);
            usbConnection.close();
            return false;
        }
    }

    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            // call method to set up device communication
//                            setupDeviceCommunication(device);
                        }
                    }
                    else {
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };

    // Function to register the BroadcastReceiver
    public void registerUSBReceiver(Context context) {
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(usbReceiver, filter);
    }

    // Function to unregister the BroadcastReceiver
    public void unregisterUSBReceiver(Context context) {
        context.unregisterReceiver(usbReceiver);
    }

    // Function to request USB permission
    public String requestUSBPermission(Context context, UsbDevice device) {
        PendingIntent permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        usbManager.requestPermission(device, permissionIntent);
        return "function called";
    }
    public String checkPermission(UsbDevice usbDevice){
        boolean checkPermission = usbManager.hasPermission(usbDevice);
        if(!checkPermission){
            return "permission absent" + "vendor ID: " + usbDevice.getVendorId() + "device ID" + usbDevice.getProductId();
        }
        return "Permission present Vendor ID:" + usbDevice.getVendorId() + "Device ID:" + usbDevice.getProductId();
    }
    public String receiveDataAsString(UsbDevice usbDevice) {
        UsbDeviceConnection usbConnection = usbManager.openDevice(usbDevice);
        checkPermission(usbDevice);
        if (usbConnection == null) {
            Log.e(TAG, "Failed to open USB device connection");
            return "Failed to open USB device connection";
        }

        UsbInterface usbInterface = usbDevice.getInterface(0);
        if (!usbConnection.claimInterface(usbInterface, true)) {
            Log.e(TAG, "Failed to claim interface");
            usbConnection.close();
            return "Failed to claim interface";
        }

        UsbEndpoint endpointIn = null;
        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            UsbEndpoint endpoint = usbInterface.getEndpoint(i);
            if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK && endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                endpointIn = endpoint;
                break;
            }
        }

        if (endpointIn == null) {
            Log.e(TAG, "Could not find IN endpoint");
            usbConnection.releaseInterface(usbInterface);
            usbConnection.close();
            return "Could not find IN endpoint";
        }

        byte[] buffer = new byte[12];
        int bytesRead = usbConnection.bulkTransfer(endpointIn, buffer, buffer.length, TIMEOUT);
        if (bytesRead >= 0) {
            Log.d(TAG, "Received " + bytesRead + " bytes");
            String receivedData = new String(buffer, 0, bytesRead, StandardCharsets.UTF);
            return receivedData;
        } else {
            Log.e(TAG, "Failed to read from USB device");
            usbConnection.releaseInterface(usbInterface);
            usbConnection.close();
            return "Failed to read from USB device";
        }
    }

    public UsbDeviceConnection establishConnection(UsbDevice usbDevice){
        UsbDeviceConnection usbConnection = usbManager.openDevice(usbDevice);
        if (usbConnection == null) {
            Log.e(TAG, "Failed to open USB device connection");
            return null;
        }

        usbInterface = usbDevice.getInterface(0);
        if (!usbConnection.claimInterface(usbInterface, true)) {
            Log.e(TAG, "Failed to claim interface");
            usbConnection.close();
            return null;
        }
//        UsbInterface intf = device.getInterface(0);
//        UsbEndpoint endpoint =
         endpointIn =  usbInterface.getEndpoint(0);;
//        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
//            UsbEndpoint endpoint = usbInterface.getEndpoint(i);
//            if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK && endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
//                endpointIn = endpoint;
//                break;
//            }
//        }

        if (endpointIn == null) {
            Log.e(TAG, "Could not find IN endpoint");
            usbConnection.releaseInterface(usbInterface);
            usbConnection.close();
            return null;
        }
        return usbConnection;
    }
    public String receiveData(UsbDeviceConnection usbConnection, int buffer_size) {
        byte[] buffer = new byte[buffer_size];
        if(usbConnection != null){
            int bytesRead = usbConnection.bulkTransfer(endpointIn, buffer, buffer.length, TIMEOUT);
            if (bytesRead > 0) {
                Log.d(TAG, "Received " + bytesRead + " bytes");
                String receivedData = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                return receivedData;
            } else if (bytesRead == 0) {
                // Transfer was successful, but no data was read
                Log.d(TAG, "Transfer successful, but no data read");
                return "No Data"; // Or handle this case according to your application's logic
            } else {
                // An error occurred during the transfer
                Log.e(TAG, "Failed to read from USB device");
                usbConnection.releaseInterface(usbInterface);
                usbConnection.close();
                return "null";
            }
        }
        else{
            return "isbconnection is null";
        }

    }
}
