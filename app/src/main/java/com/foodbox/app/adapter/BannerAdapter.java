package com.foodbox.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.foodbox.app.models.Banner;
import com.foodbox.app.R;
import com.foodbox.app.activities.HotelViewActivity;
import com.foodbox.app.helper.GlobalData;

import java.util.List;

/**
 * Created  on 22-08-2017.
 */

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.MyViewHolder> {
    Context context;
    Activity activity;
    private List<Banner> list;

    public BannerAdapter(List<Banner> list, Context con, Activity activity) {
        this.list = list;
        this.context = con;
        this.activity = activity;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.impressive_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        Banner banner = list.get(position);

        RequestOptions options = new RequestOptions()
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_banner)
                .error(R.drawable.ic_banner)
                .dontAnimate()
                .priority(Priority.HIGH);

        Glide
                .with(context)
                .load(banner.getUrl())
                .apply(options)
                .into(holder.bannerImg);

        holder.bannerImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Banner banner = list.get(position);
                context.startActivity(new Intent(context, HotelViewActivity.class));
                GlobalData.selectedShop = banner.getShop();
                activity.overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);
                Log.d("Hello", "onItemClick position: " + banner.getShop().getName());
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView bannerImg;


        public MyViewHolder(View view) {
            super(view);
            bannerImg = (ImageView) view.findViewById(R.id.banner_img);

        }


    }


}
