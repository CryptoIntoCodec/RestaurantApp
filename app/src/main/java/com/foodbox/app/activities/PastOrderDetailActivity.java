package com.foodbox.app.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.foodbox.app.HomeActivity;
import com.foodbox.app.build.api.ApiClient;
import com.foodbox.app.build.api.ApiInterface;
import com.foodbox.app.fragments.OrderViewFragment;
import com.foodbox.app.models.Message;
import com.foodbox.app.models.Order;
import com.foodbox.app.R;
import com.foodbox.app.helper.GlobalData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.foodbox.app.helper.GlobalData.isSelectedOrder;

public class PastOrderDetailActivity extends AppCompatActivity {

    Fragment orderFullViewFragment;
    FragmentManager fragmentManager;
    ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);

    Double priceAmount = 0.0;
    int discount = 0;
    int itemCount = 0;
    int itemQuantity = 0;
    String currency = "";
    @BindView(R.id.order_id_txt)
    TextView orderIdTxt;
    @BindView(R.id.order_item_txt)
    TextView orderItemTxt;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rate_view)
    LinearLayout rateView;
    @BindView(R.id.source_image)
    ImageView sourceImage;
    @BindView(R.id.restaurant_name)
    TextView restaurantName;
    @BindView(R.id.restaurant_address)
    TextView restaurantAddress;
    @BindView(R.id.source_layout)
    RelativeLayout sourceLayout;
    @BindView(R.id.destination_image)
    ImageView destinationImage;
    @BindView(R.id.user_address_title)
    TextView userAddressTitle;
    @BindView(R.id.user_address)
    TextView userAddress;
    @BindView(R.id.destination_layout)
    RelativeLayout destinationLayout;
    @BindView(R.id.view_line2)
    View viewLine2;
    @BindView(R.id.order_succeess_image)
    ImageView orderSucceessImage;
    @BindView(R.id.order_status_txt)
    TextView orderStatusTxt;
    @BindView(R.id.order_status_layout)
    RelativeLayout orderStatusLayout;
    @BindView(R.id.order_detail_fargment)
    FrameLayout orderDetailFargment;
    @BindView(R.id.nested_scroll_view)
    NestedScrollView nestedScrollView;
    @BindView(R.id.dot_line_img)
    ImageView dotLineImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        ButterKnife.bind(this);
        //Toolbar
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView rateShop = (TextView) findViewById(R.id.bt_shop);
        rateShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rate("shop","Rate your food?");

            }
        });

        TextView rateBoy = (TextView) findViewById(R.id.btn_boy);
        rateBoy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                rate("transporter","Rate your delivery?");
            }
        });


        if (isSelectedOrder != null) {
            Order order = GlobalData.isSelectedOrder;
            orderIdTxt.setText("ORDER #000" + order.getId().toString());
            itemQuantity = order.getInvoice().getQuantity();
            priceAmount = order.getInvoice().getPayable();
            if (order.getStatus().equalsIgnoreCase("CANCELLED")) {
                orderStatusTxt.setText(getResources().getString(R.string.order_cancelled));
                orderSucceessImage.setImageResource(R.drawable.order_cancelled_img);
                dotLineImg.setBackgroundResource(R.drawable.order_cancelled_line);
                orderStatusTxt.setTextColor(ContextCompat.getColor(this, R.color.colorRed));
            } else {
                orderStatusTxt.setText(getResources().getString(R.string.order_delivered_successfully_on) + getFormatTime(order.getOrdertiming().get(7).getCreatedAt()));
                orderStatusTxt.setTextColor(ContextCompat.getColor(this, R.color.colorGreen));
                orderSucceessImage.setImageResource(R.drawable.ic_circle_tick);
                dotLineImg.setBackgroundResource(R.drawable.ic_line);
                showRating();
            }
            currency = order.getItems().get(0).getProduct().getPrices().getCurrency();
            if (itemQuantity == 1)
                orderItemTxt.setText(String.valueOf(itemQuantity) + " Item, " + currency + String.valueOf(priceAmount));
            else
                orderItemTxt.setText(String.valueOf(itemQuantity) + " Items, " + currency + String.valueOf(priceAmount));

            restaurantName.setText(order.getShop().getName());
            restaurantAddress.setText(order.getShop().getAddress());
            userAddressTitle.setText(order.getAddress().getType());
            userAddress.setText(order.getAddress().getMapAddress());

            //set Fragment
            orderFullViewFragment = new OrderViewFragment();
            fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.order_detail_fargment, orderFullViewFragment).commit();
        }

    }

    public  void showRating(){
        rateView.setVisibility(View.VISIBLE);
    }

    public void rate(String type,String titleText) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            final FrameLayout frameView = new FrameLayout(this);
            builder.setView(frameView);

            final AlertDialog alertDialog = builder.create();
            LayoutInflater inflater = alertDialog.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.feedback_popup, frameView);
            alertDialog.show();

            final Integer[] rating = {5};
            final RadioGroup rateRadioGroup = (RadioGroup) dialogView.findViewById(R.id.rate_radiogroup);
            ((RadioButton) rateRadioGroup.getChildAt(4)).setChecked(true);
            rateRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    rating[0] = i;
                }
            });

            final EditText comment = (EditText) dialogView.findViewById(R.id.comment);
            final TextView title = (TextView) dialogView.findViewById(R.id.tv_title);
            title.setText(titleText);
            Button feedbackSubmit = (Button) dialogView.findViewById(R.id.feedback_submit);
            feedbackSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (GlobalData.isSelectedOrder != null && GlobalData.isSelectedOrder.getId() != null) {
                        HashMap<String, String> map = new HashMap<>();
                        map.put("order_id", String.valueOf(GlobalData.isSelectedOrder.getId()));
                        map.put("rating", String.valueOf(rating[0]));
                        map.put("comment", comment.getText().toString());
                        map.put("type", type);
                        rateTransporter(map);
                        alertDialog.dismiss();
                    }

                }
            });
            alertDialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void rateTransporter(HashMap<String, String> map) {
        System.out.println(map.toString());
        Call<Message> call = apiInterface.rate(map);
        call.enqueue(new Callback<Message>() {
            @Override
            public void onResponse(@NonNull Call<Message> call, @NonNull Response<Message> response) {
                if (response.errorBody() != null) {
                    finish();
                } else if (response.isSuccessful()) {
                    Message message = response.body();
                    Toast.makeText(PastOrderDetailActivity.this, message.getMessage(), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(PastOrderDetailActivity.this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Message> call, @NonNull Throwable t) {
                Toast.makeText(PastOrderDetailActivity.this, "Something wrong - rateTransporter", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(PastOrderDetailActivity.this, OrdersActivity.class));
                finish();
            }
        });
    }

    private String getFormatTime(String time) {
        System.out.println("Time : " + time);
        String value = "";
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm aa", Locale.getDefault());

            if (time != null) {
                Date date = df.parse(time);
                value = sdf.format(date);
            }

        } catch (ParseException e) {
            e.printStackTrace();

        }
        return value;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.anim_nothing, R.anim.slide_out_right);
    }

//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
//    }
}