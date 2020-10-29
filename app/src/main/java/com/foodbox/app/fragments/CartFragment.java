package com.foodbox.app.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.ViewSkeletonScreen;
import com.foodbox.app.activities.AccountPaymentActivity;
import com.foodbox.app.activities.CurrentOrderDetailActivity;
import com.foodbox.app.activities.HotelViewActivity;
import com.foodbox.app.activities.ProductDetailActivity;
import com.foodbox.app.activities.SaveDeliveryLocationActivity;
import com.foodbox.app.adapter.ViewCartAdapter;
import com.foodbox.app.HomeActivity;
import com.foodbox.app.R;
import com.foodbox.app.activities.PromotionActivity;
import com.foodbox.app.activities.SetDeliveryLocationActivity;
import com.foodbox.app.build.api.ApiClient;
import com.foodbox.app.build.api.ApiInterface;
import com.foodbox.app.helper.ConnectionHelper;
import com.foodbox.app.helper.CustomDialog;
import com.foodbox.app.helper.GlobalData;
import com.foodbox.app.models.AddCart;
import com.foodbox.app.models.Cart;
import com.foodbox.app.models.ClearCart;
import com.foodbox.app.models.Order;
import com.foodbox.app.utils.Utils;
import com.robinhood.ticker.TickerUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.foodbox.app.helper.GlobalData.selectedShop;


/**
 * Created  on 22-08-2017.
 */
public class CartFragment extends Fragment {

    //Animation number
    private static final char[] NUMBER_LIST = TickerUtils.getDefaultNumberList();
    public static RelativeLayout dataLayout;
    public static RelativeLayout errorLayout;
    public static TextView itemTotalAmount, deliveryCharges, promoCodeApply, discountAmount, serviceTax, payAmount;
    //Orderitem List
    private int mYear, mMonth, mDay, mHour, mMinute;

    public static List<Cart> viewCartItemList;
    public static int deliveryChargeValue = 0;
    public static ViewCartAdapter viewCartAdapter;
    public static HashMap<String, String> checkoutMap;
    @BindView(R.id.re)
    RelativeLayout re;
    @BindView(R.id.order_item_rv)
    RecyclerView orderItemRv;
    @BindView(R.id.map_marker_image)
    ImageView mapMarkerImage;
    @BindView(R.id.location_error_title)
    TextView locationErrorTitle;
    @BindView(R.id.location_error_sub_title)
    TextView locationErrorSubTitle;
    @BindView(R.id.add_address_btn)
    Button addAddressBtn;
    @BindView(R.id.dummy_image_view)
    ImageView dummyImageView;
    @BindView(R.id.total_amount)
    TextView totalAmount;
    @BindView(R.id.buttonLayout)
    LinearLayout buttonLayout;
    @BindView(R.id.address_header)
    TextView addressHeader;
    @BindView(R.id.address_detail)
    TextView addressDetail;
    @BindView(R.id.address_delivery_time)
    TextView addressDeliveryTime;
    @BindView(R.id.add_address_txt)
    TextView addAddressTxt;
    @BindView(R.id.bottom_layout)
    LinearLayout bottomLayout;
    @BindView(R.id.location_info_layout)
    LinearLayout locationInfoLayout;
    @BindView(R.id.location_error_layout)
    RelativeLayout locationErrorLayout;
    @BindView(R.id.restaurant_image)
    ImageView restaurantImage;
    @BindView(R.id.restaurant_name)
    TextView restaurantName;
    @BindView(R.id.restaurant_description)
    TextView restaurantDescription;
    @BindView(R.id.proceed_to_pay_btn)
    Button proceedToPayBtn;
    @BindView(R.id.proceed_to_schedule)
    Button proceedToSchBtn;
    @BindView(R.id.selected_address_btn)
    Button selectedAddressBtn;
    @BindView(R.id.amount_remaning_txt)
    TextView remaningAmount;
    @BindView(R.id.error_layout_description)
    TextView errorLayoutDescription;
    @BindView(R.id.use_wallet_chk_box)
    CheckBox useWalletChkBox;
    @BindView(R.id.amount_txt)
    TextView amountTxt;
    @BindView(R.id.custom_notes)
    TextView customNotes;
    @BindView(R.id.wallet_layout)
    LinearLayout walletLayout;
    AnimatedVectorDrawableCompat avdProgress;
    Fragment orderFullViewFragment;
    FragmentManager fragmentManager;
    double priceAmount = 0;
    double discount = 0;
    int itemCount = 0;
    int itemQuantity = 0;
    int ADDRESS_SELECTION = 1;
    ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
    CustomDialog customDialog;
    ViewSkeletonScreen skeleton;
    ConnectionHelper connectionHelper;
    Activity activity;
    private Context context;
    private ViewGroup toolbar;
    private View toolbarLayout;
    private boolean isOpen=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getContext();
        this.activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        ButterKnife.bind(this, view);
        connectionHelper = new ConnectionHelper(context);

