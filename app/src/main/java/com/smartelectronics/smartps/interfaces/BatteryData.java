package com.smartelectronics.smartps.interfaces;

public interface BatteryData {

    public void onVoltageUpdate(String volt);
    public void onCurrentUpdate(String current);
    public void onPowerUpdate(String power);
}
