package com.DefaultCompany.VRAware;
import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class usbconnection {
    private static final String TAG = "USBConnection";
    private static final int TIMEOUT = 1000;

    private UsbManager usbManager;
    private Context context;

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

    public String receiveDataAsString(UsbDevice usbDevice) {
        UsbDeviceConnection usbConnection = usbManager.openDevice(usbDevice);
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

        byte[] buffer = new byte[1024];
        int bytesRead = usbConnection.bulkTransfer(endpointIn, buffer, buffer.length, TIMEOUT);
        if (bytesRead >= 0) {
            Log.d(TAG, "Received " + bytesRead + " bytes");
            String receivedData = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
            return receivedData;
        } else {
            Log.e(TAG, "Failed to read from USB device");
            usbConnection.releaseInterface(usbInterface);
            usbConnection.close();
            return "Failed to read from USB device";
        }
    }

    public byte[] receiveData(UsbDevice usbDevice) {
        UsbDeviceConnection usbConnection = usbManager.openDevice(usbDevice);
        if (usbConnection == null) {
            Log.e(TAG, "Failed to open USB device connection");
            return null;
        }

        UsbInterface usbInterface = usbDevice.getInterface(0);
        if (!usbConnection.claimInterface(usbInterface, true)) {
            Log.e(TAG, "Failed to claim interface");
            usbConnection.close();
            return null;
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
            return null;
        }

        byte[] buffer = new byte[1024];
        int bytesRead = usbConnection.bulkTransfer(endpointIn, buffer, buffer.length, TIMEOUT);
        if (bytesRead >= 0) {
            Log.d(TAG, "Received " + bytesRead + " bytes");
            byte[] receivedData = new byte[bytesRead];
            System.arraycopy(buffer, 0, receivedData, 0, bytesRead);
            return receivedData;
        } else {
            Log.e(TAG, "Failed to read from USB device");
            usbConnection.releaseInterface(usbInterface);
            usbConnection.close();
            return null;
        }
    }
}
