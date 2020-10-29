package com.foodbox.app.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.foodbox.app.models.Card;
import com.foodbox.app.models.Checksum;
import com.foodbox.app.models.Order;
import com.foodbox.app.R;
import com.foodbox.app.adapter.AccountPaymentAdapter;
import com.foodbox.app.build.api.ApiClient;
import com.foodbox.app.build.api.ApiInterface;
import com.foodbox.app.fragments.CartFragment;
import com.foodbox.app.helper.CustomDialog;
import com.foodbox.app.helper.GlobalData;
import com.foodbox.app.models.Message;
import com.foodbox.app.utils.AppEnvironment;
import com.foodbox.app.utils.TextUtils;
import com.google.gson.Gson;
import com.payumoney.core.PayUmoneyConfig;
import com.payumoney.core.PayUmoneyConstants;
import com.payumoney.core.PayUmoneySdkInitializer;
import com.payumoney.core.entity.TransactionResponse;
import com.payumoney.sdkui.ui.utils.PayUmoneyFlowManager;
import com.payumoney.sdkui.ui.utils.ResultModel;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.foodbox.app.R.string.app_name;
import static com.foodbox.app.helper.GlobalData.MID;
import static com.foodbox.app.helper.GlobalData.cardArrayList;
import static com.foodbox.app.helper.GlobalData.currencySymbol;
import static com.foodbox.app.helper.GlobalData.isCardChecked;

public class AccountPaymentActivity extends AppCompatActivity  {
//    public class AccountPaymentActivity extends AppCompatActivity implements PaymentMethodNonceCreatedListener,
//            BraintreeCancelListener, BraintreeErrorListener, DropInResult.DropInResultListener {
    AppEnvironment appEnvironment;
    public static ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
    public static CustomDialog customDialog;
    public static Context context;
    public static AccountPaymentAdapter accountPaymentAdapter;
    public static LinearLayout cashPaymentLayout;
    public static LinearLayout walletPaymentLayout;
    private boolean isDisableExitConfirmation = false;
    public static RadioButton cashCheckBox,paytmCheckBox;
    public static Button proceedToPayBtn;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.payment_method_lv)
    ListView paymentMethodLv;
    @BindView(R.id.wallet_amount_txt)
    TextView walletAmtTxt;
    @BindView(R.id.wallet_layout)
    RelativeLayout walletLayout;
    NumberFormat numberFormat = GlobalData.getNumberFormat();
    @BindView(R.id.add_new_cart)
    TextView addNewCart;
    boolean isWalletVisible = false;
    boolean isCashVisible = false;
    private boolean mShouldMakePurchase = false;
    private boolean mPurchased = false;
    String custid="", orderId="",amountToPay="";
    private TextUtils mAppPreference;
    private PayUmoneySdkInitializer.PaymentParam mPaymentParams;

    public static void deleteCard(final int id) {
        customDialog.show();
        Call<Message> call = apiInterface.deleteCard(id);
        call.enqueue(new Callback<Message>() {
            @Override
            public void onResponse(@NonNull Call<Message> call, @NonNull Response<Message> response) {
                customDialog.dismiss();
                if (response.isSuccessful()) {
                    Toast.makeText(context, "" + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    for (int i = 0; i < GlobalData.cardArrayList.size(); i++) {
                        if (GlobalData.cardArrayList.get(i).getId().equals(id)) {
                            GlobalData.cardArrayList.remove(i);
                            accountPaymentAdapter.notifyDataSetChanged();
                            return;
                        }
                    }
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(context, jObjError.optString("error"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Message> call, @NonNull Throwable t) {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                customDialog.dismiss();
            }
        });


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppPreference = new TextUtils();
        setContentView(R.layout.activity_account_payment);
        ButterKnife.bind(this);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        context = AccountPaymentActivity.this;
        customDialog = new CustomDialog(context);
        cashPaymentLayout = (LinearLayout) findViewById(R.id.cash_payment_layout);
        appEnvironment = AppEnvironment.SANDBOX;
        walletPaymentLayout = (LinearLayout) findViewById(R.id.wallet_payment_layout);
        proceedToPayBtn = (Button) findViewById(R.id.proceed_to_pay_btn);
        cashCheckBox = (RadioButton) findViewById(R.id.cash_check_box);
        paytmCheckBox = (RadioButton) findViewById(R.id.paytm_check_box);

        orderId = generateStringID();
        custid = generateStringID();

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        isWalletVisible = getIntent().getBooleanExtra("is_show_wallet", false);
        isCashVisible = getIntent().getBooleanExtra("is_show_cash", false);
        amountToPay = getIntent().getStringExtra("amount");

        setAppEnvironment(AppEnvironment.SANDBOX);

        cardArrayList = new ArrayList<>();
        accountPaymentAdapter = new AccountPaymentAdapter(AccountPaymentActivity.this, cardArrayList, !isCashVisible);
        paymentMethodLv.setAdapter(accountPaymentAdapter);

        if (isWalletVisible)
            walletPaymentLayout.setVisibility(VISIBLE);
        else
            walletPaymentLayout.setVisibility(GONE);
        if (isCashVisible)
            cashPaymentLayout.setVisibility(VISIBLE);
        else
            cashPaymentLayout.setVisibility(GONE);

        cashPaymentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cashCheckBox.setChecked(true);
                paytmCheckBox.setChecked(false);
                isCardChecked = false;
                accountPaymentAdapter.notifyDataSetChanged();
                proceedToPayBtn.setVisibility(VISIBLE);
                for (int i = 0; i < cardArrayList.size(); i++) {
                    cardArrayList.get(i).setChecked(false);
                }
                accountPaymentAdapter.notifyDataSetChanged();
            }
        });
        paytmCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAppPreference.setOverrideResultScreen(true);
                    paytmCheckBox.setChecked(true);
                    cashCheckBox.setChecked(false);
                    isCardChecked = false;
                    accountPaymentAdapter.notifyDataSetChanged();
                    proceedToPayBtn.setVisibility(VISIBLE);
                    for (int i = 0; i < cardArrayList.size(); i++) {
                        cardArrayList.get(i).setChecked(false);
                    }
                    accountPaymentAdapter.notifyDataSetChanged();



            }
        });

        proceedToPayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCardChecked) {
                    for (int i = 0; i < cardArrayList.size(); i++) {
                        if (cardArrayList.get(i).isChecked()) {
                            Card card = cardArrayList.get(i);
                            CartFragment.checkoutMap.put("payment_mode", "stripe");
                            return;
                        }
                    }
                } else if (paytmCheckBox.isChecked()) {
                    CartFragment.checkoutMap.put("payment_mode", "payu");
                    generateCheckSum();

                }else if (cashCheckBox.isChecked()) {
                    CartFragment.checkoutMap.put("payment_mode", "cash");
                    checkOut(CartFragment.checkoutMap);
                } else {
                    Toast.makeText(context, "Please select payment mode", Toast.LENGTH_SHORT).show();
                }

            }
        });


