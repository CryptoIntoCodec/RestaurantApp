package com.foodbox.app.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.google.android.material.snackbar.Snackbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.foodbox.app.build.api.ApiInterface;
import com.foodbox.app.helper.GlobalData;
import com.foodbox.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Tamil on 10/14/2017.
 */

public class Utils {
    public static boolean showLog = true;
    public static String address = "";
    Retrofit retrofit;
    ApiInterface apiInterface;

    public static void displayMessage(Activity activity, Context context, String toastString) {
        try {
            Snackbar.make(activity.getCurrentFocus(), toastString, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        } catch (Exception e) {
            try {
                Toast.makeText(context, "" + toastString, Toast.LENGTH_SHORT).show();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }
    public static String getCurrentDate() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String strDate = sdf.format(c.getTime());

        return strDate;
    }
    public static String getCurrentDay() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE");
        String strDate = sdf.format(c.getTime());

        return strDate;
    }

    public  static  boolean checktimings(String startTime, String endtime) {

        String pattern = "HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);

        try {
            Date selected = sdf.parse(GlobalData.selectedTime);
            Date start = sdf.parse(startTime);
            Date end= sdf.parse(endtime);

            if(selected.getTime()>=start.getTime()&&selected.getTime()<=end.getTime()) {
                return true;
            } else {

                return false;
            }
        } catch (ParseException e){
            e.printStackTrace();
        }
        return false;
    }

    public static void print(String tag, String message) {
        if (showLog) {
            Log.v(tag, message);
        }
    }

    public static boolean isShopChanged(int shopId) {
        if (GlobalData.addCart != null && !GlobalData.addCart.getProductList().isEmpty()) {
            if (!GlobalData.addCart.getProductList().get(0).getProduct().getShopId().equals(shopId)) {
                return true;
            }
        }
        return false;

    }

    public String getAddress(final Context context, final double latitude, final double longitude) {
        retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/geocode/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiInterface = retrofit.create(ApiInterface.class);
        Call<ResponseBody> call = apiInterface.getResponse(latitude + "," + longitude,
                context.getResources().getString(R.string.google_api_key));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.e("sUCESS", "SUCESS" + response.body());
                if (response.body() != null) {
                    try {
                        String bodyString = new String(response.body().bytes());
                        Log.e("sUCESS", "bodyString" + bodyString);
                        JSONObject jsonObj = new JSONObject(bodyString);
                        JSONArray jsonArray = jsonObj.optJSONArray("results");
                        if (jsonArray.length() > 0) {
                            address = jsonArray.optJSONObject(0).optString("formatted_address");
                            Log.v("Formatted Address", "" + GlobalData.addressHeader);
                        } else {
                            address = "" + latitude + "" + longitude;

                        }
                    } catch (IOException e) {
                        e.printStackTrace();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    address = "" + latitude + "" + longitude;
                }
                //BroadCast Listner
                Intent intent = new Intent("location");
                // You can also include some extra data.
                intent.putExtra("message", "This is my message!");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("onFailure", "onFailure" + call.request().url());
                address = "" + latitude + "" + longitude;

            }
        });
        return address;

    }


}
