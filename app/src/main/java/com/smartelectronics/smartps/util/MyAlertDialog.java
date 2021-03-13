package com.smartelectronics.smartps.util;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import com.smartelectronics.smartps.R;

/**
 * Created by SAEED on 3/2/2019.
 */

public class MyAlertDialog extends Dialog {


    public  Button btnOk;
    public  Button btnCancel;
    private TextView txtMessage, txtHeader;

    private LinearLayout headerColor;

    private Context mContext;

    public MyAlertDialog(@NonNull Context context) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        mContext = context;
        setContentView(R.layout.exit_dialog);

        init();
    }

    private void initButtons(){

        btnOk = findViewById(R.id.bt_ok);
        btnOk.setTransformationMethod(null);

        btnCancel= findViewById(R.id.bt_cancel);
        btnCancel.setTransformationMethod(null);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

    }
    private void initViews(){

        initButtons();

        //txtHeader   = findViewById(R.id.txt_header);
        txtMessage  = findViewById(R.id.txt_messsage);
        headerColor = findViewById(R.id.header_dialog);
    }
    private void init(){
        initViews();

    }

    public void setHeaderColor(int color){

        headerColor.setBackgroundColor(mContext.getResources().getColor(color));
        btnOk.setTextColor(mContext.getResources().getColor(color));
        btnCancel.setTextColor(mContext.getResources().getColor(color));
    }
    public void setTitle(String msg){
        txtHeader.setText(msg);
    }
    public void setMessage(String msg){
        txtMessage.setText(msg);
    }
}
