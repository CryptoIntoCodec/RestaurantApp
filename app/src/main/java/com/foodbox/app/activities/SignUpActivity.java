package com.foodbox.app.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.foodbox.app.CountryPicker.Country;
import com.foodbox.app.CountryPicker.CountryPicker;
import com.foodbox.app.CountryPicker.CountryPickerListener;
import com.foodbox.app.build.api.ApiClient;
import com.foodbox.app.build.api.ApiInterface;
import com.foodbox.app.build.configure.BuildConfigure;
import com.foodbox.app.models.Otp;
import com.foodbox.app.HomeActivity;
import com.foodbox.app.R;
import com.foodbox.app.helper.ConnectionHelper;
import com.foodbox.app.helper.CustomDialog;
import com.foodbox.app.helper.GlobalData;
import com.foodbox.app.helper.SharedHelper;
import com.foodbox.app.models.AddCart;
import com.foodbox.app.models.AddressList;
import com.foodbox.app.models.LoginModel;
import com.foodbox.app.models.RegisterModel;
import com.foodbox.app.models.User;
import com.foodbox.app.utils.TextUtils;
import com.foodbox.app.utils.Utils;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignUpActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 1450;
    @BindView(R.id.email)
    EditText emailEdit;
    @BindView(R.id.name)
    EditText nameEdit;
    @BindView(R.id.password)
    EditText passwordEdit;
    @BindView(R.id.sign_up)
    Button signUpBtn;
    ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
    @BindView(R.id.app_logo)
    ImageView appLogo;
    String name, email, password, strConfirmPassword;
    String GRANT_TYPE = "password";
    Context context;
    @BindView(R.id.confirm_password)
    EditText confirmPassword;
    CustomDialog customDialog;
    @BindView(R.id.back_img)
    ImageView backImg;
    @BindView(R.id.password_eye_img)
    ImageView passwordEyeImg;
    @BindView(R.id.confirm_password_eye_img)
    ImageView confirmPasswordEyeImg;
    ConnectionHelper connectionHelper;
    Activity activity;
    String device_token, device_UDID;
    Utils utils = new Utils();
    String TAG = "Login";
    @BindView(R.id.countryImage)
    ImageView countryImage;
    @BindView(R.id.countryNumber)
    TextView countryNumber;
    @BindView(R.id.et_mobile_number)
    EditText etMobileNumber;
    @BindView(R.id.mobile_number_layout)
    RelativeLayout mobileNumberLayout;
    @BindView(R.id.linearLayout)
    LinearLayout linearLayout;
    @BindView(R.id.password_layout)
    RelativeLayout passwordLayout;
    @BindView(R.id.confirm_password_layout)
    RelativeLayout confirmPasswordLayout;
    String country_code = "+91";
    GoogleApiClient mGoogleApiClient;
    private CountryPicker mCountryPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_sign_up);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ButterKnife.bind(this);
        context = SignUpActivity.this;
        activity = SignUpActivity.this;
        connectionHelper = new ConnectionHelper(context);
        customDialog = new CustomDialog(context);
        mCountryPicker = CountryPicker.newInstance("Select Country");
        passwordEyeImg.setTag(1);
        confirmPasswordEyeImg.setTag(1);
        if (!GlobalData.loginBy.equals("manual")) {
            confirmPasswordLayout.setVisibility(View.GONE);
            passwordLayout.setVisibility(View.GONE);
            mobileNumberLayout.setVisibility(View.VISIBLE);
            nameEdit.setText(GlobalData.name);
            emailEdit.setText(GlobalData.email);
        } else {
            confirmPasswordLayout.setVisibility(View.VISIBLE);
            passwordLayout.setVisibility(View.VISIBLE);
            mobileNumberLayout.setVisibility(View.GONE);
        }


        // You can limit the displayed countries
        List<Country> countryList = Country.getAllCountries();
        Collections.sort(countryList, new Comparator<Country>() {
            @Override
            public int compare(Country s1, Country s2) {
                return s1.getName().compareToIgnoreCase(s2.getName());
            }
        });
        mCountryPicker.setCountriesList(countryList);
        setListener();
        //Social login logout
        signOut();
        LoginManager.getInstance().logOut();


        /*----------------Face Integration---------------*/
        try {
            @SuppressLint("PackageManagerGetSignatures") PackageInfo info = getPackageManager().getPackageInfo(
                    "com.foodbox.app",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException ignored) {

        }
        getDeviceToken();

    }

    private void setListener() {
        mCountryPicker.setListener(new CountryPickerListener() {
            @Override
            public void onSelectCountry(String name, String code, String dialCode,
                                        int flagDrawableResID) {
                countryNumber.setText(dialCode);
                country_code = dialCode;
                countryImage.setImageResource(flagDrawableResID);
                mCountryPicker.dismiss();
            }
        });
        countryNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCountryPicker.show(getSupportFragmentManager(), "COUNTRY_PICKER");
            }
        });
        countryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCountryPicker.show(getSupportFragmentManager(), "COUNTRY_PICKER");
            }
        });
        getUserCountryInfo();
    }

    private void getUserCountryInfo() {
        Locale current = getResources().getConfiguration().locale;
        Country country = Country.getCountryFromSIM(context);
        if (country != null) {
            Country us = new Country("IN", "India", "+91", R.drawable.flag_in);
            countryImage.setImageResource(us.getFlag());
            countryNumber.setText(us.getDialCode());
            country_code = us.getDialCode();
            /*countryImage.setImageResource(country.getFlag());
            countryNumber.setText(country.getDialCode());
            country_code = country.getDialCode();*/
        } else {
            Country us = new Country("IN", "India", "+91", R.drawable.flag_in);
            countryImage.setImageResource(us.getFlag());
            countryNumber.setText(us.getDialCode());
            country_code = us.getDialCode();
        }
    }

    public void getDeviceToken() {
        try {
            if (!SharedHelper.getKey(context, "device_token").equals("") && SharedHelper.getKey(context, "device_token") != null) {
                device_token = SharedHelper.getKey(context, "device_token");
                Log.d(TAG, "GCM Registration Token: " + device_token);
            } else {
                device_token = "" + FirebaseInstanceId.getInstance().getToken();
                SharedHelper.putKey(context, "device_token", "" + FirebaseInstanceId.getInstance().getToken());
                Log.d(TAG, "Failed to complete token refresh: " + device_token);
            }
        } catch (Exception e) {
            device_token = "COULD NOT GET FCM TOKEN";
            Log.d(TAG, "Failed to complete token refresh");
        }

        try {
            device_UDID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            Log.d(TAG, "Device UDID:" + device_UDID);
        } catch (Exception e) {
            device_UDID = "COULD NOT GET UDID";
            e.printStackTrace();
            Log.d(TAG, "Failed to complete device UDID");
        }
    }

    @OnClick({R.id.sign_up, R.id.back_img, R.id.password_eye_img, R.id.confirm_password_eye_img})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.sign_up:
                initValues();
                break;
            case R.id.back_img:
                onBackPressed();
                break;
            case R.id.password_eye_img:
                if (passwordEyeImg.getTag().equals(1)) {
                    passwordEdit.setTransformationMethod(null);
                    passwordEyeImg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_eye_close));
                    passwordEyeImg.setTag(0);
                } else {
                    passwordEyeImg.setTag(1);
                    passwordEdit.setTransformationMethod(new PasswordTransformationMethod());
                    passwordEyeImg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_eye_open));
                }
                break;
            case R.id.confirm_password_eye_img:
                if (confirmPasswordEyeImg.getTag().equals(1)) {
                    confirmPassword.setTransformationMethod(null);
                    confirmPasswordEyeImg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_eye_close));
                    confirmPasswordEyeImg.setTag(0);
                } else {
                    confirmPasswordEyeImg.setTag(1);
                    confirmPassword.setTransformationMethod(new PasswordTransformationMethod());
                    confirmPasswordEyeImg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_eye_open));
                }
                break;
        }
    }


    public void signup(HashMap<String, String> map) {
        customDialog.show();
        Call<RegisterModel> call = apiInterface.postRegister(map);
        call.enqueue(new Callback<RegisterModel>() {
            @Override
            public void onResponse(@NonNull Call<RegisterModel> call, @NonNull Response<RegisterModel> response) {
                if (response.body() != null) {
                    HashMap<String, String> map = new HashMap<>();
                    map.put("username", GlobalData.mobile);
                    map.put("password", password);
                    map.put("grant_type", GRANT_TYPE);
                    map.put("client_id", BuildConfigure.CLIENT_ID);
                    map.put("client_secret", BuildConfigure.CLIENT_SECRET);
                    login(map);
                } else if (response.errorBody() != null) {
                    customDialog.dismiss();
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        if (jObjError.has("email"))
                            Toast.makeText(context, jObjError.optString("email"), Toast.LENGTH_LONG).show();
                        else if (jObjError.has("password"))
                            Toast.makeText(context, jObjError.optString("password"), Toast.LENGTH_LONG).show();
                        else if (jObjError.has("error"))
                            Toast.makeText(context, jObjError.optString("error"), Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(context, "Invalid", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegisterModel> call, @NonNull Throwable t) {

            }
        });

    }

    private void signOut() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                //taken from google api console (Web api client id)
//                .requestIdToken("795253286119-p5b084skjnl7sll3s24ha310iotin5k4.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {

//                FirebaseAuth.getInstance().signOut();
                if (mGoogleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                Log.d("MainAct", "Google User Logged out");
                               /* Intent intent = new Intent(LogoutActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();*/
                            }
                        }
                    });
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                Log.d("MAin", "Google API Client Connection Suspended");
            }
        });
    }

    private void login(HashMap<String, String> map) {
        Call<LoginModel> call = apiInterface.postLogin(map);
        call.enqueue(new Callback<LoginModel>() {
            @Override
            public void onResponse(@NonNull Call<LoginModel> call, @NonNull Response<LoginModel> response) {
                if (response.body() != null) {
                    SharedHelper.putKey(context, "access_token", response.body().getTokenType() + " " + response.body().getAccessToken());
                    GlobalData.accessToken = response.body().getTokenType() + " " + response.body().getAccessToken();
                    //Get Profile data
                    getProfile();

                }

            }

            @Override
            public void onFailure(@NonNull Call<LoginModel> call, @NonNull Throwable t) {

            }
        });
    }

    public void initValues() {
        name = nameEdit.getText().toString();
        email = emailEdit.getText().toString();
        strConfirmPassword = confirmPassword.getText().toString();
        if (!GlobalData.loginBy.equals("manual"))
            GlobalData.mobile = country_code + etMobileNumber.getText().toString();
        password = passwordEdit.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, getResources().getString(R.string.please_enter_username), Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, getResources().getString(R.string.please_enter_your_email), Toast.LENGTH_SHORT).show();
        } else if (!TextUtils.isValidEmail(email)) {
            Toast.makeText(this, getResources().getString(R.string.please_enter_valid_email), Toast.LENGTH_SHORT).show();
        } else if (!isValidMobile(etMobileNumber.getText().toString()) && !GlobalData.loginBy.equals("manual")) {
            Toast.makeText(this, getResources().getString(R.string.please_enter_your_mobile_number), Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password) && GlobalData.loginBy.equals("manual")) {
            Toast.makeText(this, getResources().getString(R.string.please_enter_password), Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(strConfirmPassword) && GlobalData.loginBy.equals("manual")) {
            Toast.makeText(this, getResources().getString(R.string.please_enter_your_confirm_password), Toast.LENGTH_SHORT).show();
        } else if (!strConfirmPassword.equalsIgnoreCase(password) && GlobalData.loginBy.equals("manual")) {
            Toast.makeText(this, getResources().getString(R.string.password_and_confirm_password_doesnot_match), Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, String> map = new HashMap<>();
            map.put("name", name);
            map.put("email", email);
            map.put("phone", GlobalData.mobile);
            map.put("password", password);
            map.put("password_confirmation", strConfirmPassword);
            if (connectionHelper.isConnectingToInternet()) {
                if (GlobalData.loginBy.equals("manual")) {
                    signup(map);
                } else {
                    HashMap<String, String> map1 = new HashMap<>();
                    map1.put("phone", GlobalData.mobile);
                    map1.put("login_by", GlobalData.loginBy);
                    map1.put("accessToken", GlobalData.access_token);
                    getOtpVerification(map);
                }

            } else {
                Utils.displayMessage(activity, context, getString(R.string.oops_connect_your_internet));
            }
        }
    }

    public void getOtpVerification(HashMap<String, String> map) {
        customDialog.show();
        Call<Otp> call = apiInterface.postOtp(map);
        call.enqueue(new Callback<Otp>() {
            @Override
            public void onResponse(@NonNull Call<Otp> call, @NonNull Response<Otp> response) {
                customDialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(context, "" + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    GlobalData.otpValue = response.body().getOtp();
                    startActivity(new Intent(context, OtpActivity.class));
                    finish();
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        if (jObjError.has("phone"))
                            Toast.makeText(context, jObjError.optString("phone"), Toast.LENGTH_LONG).show();
                        else if (jObjError.has("email"))
                            Toast.makeText(context, jObjError.optString("email"), Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(context, jObjError.optString("error"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<Otp> call, @NonNull Throwable t) {
                customDialog.dismiss();
                Toast.makeText(SignUpActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getProfile() {
        HashMap<String, String> map = new HashMap<>();
        map.put("device_type", "android");
        map.put("device_id", device_UDID);
        map.put("device_token", device_token);
        Call<User> getprofile = apiInterface.getProfile(map);
        getprofile.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    SharedHelper.putKey(context, "logged", "true");
                    GlobalData.profileModel = response.body();
                    GlobalData.addCart = new AddCart();
                    GlobalData.addCart.setProductList(response.body().getCart());
                    GlobalData.addressList = new AddressList();
                    GlobalData.addressList.setAddresses(response.body().getAddresses());
                    startActivity(new Intent(context, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    finish();
                } else {
                    if (response.code() == 401) {
                        SharedHelper.putKey(context, "logged", "false");
                        startActivity(new Intent(context, LoginActivity.class));
                        finish();
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

            }
        });
    }

//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.anim_nothing, R.anim.slide_out_right);
    }

    private boolean isValidMobile(String phone) {
        boolean check = false;
        if (!Pattern.matches("[a-zA-Z]+", phone)) {
            if (phone.length() < 6 || phone.length() > 13) {
                // if(phone.length() != 10) {
                check = false;
            } else {
                check = true;
            }
        } else {
            check = false;
        }
        return check;
    }
}