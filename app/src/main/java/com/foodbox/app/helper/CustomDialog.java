package com.foodbox.app.helper;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.Window;

import com.foodbox.app.R;


public class CustomDialog extends Dialog {

    public CustomDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog);
        //setIndeterminate(true);
        //setMessage("Please wait...");
        setCancelable(false);
        //  setContentView(R.layout.custom_dialog);
    }
}
