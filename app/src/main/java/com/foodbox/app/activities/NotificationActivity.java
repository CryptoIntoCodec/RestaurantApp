package com.foodbox.app.activities;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import com.foodbox.app.models.NotificationItem;
import com.foodbox.app.R;
import com.foodbox.app.adapter.NotificationAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class NotificationActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.notification_rv)
    RecyclerView notificationRv;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        ButterKnife.bind(this);
        context = NotificationActivity.this;
        //Toolbar
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setContentInsetsAbsolute(toolbar.getContentInsetLeft(), 0);

        final ArrayList<NotificationItem> notificationList = new ArrayList<>();
        notificationList.add(new NotificationItem("10% offer for veg orders", "Sep 08,2017", "Use Code AD123"));
        notificationList.add(new NotificationItem("5% offer for Non-veg orders", "Sep 15,2017", "Use Code NV124"));
        //Offer Restaurant Adapter
        notificationRv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        notificationRv.setItemAnimator(new DefaultItemAnimator());
        notificationRv.setHasFixedSize(true);
        NotificationAdapter orderItemListAdapter = new NotificationAdapter(notificationList, context);
        notificationRv.setAdapter(orderItemListAdapter);

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
