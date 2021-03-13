package com.smartelectronics.smartps.util;

import java.util.Locale;

public class ProcessIncomingData {

    private static final float REFERENCE_VOLTAGE = 3.3f;
    private static final int   MAX_DIGITAL_OUTPUT = 1023;

    private String voltage;
    private String current;
    private String stateOfCharge;
    private String power;
    private String temperature;
    private String timeHour;
    private String timeMinute;
    private String polarity;
    private String batteryType;
    private String batteryCell;
    private String batteryCurrent;
    private String chargingType;
    public boolean readyData = false;


    public void processData(String message) {

        float temp_v, temp_i, temp_t, temp_p;

        try {

            voltage        = message.substring(0, 3);
            current        = message.substring(3, 6);
            stateOfCharge  = message.substring(6, 9);
            temperature    = message.substring(9, 12);
            timeHour       = message.substring(12, 14);
            timeMinute     = message.substring(14, 16);
            polarity       = message.substring(16, 17);
            batteryType    = message.substring(17, 18);
            batteryCell    = message.substring(18, 20);
            batteryCurrent = message.substring(20, 23);
            chargingType   = message.substring(23, 24);

            //temp_v = ( Float.parseFloat(voltage) * REFERENCE_VOLTAGE * 48)/ MAX_DIGITAL_OUTPUT;
            temp_v = (float)( Integer.parseInt(voltage) / 6.2f);
            voltage = String.format(Locale.ENGLISH,"%02.1f", temp_v);

            //temp_i = ( Float.parseFloat(current) * REFERENCE_VOLTAGE)/ ( MAX_DIGITAL_OUTPUT * 0.1f);
            temp_i = (float) (Integer.parseInt(current) / 30.4);
            current = String.format(Locale.ENGLISH,"%02.1f", temp_i);

            temp_t = ( Float.parseFloat(temperature) * REFERENCE_VOLTAGE * 100)/ MAX_DIGITAL_OUTPUT;
            temperature = String.format(Locale.ENGLISH,"%.0f", temp_t);

            temp_p = Float.parseFloat(voltage) * Float.parseFloat(current);
            power = String.format(Locale.ENGLISH,"%03.0f", temp_p);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getVoltage() {
        return  voltage;
    }
    public String getCurrent() {
        return current;
    }
    public String getStateOfCharge() {
        return stateOfCharge;
    }
    public String getTemperature() {
        return temperature;
    }
    public String getTime() {
        String time = timeHour + ":" + timeMinute;
        return time;
    }
    public String getPolarity() {
        return polarity;
    }
    public String getBatteryType() {
        return batteryType;
    }
    public String getBatteryCell() {
        return batteryCell;
    }
    public String getBatteryCurrent() {
        return batteryCurrent;
    }
    public String getChargingType() {
        return chargingType;
    }
    public String getPower(){
        return power;
    }




}