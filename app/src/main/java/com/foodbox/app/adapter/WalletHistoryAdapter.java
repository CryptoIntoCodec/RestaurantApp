package com.foodbox.app.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.foodbox.app.helper.GlobalData;
import com.foodbox.app.models.WalletHistory;
import com.foodbox.app.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created  on 22-08-2017.
 */

public class WalletHistoryAdapter extends RecyclerView.Adapter<WalletHistoryAdapter.MyViewHolder> {
    private List<WalletHistory> list;


    public WalletHistoryAdapter(List<WalletHistory> list) {
        this.list = list;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wallet_history_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        WalletHistory walletHistory = list.get(position);
        holder.amountTxt.setText(GlobalData.currencySymbol + " " + walletHistory.getAmount());
        holder.timeTxt.setText(getFormatTime(walletHistory.getCreatedAt()));
        holder.statusTxt.setText(walletHistory.getStatus());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private String getFormatTime(String time) {

        System.out.println("Time : " + time);
        String value = "";
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, hh:mm aaa");

            if (time != null) {
                Date date = df.parse(time);
                value = sdf.format(date);
            }

        } catch (ParseException e) {
            e.printStackTrace();

        }
        return value;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView amountTxt, timeTxt, statusTxt;

        private MyViewHolder(View view) {
            super(view);
            amountTxt = (TextView) view.findViewById(R.id.amount_txt);
            timeTxt = (TextView) view.findViewById(R.id.time_txt);
            statusTxt = (TextView) view.findViewById(R.id.status_txt);
        }
    }

}