        /*  Intialize Global Values*/
        itemTotalAmount = view.findViewById(R.id.item_total_amount);
        deliveryCharges = view.findViewById(R.id.delivery_charges);
        promoCodeApply = view.findViewById(R.id.promo_code_apply);
        discountAmount = view.findViewById(R.id.discount_amount);
        serviceTax = view.findViewById(R.id.service_tax);
        payAmount = view.findViewById(R.id.total_amount);
        dataLayout = view.findViewById(R.id.data_layout);
        errorLayout = view.findViewById(R.id.error_layout);


        proceedToSchBtn.setText("Schedule "+"("+GlobalData.selectedDate+" "+GlobalData.selectedTime+")");
//        getActivity().findViewById(R.id.fab_map).setVisibility(View.GONE);

        HomeActivity.updateNotificationCount(context, 0);
        customDialog = new CustomDialog(context);

        skeleton = Skeleton.bind(dataLayout)
                .load(R.layout.skeleton_fragment_cart)
                .show();
        viewCartItemList = new ArrayList<>();
        //Offer Restaurant Adapter
        orderItemRv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        orderItemRv.setItemAnimator(new DefaultItemAnimator());
        orderItemRv.setHasFixedSize(false);
        orderItemRv.setNestedScrollingEnabled(false);