//        if (savedInstanceState != null) {
//            if (savedInstanceState.containsKey(KEY_NONCE)) {
//                mNonce = savedInstanceState.getParcelable(KEY_NONCE);
//            }
//        }
    }
    private String generateStringID() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-", "");
    }
    private void generateCheckSum() {
        launchPayUMoneyFlow();
    }

    private void checkOut(HashMap<String, String> map) {
        customDialog.show();
        Log.d("mapisvalues","="+map.toString());
        Call<Order> call = apiInterface.postCheckout(map);
        Log.e("i. at checkout page", String.valueOf(map));
        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(@NonNull Call<Order> call, @NonNull Response<Order> response) {
                customDialog.dismiss();
                Log.d("checkoutpagereso","="+new Gson().toJson(response.body()));
                if (response.isSuccessful()) {
                    GlobalData.addCart = null;
                    GlobalData.notificationCount = 0;
                    GlobalData.selectedShop = null;
                    GlobalData.profileModel.setWalletBalance(response.body().getUser().getWalletBalance());
                    GlobalData.isSelectedOrder = new Order();
                    GlobalData.isSelectedOrder = response.body();
                    startActivity(new Intent(context, CurrentOrderDetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    finish();
                } else {
                    try {
                        Log.e("1. at checkout page", String.valueOf(response.body()));

                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Log.e("2. at checkout page", String.valueOf(jObjError));
                        Toast.makeText(context, jObjError.optString("message"), Toast.LENGTH_LONG).show();
                        finish();
                    } catch (Exception e) {
                        Log.e("3. at checkout page", String.valueOf(e.getMessage()));
                        //startActivity(new Intent(context, CurrentOrderDetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        finish();
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Order> call, @NonNull Throwable t) {
                Log.d("Throwable","="+t.getMessage());
                Toast.makeText(AccountPaymentActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCardList() {
        customDialog.show();
        Call<List<Card>> call = apiInterface.getCardList();
        call.enqueue(new Callback<List<Card>>() {
            @Override
            public void onResponse(@NonNull Call<List<Card>> call, @NonNull Response<List<Card>> response) {
                customDialog.dismiss();
                if (response.isSuccessful()) {
                    cardArrayList.clear();
                    cardArrayList.addAll(response.body());
                    accountPaymentAdapter.notifyDataSetChanged();
                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(context, jObjError.optString("error"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Card>> call, @NonNull Throwable t) {
                customDialog.dismiss();
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

    protected void showDialog(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
//        if (mNonce != null) {
//            outState.putParcelable(KEY_NONCE, mNonce);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int walletMoney = GlobalData.profileModel.getWalletBalance();
        walletAmtTxt.setText(currencySymbol + " " + String.valueOf(walletMoney));
        getCardList();
    }


    @OnClick({R.id.wallet_layout, R.id.add_new_cart})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.wallet_layout:
                startActivity(new Intent(this, WalletActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);
                finish();
                break;
            case R.id.add_new_cart:
//              launchDropIn(view);
                startActivity(new Intent(AccountPaymentActivity.this, AddCardActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);
                break;
        }
    }

//    @Override
//    public void onTransactionResponse(Bundle inResponse) {
//        Log.e("value res", "onTransactionResponse: "+inResponse.toString() );
//        checkOut(CartFragment.checkoutMap);
//
//    }

    public static String hashCal(String str) {
        byte[] hashseq = str.getBytes();
        StringBuilder hexString = new StringBuilder();
        try {
            MessageDigest algorithm = MessageDigest.getInstance("SHA-512");
            algorithm.reset();
            algorithm.update(hashseq);
            byte messageDigest[] = algorithm.digest();
            for (byte aMessageDigest : messageDigest) {
                String hex = Integer.toHexString(0xFF & aMessageDigest);
                if (hex.length() == 1) {
                    hexString.append("0");
                }
                hexString.append(hex);
            }
        } catch (NoSuchAlgorithmException ignored) {
        }
        return hexString.toString();
    }

    /**
     * This function prepares the data for payment and launches payumoney plug n play sdk
     */
    private void launchPayUMoneyFlow() {

        PayUmoneyConfig payUmoneyConfig = PayUmoneyConfig.getInstance();
        Log.e("phone new",removeCountryCode(GlobalData.profileModel.getPhone()));
        //Use this to set your custom text on result screen button
        payUmoneyConfig.setDoneButtonText(String.valueOf(app_name));

        //Use this to set your custom title for the activity
        payUmoneyConfig.setPayUmoneyActivityTitle(String.valueOf(app_name));

        payUmoneyConfig.disableExitConfirmation(isDisableExitConfirmation);

        PayUmoneySdkInitializer.PaymentParam.Builder builder = new PayUmoneySdkInitializer.PaymentParam.Builder();

        double amount = 0;
        try {
            amount = Double.parseDouble(amountToPay.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        String txnId = System.currentTimeMillis() + "";
        //String txnId = "TXNID720431525261327973";
        String phone = "8126961804";//removeCountryCode(GlobalData.profileModel.getPhone());
        String productName = orderId;
        String firstName = GlobalData.profileModel.getName();
        String email = GlobalData.profileModel.getEmail().toString().trim();
        String udf1 = "";
        String udf2 = "";
        String udf3 = "";
        String udf4 = "";
        String udf5 = "";
        String udf6 = "";
        String udf7 = "";
        String udf8 = "";
        String udf9 = "";
        String udf10 = "";

        AppEnvironment appEnvironment = getAppEnvironment();
        builder.setAmount(String.valueOf(amount))
                .setTxnId(txnId)
                .setPhone(phone)
                .setProductName(productName)
                .setFirstName(firstName)
                .setEmail(email)
                .setsUrl(appEnvironment.surl())
                .setfUrl(appEnvironment.furl())
                .setUdf1(udf1)
                .setUdf2(udf2)
                .setUdf3(udf3)
                .setUdf4(udf4)
                .setUdf5(udf5)
                .setUdf6(udf6)
                .setUdf7(udf7)
                .setUdf8(udf8)
                .setUdf9(udf9)
                .setUdf10(udf10)
                .setIsDebug(appEnvironment.debug())
                .setKey(appEnvironment.merchant_Key())
                .setMerchantId(appEnvironment.merchant_ID());

        try {
            mPaymentParams = builder.build();

            /*
             * Hash should always be generated from your server side.
             * */
            //    generateHashFromServer(mPaymentParams);

            /*            *//**
             * Do not use below code when going live
             * Below code is provided to generate hash from sdk.
             * It is recommended to generate hash from server side only.
             * */
            mPaymentParams = calculateServerSideHashAndInitiatePayment1(mPaymentParams);



                PayUmoneyFlowManager.startPayUMoneyFlow(mPaymentParams,AccountPaymentActivity.this, R.style.AppTheme_default, mAppPreference.isOverrideResultScreen());


        } catch (Exception e) {
            // some exception occurred
            Log.e("issue ",e.toString());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            //payNowButton.setEnabled(true);
        }
    }

    /**
     * Thus function calculates the hash for transaction
     *
     * @param paymentParam payment params of transaction
     * @return payment params along with calculated merchant hash
     */
    private PayUmoneySdkInitializer.PaymentParam calculateServerSideHashAndInitiatePayment1(final PayUmoneySdkInitializer.PaymentParam paymentParam) {

        StringBuilder stringBuilder = new StringBuilder();
        HashMap<String, String> params = paymentParam.getParams();
        stringBuilder.append(params.get(PayUmoneyConstants.KEY) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.TXNID) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.AMOUNT) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.PRODUCT_INFO) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.FIRSTNAME) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.EMAIL) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF1) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF2) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF3) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF4) + "|");
        stringBuilder.append(params.get(PayUmoneyConstants.UDF5) + "||||||");

        AppEnvironment appEnvironment = getAppEnvironment();
        stringBuilder.append(appEnvironment.salt());

        String hash = hashCal(stringBuilder.toString());
        paymentParam.setMerchantHash(hash);

        return paymentParam;
    }

    public AppEnvironment getAppEnvironment() {
        return appEnvironment;
    }

    public void setAppEnvironment(AppEnvironment appEnvironment) {
        this.appEnvironment = appEnvironment;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result Code is -1 send from Payumoney activity
        Log.d("MainActivity", "request code " + requestCode + " resultcode " + resultCode);
        if (requestCode == PayUmoneyFlowManager.REQUEST_CODE_PAYMENT && resultCode == RESULT_OK && data !=
                null) {
            TransactionResponse transactionResponse = data.getParcelableExtra(PayUmoneyFlowManager
                    .INTENT_EXTRA_TRANSACTION_RESPONSE);

            ResultModel resultModel = data.getParcelableExtra(PayUmoneyFlowManager.ARG_RESULT);

            // Check which object is non-null
            if (transactionResponse != null && transactionResponse.getPayuResponse() != null) {
                if (transactionResponse.getTransactionStatus().equals(TransactionResponse.TransactionStatus.SUCCESSFUL)) {
                    //Success Transaction
                    checkOut(CartFragment.checkoutMap);
                } else {
                    //Failure Transaction
                    Toast.makeText(context, "Try again later", Toast.LENGTH_SHORT).show();
                }

                // Response from Payumoney
                String payuResponse = transactionResponse.getPayuResponse();

                // Response from SURl and FURL
                String merchantResponse = transactionResponse.getTransactionDetails();

                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage("Payu's Data : " + payuResponse + "\n\n\n Merchant's Data: " + merchantResponse)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }).show();

            } else if (resultModel != null && resultModel.getError() != null) {
                Log.d("payu : ", "Error response : " + resultModel.getError().getTransactionResponse());
            } else {
                Log.d("payu : ", "Both objects are null!");
            }
        }
    }

    public static String removeCountryCode(String strMobWithCD){

        String strNewMob = null;
        if(strMobWithCD.contains("+91")){
            strNewMob = strMobWithCD.replace("+91","").toString();
        }else if(strMobWithCD.contains("91")){
            strNewMob = strMobWithCD.replace("91","").toString();
        }else {
            strNewMob = strMobWithCD;
        }
        return strNewMob;
    }
}
