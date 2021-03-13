package com.smartelectronics.smartps.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Slide;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.smartelectronics.smartps.R;
import com.smartelectronics.smartps.util.BluetoothControl;
import com.smartelectronics.smartps.util.BluetoothSPP;
import com.smartelectronics.smartps.util.BluetoothState;
import com.smartelectronics.smartps.util.MyAlertDialog;
import com.smartelectronics.smartps.util.ProcessIncomingData;

import java.util.ArrayList;
import java.util.Locale;

import eo.view.batterymeter.BatteryMeter;

public class MainActivity extends AppCompatActivity implements BluetoothSPP.BluetoothConnectionListener {

    private Toolbar toolbar;
    private TextView txtBtStatus, txtBatVoltage, txtBatCurrent, txtBatPower, txtBatTemp, txtTime;
    private Button btnStart, btnStop;
    private CardView voltageLayout, currentLayout, powerLayout, tempLayout, settingPanel, btnControlPanel;
    private RadioGroup btTypes, chrMethods;
    private Spinner btCellInfos;
    private EditText edtBatteryCurrent;

    private Animation anim;

    private BatteryMeter batteryMeter;

    // spinner lists
    private ArrayAdapter spinnerAdapter;
    private String[] slaType, ionType, nmType, ncType;

    // bluetooth objects
    private ProcessIncomingData incomingData;
    private BluetoothSPP bluetoothSPP;
    private BluetoothControl btControl;


