package com.foodbox.app.fragments;


import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.foodbox.app.R;
import com.foodbox.app.adapter.OrderDetailAdapter;
import com.foodbox.app.helper.GlobalData;
import com.foodbox.app.models.Item;
import com.foodbox.app.models.Order;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class OrderDetailFragment extends Fragment {


    @BindView(R.id.order_recycler_view)
    RecyclerView orderRecyclerView;
    Unbinder unbinder;
    Context context = getActivity();
    @BindView(R.id.item_total_amount)
    TextView itemTotalAmount;
    @BindView(R.id.service_tax)
    TextView serviceTax;
    @BindView(R.id.delivery_charges)
    TextView deliveryCharges;
    @BindView(R.id.total_amount)
    TextView totalAmount;
    List<Item> itemList;

    int totalAmountValue = 0;
    int discount = 0;
    int itemCount = 0;
    int itemQuantity = 0;
    String currency = "";
    @BindView(R.id.discount_amount)
    TextView discountAmount;
    @BindView(R.id.wallet_amount_detection)
    TextView walletAmountDetection;


    public OrderDetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_order_detail, container, false);
        unbinder = ButterKnife.bind(this, view);


        Order order = GlobalData.isSelectedOrder;
        //set Item List Values
        itemList = new ArrayList<>();
        if (order != null) {
            itemList.addAll(order.getItems());
            //Offer Restaurant Adapter
            orderRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            orderRecyclerView.setItemAnimator(new DefaultItemAnimator());
            orderRecyclerView.setHasFixedSize(true);
            OrderDetailAdapter orderItemListAdapter = new OrderDetailAdapter(itemList, context);
            orderRecyclerView.setAdapter(orderItemListAdapter);

            currency = order.getItems().get(0).getProduct().getPrices().getCurrency();
            itemQuantity = order.getInvoice().getQuantity();
            itemTotalAmount.setText(currency + order.getInvoice().getGross().toString());
            serviceTax.setText(currency + order.getInvoice().getTax().toString());
            deliveryCharges.setText(currency + order.getInvoice().getDeliveryCharge().toString());
            discountAmount.setText("-" + currency + order.getInvoice().getDiscount().toString());
            walletAmountDetection.setText("-" + currency + order.getInvoice().getWalletAmount().toString());
            totalAmount.setText(currency + String.valueOf(order.getInvoice().getPayable()));
        }


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