        //Intialize address Value
        if (GlobalData.selectedAddress != null && GlobalData.selectedAddress.getLandmark() != null) {
            if (GlobalData.addressList.getAddresses().size() == 1)
                addAddressTxt.setText(getString(R.string.add_address));
            else
                addAddressTxt.setText(getString(R.string.change_address));
            addAddressBtn.setBackgroundResource(R.drawable.button_corner_bg_green);
            addAddressBtn.setText(getResources().getString(R.string.proceed_to_pay));
            addressHeader.setText(GlobalData.selectedAddress.getType());
            addressDetail.setText(GlobalData.selectedAddress.getMapAddress());
            if (viewCartItemList != null && viewCartItemList.size() != 0)
                addressDeliveryTime.setText(viewCartItemList.get(0).getProduct().getShop().getEstimatedDeliveryTime().toString() + " Mins");
        } else if (GlobalData.addressList != null) {
            addAddressBtn.setBackgroundResource(R.drawable.button_corner_bg_theme);
            addAddressBtn.setText(getResources().getString(R.string.add_address));
            locationErrorSubTitle.setText(GlobalData.addressHeader);
            selectedAddressBtn.setVisibility(View.VISIBLE);
            locationErrorLayout.setVisibility(View.VISIBLE);
            locationInfoLayout.setVisibility(View.GONE);
        } else {
            if (GlobalData.selectedAddress != null)
                locationErrorSubTitle.setText(GlobalData.selectedAddress.getMapAddress());
            else
                locationErrorSubTitle.setText(GlobalData.addressHeader);
            locationErrorLayout.setVisibility(View.VISIBLE);
            selectedAddressBtn.setVisibility(View.GONE);
            locationInfoLayout.setVisibility(View.GONE);
        }
        return view;
    }

    private double topPayAmount = 0,finalAmount=0;

    private void getViewCart() {
        Call<AddCart> call = apiInterface.getViewCart();
        call.enqueue(new Callback<AddCart>() {
            @Override
            public void onResponse(Call<AddCart> call, Response<AddCart> response) {
                //Toast.makeText(context,"hello", Toast.LENGTH_LONG).show();
                skeleton.hide();
                if (response != null && !response.isSuccessful() && response.errorBody() != null) {
                    errorLayout.setVisibility(View.VISIBLE);
                    dataLayout.setVisibility(View.GONE);
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(context, jObjError.optString("message"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else if (response.isSuccessful()) {
                    customDialog.dismiss();
                    //get Item Count
                    itemCount = response.body().getProductList().size();
                    GlobalData.notificationCount = response.body().getProductList().size();
                    if (itemCount == 0) {
                        errorLayout.setVisibility(View.VISIBLE);
                        dataLayout.setVisibility(View.GONE);
                        GlobalData.addCart = response.body();
                        GlobalData.addCart = null;
                    } else {
                        AddCart addCart = response.body();
                        errorLayout.setVisibility(View.GONE);
                        dataLayout.setVisibility(View.VISIBLE);
                        for (int i = 0; i < itemCount; i++) {
                            //Get Total item Quantity
                            itemQuantity = itemQuantity + response.body().getProductList().get(i).getQuantity();
                            //Get product price
                            if (response.body().getProductList().get(i).getProduct().getPrices().getPrice() != null)
                                priceAmount = priceAmount + (response.body().getProductList().get(i).getQuantity()
                                        * response.body().getProductList().get(i).getProduct().getPrices().getPrice());

                            if (addCart.getProductList().get(i).getCartAddons() != null && !addCart.getProductList().get(i).getCartAddons().isEmpty())
                                for (int j = 0; j < addCart.getProductList().get(i).getCartAddons().size(); j++)
                                    priceAmount = priceAmount + (addCart.getProductList().get(i).getQuantity()
                                            * (addCart.getProductList().get(i).getCartAddons().get(j).getQuantity()
                                            * addCart.getProductList().get(i).getCartAddons().get(j).getAddonProduct().getPrice()));
                        }
                        GlobalData.notificationCount = itemQuantity;
                        GlobalData.addCartShopId = response.body().getProductList().get(0).getProduct().getShopId();

                        //      Set Payment details
                        String currency = response.body().getProductList().get(0).getProduct().getPrices().getCurrency();

                        //      RRR item total
                        itemTotalAmount.setText(currency + "" + priceAmount);

                        //      RRR Delivery Fee
                        deliveryCharges.setText(currency + "" + response.body().getDeliveryCharges().toString());

                        //      RRR IGST
                        double itemTotalIGST;
                        double igstVal = priceAmount * response.body().getTaxPercentage() / 100;
                        System.out.println("RRR Tax Amt = " + igstVal);
//                        itemTotalIGST = priceAmount + Math.round(igstVal);
//                        serviceTax.setText(currency + String.valueOf(Math.round(igstVal)));
                        itemTotalIGST = priceAmount + igstVal;
                        serviceTax.setText(currency + igstVal);

                        //      RRR Discount
                        double itemTotalDiscount;
                        if (response.body().getProductList().get(0).getProduct().getShop().getOfferMinAmount() != null)
                            if (response.body().getProductList().get(0).getProduct().getShop().getOfferMinAmount() < priceAmount) {
                                int offerPercentage = response.body().getProductList().get(0).getProduct().getShop().getOfferPercent();
                                discount = (itemTotalIGST * offerPercentage) / 100;
                            }
                        itemTotalDiscount = itemTotalIGST - discount;
                        discountAmount.setText("- " + currency + "" + discount);

                        int money = GlobalData.profileModel.getWalletBalance();
                        if (useWalletChkBox.isChecked()&&money>0){
                            topPayAmount = itemTotalDiscount + response.body().getDeliveryCharges();
                            if (money>topPayAmount){
                                payAmount.setText(currency + "0");
                                remaningAmount.setText("( "+currency+(money-Math.round(topPayAmount))+" )");
                                finalAmount=0;
                            }

                            else {
                                payAmount.setText(currency + "" + Math.round(topPayAmount-money));
                                finalAmount=topPayAmount-money;
                            }

                        }else {
                            topPayAmount = itemTotalDiscount + response.body().getDeliveryCharges();
                            payAmount.setText(currency + "" + Math.round(topPayAmount));
                            finalAmount=topPayAmount;

                        }
                        useWalletChkBox.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (useWalletChkBox.isChecked()){

                                    topPayAmount = itemTotalDiscount + response.body().getDeliveryCharges();
                                    if (money>topPayAmount){
                                        payAmount.setText(currency + "0");
                                        remaningAmount.setText("( "+currency+(money-Math.round(topPayAmount))+" )");
                                        finalAmount=0;

                                    }

                                    else {
                                        payAmount.setText(currency + "" + Math.round(topPayAmount-money));
                                        finalAmount=topPayAmount-money;

                                    }


                                }
                                else {
                                    topPayAmount = itemTotalDiscount + response.body().getDeliveryCharges();
                                    payAmount.setText(currency + "" + Math.round(topPayAmount));
                                    remaningAmount.setText(currency+"0");
                                    finalAmount=topPayAmount;


                                }
                            }
                        });
/*

                        //      RRR Total Payable Amount
                        topPayAmount = itemTotalDiscount + response.body().getDeliveryCharges() - money;
                        payAmount.setText(currency + "" + Math.round(topPayAmount));
*/

                        //Set Restaurant Details
                        restaurantName.setText(response.body().getProductList().get(0).getProduct().getShop().getName());
                        restaurantDescription.setText(response.body().getProductList().get(0).getProduct().getShop().getDescription());
                        String image_url = response.body().getProductList().get(0).getProduct().getShop().getAvatar();

                        RequestOptions options = new RequestOptions()
                                .placeholder(R.drawable.ic_restaurant_place_holder)
                                .error(R.drawable.ic_restaurant_place_holder)
                                .dontAnimate()
                                .priority(Priority.HIGH);

                        Glide
                                .with(context)
                                .load(image_url)
                                .apply(options)
                                .into(restaurantImage);

                        deliveryChargeValue = response.body().getDeliveryCharges();

                        viewCartItemList.clear();
                        viewCartItemList = response.body().getProductList();
                        viewCartAdapter = new ViewCartAdapter(viewCartItemList, context);
                        orderItemRv.setAdapter(viewCartAdapter);
                    }

                }
            }

            @Override
            public void onFailure(Call<AddCart> call, Throwable t) {
                errorLayout.setVisibility(View.VISIBLE);
                dataLayout.setVisibility(View.GONE);
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        priceAmount = 0;
        discount = 0;
        itemCount = 0;
        itemQuantity = 0;
        if (GlobalData.profileModel != null) {
            int money = GlobalData.profileModel.getWalletBalance();
            dataLayout.setVisibility(View.VISIBLE);
            errorLayout.setVisibility(View.GONE);
            skeleton.show();
            errorLayoutDescription.setText(getResources().getString(R.string.cart_error_description));
            if (connectionHelper.isConnectingToInternet()) getViewCart();
            else
                Utils.displayMessage(activity, context, getString(R.string.oops_connect_your_internet));
            if (money > 0) {
//                amountTxt.setText(numberFormat.format(money));
                amountTxt.setText(GlobalData.currencySymbol + " " + money);
                walletLayout.setVisibility(View.VISIBLE);
            } else walletLayout.setVisibility(View.INVISIBLE);
        } else {
            dataLayout.setVisibility(View.GONE);
            errorLayout.setVisibility(View.VISIBLE);
            errorLayoutDescription.setText(getResources().getString(R.string.please_login_and_order_dishes));
        }
        if (ViewCartAdapter.bottomSheetDialogFragment != null)
            ViewCartAdapter.bottomSheetDialogFragment.dismiss();

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (toolbar != null) toolbar.removeView(toolbarLayout);
    }


    public void FeedbackDialog() {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.feedback);
        EditText commentEdit = dialog.findViewById(R.id.comment);

        Button submitBtn = dialog.findViewById(R.id.submit);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        System.out.println("CartFragment");
        toolbar = getActivity().findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setVisibility(View.GONE);
            dummyImageView.setVisibility(View.VISIBLE);
        } else {

            dummyImageView.setVisibility(View.GONE);
        }

    }


    @OnClick({R.id.add_address_txt, R.id.add_address_btn, R.id.selected_address_btn, R.id.proceed_to_pay_btn, R.id.proceed_to_schedule, R.id.apppromocode})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.add_address_txt:
                /**  If address is empty */
                if (addAddressTxt.getText().toString().equalsIgnoreCase(getResources().getString(R.string.change_address))) {
                    startActivityForResult(new Intent(getActivity(), SetDeliveryLocationActivity.class).putExtra("get_address", true), ADDRESS_SELECTION);
                    getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);
                }
                /**  If address is filled */
                else if (addAddressTxt.getText().toString().equalsIgnoreCase(getResources().getString(R.string.add_address))) {
                    startActivityForResult(new Intent(getActivity(), SaveDeliveryLocationActivity.class).putExtra("get_address", true), ADDRESS_SELECTION);
                    getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);
                }
                break;
            case R.id.add_address_btn:
                /**  If address is empty */
                startActivityForResult(new Intent(getActivity(), SaveDeliveryLocationActivity.class).putExtra("get_address", true), ADDRESS_SELECTION);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);
                break;
            case R.id.selected_address_btn:
                /**  If address is filled */
                startActivityForResult(new Intent(getActivity(), SetDeliveryLocationActivity.class).putExtra("get_address", true), ADDRESS_SELECTION);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);

                break;

            case R.id.proceed_to_schedule:
                /**  If address is filled */

                    OPenDialogFloat();
                break;

            case R.id.proceed_to_pay_btn:
                /**  If address is filled */
                if (connectionHelper.isConnectingToInternet()) {
//                    checkOut(GlobalData.getInstance().selectedAddress.getId());
                    checkoutMap = new HashMap<>();
                    checkoutMap.put("user_address_id", "" + GlobalData.selectedAddress.getId());
                    checkoutMap.put("note", "" + customNotes.getText());

                    if (useWalletChkBox.isChecked())
                        checkoutMap.put("wallet", "1");
                    else
                        checkoutMap.put("wallet", "0");



                    if (finalAmount==0){
                        checkoutMap.put("payment_mode", "cash");
                        checkOut(checkoutMap);
                    }else {
                        startActivity(new Intent(context, AccountPaymentActivity.class).putExtra("is_show_wallet", false).putExtra("is_show_cash", true).putExtra("amount",""+finalAmount));
                        activity.overridePendingTransition(R.anim.anim_nothing, R.anim.slide_out_right);

                    }
                    } else {
                    Utils.displayMessage(activity, context, getString(R.string.oops_connect_your_internet));
                }
                break;
            case R.id.apppromocode:
                startActivity(new Intent(context, PromotionActivity.class));
                break;

        }
    }

    private void checkOut(HashMap<String, String> map) {
        customDialog.show();
        Call<Order> call = apiInterface.postCheckout(map);
        Log.e("i. at checkout page", String.valueOf(map));
        call.enqueue(new Callback<Order>() {
            @Override
            public void onResponse(@NonNull Call<Order> call, @NonNull Response<Order> response) {
                customDialog.dismiss();
                Log.e("0. at checkout page", String.valueOf(response));
                if (response.isSuccessful()) {
                    GlobalData.addCart = null;
                    GlobalData.notificationCount = 0;
                    GlobalData.selectedShop = null;
                    GlobalData.profileModel.setWalletBalance(response.body().getUser().getWalletBalance());
                    GlobalData.isSelectedOrder = new Order();
                    GlobalData.isSelectedOrder = response.body();
                    startActivity(new Intent(context, CurrentOrderDetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

                } else {
                    try {
                        Log.e("1. at checkout page", String.valueOf(response.body()));

                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Log.e("2. at checkout page", String.valueOf(jObjError));
                        Toast.makeText(context, jObjError.optString("message"), Toast.LENGTH_LONG).show();

                    } catch (Exception e) {
                        Log.e("3. at checkout page", String.valueOf(e.getMessage()));
                        //startActivity(new Intent(context, CurrentOrderDetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Order> call, @NonNull Throwable t) {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.print("CartFragment");
        if (requestCode == ADDRESS_SELECTION && resultCode == Activity.RESULT_OK) {
            System.out.print("CartFragment : Success");
            if (GlobalData.selectedAddress != null) {
                locationErrorLayout.setVisibility(View.GONE);
                locationInfoLayout.setVisibility(View.VISIBLE);
                //Intialize address Value
                if (GlobalData.selectedAddress != null && GlobalData.selectedAddress.getLandmark() != null) {
                    if (GlobalData.addressList.getAddresses().size() == 1)
                        addAddressTxt.setText(getString(R.string.add_address));
                    else
                        addAddressTxt.setText(getString(R.string.change_address));
                }
                addressHeader.setText(GlobalData.selectedAddress.getType());
                addressDetail.setText(GlobalData.selectedAddress.getMapAddress());
                addressDeliveryTime.setText(""+viewCartItemList.get(0).getProduct().getShop().getEstimatedDeliveryTime() + " Mins");
            } else {
                locationErrorLayout.setVisibility(View.VISIBLE);
                locationInfoLayout.setVisibility(View.GONE);
            }
        } else if (requestCode == ADDRESS_SELECTION && resultCode == Activity.RESULT_CANCELED) {
            System.out.print("CartFragment : Failure");

        }
    }

    @OnClick(R.id.wallet_layout)
    public void onViewClicked() {
    }

    @OnClick(R.id.custom_notes)
    public void onAddCustomNotesClicked() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            final FrameLayout frameView = new FrameLayout(getActivity());
            builder.setView(frameView);

            final AlertDialog alertDialog = builder.create();
            LayoutInflater inflater = alertDialog.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.custom_note_popup, frameView);

            final EditText notes = dialogView.findViewById(R.id.notes);
            notes.setText(customNotes.getText());
            Button submit = dialogView.findViewById(R.id.custom_note_submit);
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customNotes.setText(notes.getText());
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void OPenDialogFloat() {

        try {

            final Dialog   dialogFloating = new Dialog(getActivity());
            dialogFloating.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogFloating.setCancelable(false);
            dialogFloating.setContentView(R.layout.schedule_layout);
            dialogFloating.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialogFloating.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialogFloating.getWindow().setGravity(Gravity.CENTER);
            dialogFloating.setTitle("");
            final TextView selDate,selTime, Apply;
            final Button schedule,cancel;
            schedule = dialogFloating.findViewById(R.id.schedule_request);
            cancel = dialogFloating.findViewById(R.id.cancel);

            selDate = dialogFloating.findViewById(R.id.date);
            selTime = dialogFloating.findViewById(R.id.time);
            selDate.setText(GlobalData.selectedDate);
            selTime.setText(GlobalData.selectedTime);




            selDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    datepick(selDate);

                }
            });
            selTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    timePicker(selTime);

                }
            });


            schedule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isOpen){
                        openAlertCart();
                    }else {
                        if (selDate.getText().toString().isEmpty() || selTime.getText().toString().isEmpty()) {
                            Toast.makeText(getActivity(), R.string.please_select_date_time, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (connectionHelper.isConnectingToInternet()) {
//                    checkOut(GlobalData.getInstance().selectedAddress.getId());
                            checkoutMap = new HashMap<>();
                            checkoutMap.put("user_address_id", "" + GlobalData.selectedAddress.getId());
                            checkoutMap.put("note", "" + customNotes.getText());
                            checkoutMap.put("delivery_date",selDate.getText().toString()+" "+selTime.getText().toString());


                            if (useWalletChkBox.isChecked())
                                checkoutMap.put("wallet", "1");
                            else
                                checkoutMap.put("wallet", "0");

                            dialogFloating.dismiss();
                            startActivity(new Intent(context, AccountPaymentActivity.class).putExtra("is_show_wallet", false).putExtra("is_show_cash", true));
                            activity.overridePendingTransition(R.anim.anim_nothing, R.anim.slide_out_right);
                        } else {
                            Utils.displayMessage(activity, context, getString(R.string.oops_connect_your_internet));
                        }

                    }




                }
            });


            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogFloating.dismiss();


                }
            });
            dialogFloating.show();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    private void scheduleRequest(HashMap<String, String> map) {
        Call<Object> call = apiInterface.sendRequest(map);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {

            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {

            }

        });
    }


    private void datepick(final TextView txtDate) {
        try {
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);


            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            isOpen=true;

                            txtDate.setText(year + "-" + (monthOfYear + 1) + "-" + dayOfMonth);

                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis()-1000);
            datePickerDialog.show();

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void timePicker( final TextView textView){
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);

        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        isOpen=true;
                        textView.setText(hourOfDay + ":" + minute+":"+"00");
                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    private  void openAlertCart(){
        String message = activity.getResources().getString(R.string.reorder_confirm_message);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.replace_cart_item))
                .setMessage(message)
                .setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        clearCart();

                             context.startActivity(new Intent(context, ProductDetailActivity.class));
                            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);


                    }
                })
                .setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                        dialog.dismiss();

                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        nbutton.setTextColor(ContextCompat.getColor(context, R.color.theme));
        nbutton.setTypeface(nbutton.getTypeface(), Typeface.BOLD);
        Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        pbutton.setTextColor(ContextCompat.getColor(context, R.color.theme));
        pbutton.setTypeface(pbutton.getTypeface(), Typeface.BOLD);
    }

    private void clearCart() {
        Call<ClearCart> call = apiInterface.clearCart();
        call.enqueue(new Callback<ClearCart>() {
            @Override
            public void onResponse(Call<ClearCart> call, Response<ClearCart> response) {

                if (response != null && !response.isSuccessful() && response.errorBody() != null) {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(context, jObjError.optString("message"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else if (response.isSuccessful()) {
                    selectedShop = HotelViewActivity.shops;
                    GlobalData.addCart.getProductList().clear();
                    GlobalData.notificationCount = 0;

                }

            }

            @Override
            public void onFailure(Call<ClearCart> call, Throwable t) {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                GlobalData.addCartShopId = selectedShop.getId();
            }
        });

    }


}