    // battery parameter for send to SmartPs
    private String batType = "2", batCells, batCurrent, chargeType = "N", startCharge;
    private int tempCurrent;

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        turnOffBtModule();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the toolbar_menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id){

            case R.id.exit:

                MyAlertDialog alertDialog = new MyAlertDialog(this);
                alertDialog.btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        finish();
                    }
                });
                alertDialog.setCancelable(false);
                alertDialog.show();
                return true;

            case R.id.bt_list:

                if(bluetoothSPP.isServiceAvailable() && btControl.isEnabled()) {
                    final ArrayList<String> arr_filter_address = new ArrayList<String>();
                    final ArrayList<String> arr_filter_name = new ArrayList<String>();
                    String[] arr_name = bluetoothSPP.getPairedDeviceName();
                    String[] arr_address = bluetoothSPP.getPairedDeviceAddress();
                    for (int i = 0; i < arr_name.length; i++) {
                        if (arr_name[i].contains("SmartPS")) {
                            arr_filter_address.add(arr_address[i]);
                            arr_filter_name.add(arr_name[i]);
                        }
                    }
                    bluetoothSPP.connect(arr_filter_address.get(0));
                }

                if(btControl.isEnabled() == false)
                    Toast.makeText(getBaseContext(),
                            "Please turn on the Bluetooth, and try again.",
                            Toast.LENGTH_SHORT).show();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {

            if (resultCode == Activity.RESULT_OK) {
                bluetoothSPP.setupService();
                bluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
            } else {
                Toast.makeText(getBaseContext(), "Bluetooth should be turn on, to continue.", Toast.LENGTH_SHORT).show();
                //toast.showMessage("Bluetooth should be turn on, to continue.");
                finish();
            }

        }
    }

    // My methods ==============================
    private void initToolBar(){

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
    private void initBatteryView(){

        batteryMeter = findViewById(R.id.battery_view);
        batteryMeter.setChargeLevel(10);
    }
    private void initLayers(){

        voltageLayout = findViewById(R.id.voltage_layout);
        currentLayout = findViewById(R.id.current_layout);
        powerLayout   = findViewById(R.id.power_layout);

        tempLayout    = findViewById(R.id.layout_temp_time_panel);
        settingPanel  = findViewById(R.id.settings_panel);

        btnControlPanel  = findViewById(R.id.btn_control_panel);
    }
    private void initTextViews(){

        anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(700);
        anim.setStartOffset(10);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);

        txtBtStatus = findViewById(R.id.txt_bt_status);
        txtBtStatus.setText("Disconnected");
        txtBtStatus.startAnimation(anim);

        txtBatVoltage = findViewById(R.id.txt_bat_voltage_value);
        txtBatCurrent  = findViewById(R.id.txt_bat_current_value);
        txtBatPower    = findViewById(R.id.txt_power_value);
        txtBatTemp     = findViewById(R.id.txt_temp_value);
        txtTime        = findViewById(R.id.txt_time_value);

    }
    private void initButtons(){

        btnStart = findViewById(R.id.btn_start);
        btnStop  = findViewById(R.id.btn_stop);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String dataPacket = null;
                batCurrent  = edtBatteryCurrent.getText().toString();
                if(!batCurrent.equals("") && !batCurrent.equals(".")) {
                    tempCurrent = (int) (Float.parseFloat(batCurrent) * 10);
                    if (tempCurrent <= 100 && tempCurrent > 0) {
                        dataPacket = batType + batCells + String.format(Locale.ENGLISH, "%03d", tempCurrent) + chargeType + "1\r";
                        startCharging(dataPacket);
                    }
                    else if(tempCurrent > 100)
                        Toast.makeText(getBaseContext(), "Maximum current should be lower than 10A", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getBaseContext(), "Please set your charge current!", Toast.LENGTH_SHORT).show();

                    Log.i("start", "onClick: " + dataPacket);
                }else
                    Toast.makeText(getBaseContext(), "Please set your charge current!", Toast.LENGTH_SHORT).show();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String dataPacket = batType + batCells + String.format(Locale.ENGLISH, "%03d", tempCurrent) + chargeType + "0\r";
                stopCharging(dataPacket);
                /*final MyAlertDialog alertDialog = new MyAlertDialog(MainActivity.this);
                alertDialog.btnOk.setText("Im sure");
                alertDialog.btnCancel.setText("Forget");
                alertDialog.setMessage("Do you want to stop this process?");
                alertDialog.btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        stopCharging(dataPacket);
                        alertDialog.dismiss();
                    }
                });
                alertDialog.setCancelable(false);
                alertDialog.show();*/

                Log.i("stop", "onClick: " + dataPacket);
            }
        });
    }
    private void initSpinner(){

        btCellInfos = findViewById(R.id.cell_info);

        // sla types
        slaType = new String[]{"6 volt", "12 volt"};

        // li-ion
        ionType = new String[]{"1 cell", "2 cells", "3 cell", "4 cells", "5 cell", "6 cells",
                                        "7 cells", "8 cells", "9 cells", "10 cells", "11 cells",
                                        "12 cells", "13 cells"};

        // ni-mh
        nmType = new String[]{"1 cell", "2 cells", "3 cell", "4 cells", "5 cell", "6 cells",
                                       "7 cells", "8 cells", "9 cells", "10 cells", "11 cells",
                                       "12 cells", "13 cells", "14 cells", "15 cells", "16 cells",
                                       "17 cells", "18 cells", "19 cells", "20 cells", "21 cells",
                                       "22 cells", "23 cells", "24 cells", "25 cells", "26 cells",
                                       "27 cells", "28 cells", "29 cells", "30 cells", "31 cells"};

        // ni-cd
        ncType = new String[]{"1 cell", "2 cells", "3 cell", "4 cells", "5 cell", "6 cells",
                                       "7 cells", "8 cells", "9 cells", "10 cells", "11 cells",
                                       "12 cells", "13 cells", "14 cells", "15 cells", "16 cells",
                                       "17 cells", "18 cells", "19 cells", "20 cells", "21 cells",
                                       "22 cells", "23 cells", "24 cells", "25 cells", "26 cells",
                                       "27 cells", "28 cells", "29 cells", "30 cells", "31 cells"};

        spinnerAdapter = new ArrayAdapter<>(getBaseContext(), R.layout.spinner_item, ionType);
        btCellInfos.setAdapter(spinnerAdapter);

        btCellInfos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {

                batCells = String.format(Locale.ENGLISH,"%02d", pos + 1);
                Log.i("spinner", "onItemSelected: " + batCells);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    private void initEditText(){

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        edtBatteryCurrent = findViewById(R.id.edt_current);
        //edtBatteryCurrent.setFilters(new InputFilter[]{ new InputFilterMinMax("0", "10")});
    }
    private void initRadioGroup(){

        btTypes    = findViewById(R.id.battery_types);
        chrMethods = findViewById(R.id.rg_charge_methods);

        btTypes.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {

                switch (id){

                    case R.id.sla_type:
                        spinnerAdapter = new ArrayAdapter<>(getBaseContext(), R.layout.spinner_item, slaType);
                        btCellInfos.setAdapter(spinnerAdapter);

                        batType = "1";

                        break;

                    case R.id.ion_type:
                        spinnerAdapter = new ArrayAdapter<>(getBaseContext(), R.layout.spinner_item, ionType);
                        btCellInfos.setAdapter(spinnerAdapter);

                        batType = "2";

                        break;

                    case R.id.nm_type:
                        spinnerAdapter = new ArrayAdapter<>(getBaseContext(), R.layout.spinner_item, nmType);
                        btCellInfos.setAdapter(spinnerAdapter);

                        batType = "3";

                        break;

                    case R.id.cd_type:
                        spinnerAdapter = new ArrayAdapter<>(getBaseContext(), R.layout.spinner_item, ncType);
                        btCellInfos.setAdapter(spinnerAdapter);

                        batType = "4";

                        break;
                }
            }
        });

        chrMethods.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {

                switch (id){

                    case R.id.rg_normal_charge:
                        chargeType = "N";
                        break;

                    case R.id.rg_fast_charge:
                        chargeType = "F";
                }
            }
        });
    }
    private void runAnimations(){

        /// set animations for voltage current power panels
        Animation animVoltage = new TranslateAnimation(-800,voltageLayout.getWidth(),0,0);
        animVoltage.setDuration(500);

        Animation animCurrent = new TranslateAnimation(-800,currentLayout.getWidth(),0,0);
        animCurrent.setStartOffset(400);
        animCurrent.setDuration(700);

        Animation animPower = new TranslateAnimation(-800,powerLayout.getWidth(),0,0);
        animPower.setStartOffset(800);
        animPower.setDuration(900);

        voltageLayout.startAnimation(animVoltage);
        currentLayout.startAnimation(animCurrent);
        powerLayout.startAnimation(animPower);

        /// set animations for temp time panels
        Animation animTime = new TranslateAnimation(0,0, 800, tempLayout.getHeight());
        animTime.setDuration(800);

        Animation animSettingPanel = new TranslateAnimation(0,0, 800, settingPanel.getHeight());
        animSettingPanel.setDuration(1100);

        tempLayout.startAnimation(animTime);
        settingPanel.startAnimation(animSettingPanel);

        /// set animations for control panel
        Animation ctlPanel = new TranslateAnimation(600,btnControlPanel.getWidth(), 0, 0);
        ctlPanel.setDuration(2000);

        btnControlPanel.startAnimation(ctlPanel);
    }
    private void initBluetooth(){

        btControl    = new BluetoothControl(this);
        bluetoothSPP = new BluetoothSPP(this);
        bluetoothSPP.setBluetoothConnectionListener(this);

        // enable bluetooth module and setup it
        if(bluetoothSPP.isBluetoothEnabled()){

            if(!bluetoothSPP.isServiceAvailable()) {
                bluetoothSPP.setupService();
                bluetoothSPP.startService(BluetoothState.DEVICE_OTHER);
            }
        }else {

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    Intent btEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(btEnable, BluetoothState.REQUEST_ENABLE_BT);
                }
            }, 3000);

        }
    }
    private void bluetoothOperate() {

        // init process Incoming data
        if(incomingData == null)
            incomingData = new ProcessIncomingData();

        if (!bluetoothSPP.isBluetoothAvailable()) {

            Toast.makeText(getApplicationContext(),
                    "You don't have Bluetooth module.",
                    Toast.LENGTH_LONG);
            finish();
        }

        bluetoothSPP.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data, String message) {

                Log.i("Bluetooth", "onDataReceived: "+ message);

                incomingData.processData(message);

                txtBatVoltage.setText(incomingData.getVoltage());
                txtBatCurrent.setText(incomingData.getCurrent());
                txtBatPower.setText(incomingData.getPower());
                txtBatTemp.setText(incomingData.getTemperature() + "°C");
                txtTime.setText(incomingData.getTime());
            }
        });
    }
    private void turnOffBtModule(){

        if(bluetoothSPP.isServiceAvailable())
            bluetoothSPP.stopService();

        btControl.turnOffBt();
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupWindowAnimations() {
        Slide slide = new Slide();
        slide.setDuration(1200);
        getWindow().setExitTransition(slide);
    }
    private void startCharging(String data){

        bluetoothSPP.send(data, false);
        //btnStart.setEnabled(false);
        //btnStop.setEnabled(true);
    }
    private void stopCharging(String data){

        bluetoothSPP.send(data, false);
        //btnStart.setEnabled(true);
        //btnStop.setEnabled(false);
    }
    private void init(){

        initToolBar();
        initBatteryView();
        initLayers();
        initSpinner();
        initEditText();
        initTextViews();
        initButtons();
        initRadioGroup();
        runAnimations();
        initBluetooth();
        bluetoothOperate();
        setupWindowAnimations();
    }



    /// BluetoothSpp Listeners
    @Override
    public void onDeviceConnected(String name, String address) {

        txtBtStatus.setText("Connected");
        txtBtStatus.clearAnimation();
    }

    @Override
    public void onDeviceDisconnected() {

        txtBtStatus.setText("Disconnected");
        txtBtStatus.startAnimation(anim);
    }

    @Override
    public void onDeviceConnectionFailed() {

        Toast.makeText(this,"Unable to connect.", Toast.LENGTH_LONG).show();
    }
}
