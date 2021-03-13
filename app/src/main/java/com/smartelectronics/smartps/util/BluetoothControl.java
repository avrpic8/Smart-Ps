package com.smartelectronics.smartps.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import java.util.Set;

/**
 * Created by SAEED on 1/13/2019.
 */

public class BluetoothControl {

    public static final int ENABLE_BT_INTENT = 1;
    private boolean avalable= false;

    private BluetoothAdapter btAdapter;
    private Activity mContext;
    private Set<BluetoothDevice> paredDevices;

    public BluetoothControl(Activity context){

        this.mContext = context;
        this.avalable = getAdapter();
    }

    private boolean getAdapter(){

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (btAdapter == null)
            return false;
        else
            return true;
    }

    public boolean isAvalable(){
        return this.avalable;
    }
    public boolean isEnabled(){
        return this.btAdapter.isEnabled();
    }

    public boolean isTurningOn(){

        if(this.btAdapter.getState() != BluetoothAdapter.STATE_OFF)
            return true;
        else return false;
    }
    public void turnOffBt(){

        if(this.avalable)
            this.btAdapter.disable();
    }

    public void cancelDiscovery(){
        this.btAdapter.cancelDiscovery();
    }
    public Set<BluetoothDevice> getParedDevices(){

        this.paredDevices = this.btAdapter.getBondedDevices();
        return this.paredDevices;
    }
    public BluetoothDevice getDeviceByMac(String mac){

        for(BluetoothDevice dev: getParedDevices()){
            if(dev.getAddress().equals(mac))
                return dev;
        }

        return null;
    }

    public void requestBluetoothActivity(){
        this.mContext.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), ENABLE_BT_INTENT);
    }

}
