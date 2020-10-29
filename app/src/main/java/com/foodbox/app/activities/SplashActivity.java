package com.foodbox.app.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;

import com.foodbox.app.SearchOnIntroActivity;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.foodbox.app.HomeActivity;
import com.foodbox.app.R;
import com.foodbox.app.build.api.ApiClient;
import com.foodbox.app.build.api.ApiInterface;
import com.foodbox.app.helper.ConnectionHelper;
import com.foodbox.app.helper.GlobalData;
import com.foodbox.app.helper.SharedHelper;
import com.foodbox.app.models.AddCart;
import com.foodbox.app.models.AddressList;
import com.foodbox.app.models.User;
import com.foodbox.app.utils.Utils;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.util.HashMap;

import io.fabric.sdk.android.Fabric;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.foodbox.app.helper.GlobalData.addCart;

    public class SplashActivity extends AppCompatActivity {

        int retryCount = 0;
        Context context;
        ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
        ConnectionHelper connectionHelper;
        String device_token, device_UDID;
        Utils utils = new Utils();
        String TAG = "Login";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Fabric.with(this, new Crashlytics());
            setContentView(R.layout.activity_splash);
            context = SplashActivity.this;
            connectionHelper = new ConnectionHelper(context);
            getDeviceToken();
           // Toast.makeText(context, "oncreate", Toast.LENGTH_SHORT).show();

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Do something after 3000ms
                   // Toast.makeText(context, "handler", Toast.LENGTH_SHORT).show();

                    if (SharedHelper.getKey(context, "logged").equalsIgnoreCase("true") && SharedHelper.getKey(context, "logged") != null) {
                        GlobalData.accessToken = SharedHelper.getKey(context, "access_token");
                        if (connectionHelper.isConnectingToInternet()) {
                            getProfile();
                            Toast.makeText(context, "getProfile", Toast.LENGTH_SHORT).show();

                        } else {
                            displayMessage(getString(R.string.oops_connect_your_internet));
                        }

                    } else {
                        startActivity(new Intent(SplashActivity.this, WelcomeScreenActivity.class));
                        Toast.makeText(context, "WelcomeScreenActivity", Toast.LENGTH_SHORT).show();

                        finish();
                    }

                }
            }, 3000);
        }

        public void getDeviceToken() {
            try {
               // Toast.makeText(context, "getDeviceToken", Toast.LENGTH_SHORT).show();

                if (!SharedHelper.getKey(context, "device_token").equals("") && SharedHelper.getKey(context, "device_token") != null) {
                    device_token = SharedHelper.getKey(context, "device_token");
                    Log.d(TAG, "GCM Registration Token: " + device_token);
                } else {
                 //   Toast.makeText(context, "elsegetDeviceToken", Toast.LENGTH_SHORT).show();

                    device_token = "" + FirebaseInstanceId.getInstance().getToken();
                    SharedHelper.putKey(context, "device_token", "" + FirebaseInstanceId.getInstance().getToken());
                    Log.d(TAG, "Failed to complete token refresh: " + device_token);
                }
            } catch (Exception e) {
                device_token = "COULD NOT GET FCM TOKEN";
                Log.d(TAG, "Failed to complete token refresh");
               // Toast.makeText(context, "COULD NOT GET FCM TOKEN", Toast.LENGTH_SHORT).show();

            }

            try {
                device_UDID = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
                Log.d(TAG, " get Device UDID:" + device_UDID);
                //Toast.makeText(context, "Device UDID", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                device_UDID = "COULD NOT GET UDID";
              //  Toast.makeText(context, "Devicenot UDID", Toast.LENGTH_SHORT).show();

                e.printStackTrace();
                Log.d(TAG, "Failed to complete device UDID");
            }
        }

        private void getProfile() {
            retryCount++;
            Toast.makeText(context, "profile", Toast.LENGTH_SHORT).show();

            HashMap<String, String> map = new HashMap<>();
            map.put("device_type", "android");
            map.put("device_id", device_UDID);
            map.put("device_token", device_token);
            Call<User> getprofile = apiInterface.getProfile(map);
            getprofile.enqueue(new Callback<User>() {
                @Override
                public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                    Toast.makeText(context, "onResponse", Toast.LENGTH_SHORT).show();

                    if (response.isSuccessful()) {
                        SharedHelper.putKey(context, "logged", "true");
                        GlobalData.profileModel = response.body();
                        addCart = new AddCart();
                        addCart.setProductList(response.body().getCart());
                        GlobalData.addressList = new AddressList();
                        GlobalData.addressList.setAddresses(response.body().getAddresses());
                        if (addCart.getProductList() != null && addCart.getProductList().size() != 0)
                            GlobalData.addCartShopId = addCart.getProductList().get(0).getProduct().getShopId();
                        startActivity(new Intent(context, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        finish();
                    } else {
                        Toast.makeText(context, "elseprofile", Toast.LENGTH_SHORT).show();

                        if (response.code() == 401) {
                            SharedHelper.putKey(context, "logged", "false");
                            startActivity(new Intent(context, LoginActivity.class));
                            finish();
                            Toast.makeText(context, "401", Toast.LENGTH_SHORT).show();
                        }
                        try {
                            JSONObject jObjError = new JSONObject(response.errorBody().toString());
                            Toast.makeText(context, jObjError.optString("error"), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }


                }

                @Override
                public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                    if (retryCount < 5) {
                        getProfile();
                    }

                }
            });
        }

        public void displayMessage(String toastString) {
            try {
                Snackbar.make(getCurrentFocus(), toastString, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            } catch (Exception e) {
                try {
                    Toast.makeText(context, "" + toastString, Toast.LENGTH_SHORT).show();
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
            }
        }

//        @Override
//        protected void attachBaseContext(Context newBase) {
//            super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
//        }
}