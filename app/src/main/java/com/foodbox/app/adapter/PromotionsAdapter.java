package com.foodbox.app.adapter;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.foodbox.app.models.Promotions;
import com.foodbox.app.models.Restaurant;
import com.foodbox.app.R;

import java.util.List;

/**
 * Created  on 22-08-2017.
 */

public class PromotionsAdapter extends RecyclerView.Adapter<PromotionsAdapter.MyViewHolder> {
    PromotionListener promotionListener;
    private List<Promotions> list;

    public PromotionsAdapter(List<Promotions> list, PromotionListener promotionListener) {
        this.list = list;
        this.promotionListener = promotionListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.promotions_item, parent, false);

        return new MyViewHolder(itemView);
    }

    public void add(Promotions item, int position) {
        list.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(Restaurant item) {
        int position = list.indexOf(item);
        list.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Promotions promotionsModel = list.get(position);
        holder.promoNameTxt.setText(promotionsModel.getPromoCode());
        holder.statusBtnTxt.setTag(promotionsModel);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface PromotionListener {
        void onApplyBtnClick(Promotions promotions);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView promoNameTxt;
        Button statusBtnTxt;

        private MyViewHolder(View view) {
            super(view);
            promoNameTxt = (TextView) view.findViewById(R.id.promo_name_txt);
            statusBtnTxt = (Button) view.findViewById(R.id.status_btn);

            statusBtnTxt.setOnClickListener(this);
        }

        public void onClick(View v) {
            Promotions promotions = (Promotions) v.getTag();
            promotionListener.onApplyBtnClick(promotions);
        }
    }
}
