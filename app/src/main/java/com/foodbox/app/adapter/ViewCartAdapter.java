package com.foodbox.app.adapter;

import android.app.Dialog;
import android.content.Context;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.foodbox.app.R;
import com.foodbox.app.build.api.ApiClient;
import com.foodbox.app.build.api.ApiInterface;
import com.foodbox.app.fragments.AddonBottomSheetFragment;
import com.foodbox.app.fragments.CartChoiceModeFragment;
import com.foodbox.app.fragments.CartFragment;
import com.foodbox.app.helper.GlobalData;
import com.foodbox.app.models.AddCart;
import com.foodbox.app.models.Cart;
import com.foodbox.app.models.CartAddon;
import com.foodbox.app.models.Product;
import com.foodbox.app.models.Shop;
import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created  on 22-08-2017.
 */

public class ViewCartAdapter extends RecyclerView.Adapter<ViewCartAdapter.MyViewHolder> {
    //Animation number
    private static final char[] NUMBER_LIST = TickerUtils.getDefaultNumberList();
    public static Context context;
    public static double priceAmount = 0;
    public static double discount = 0;
    public static int itemCount = 0;
    public static int itemQuantity = 0;
    public static Product product;
    public static boolean dataResponse = true;
    public static Cart productList;
    public static ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
    public static AddCart addCart;
    public static AnimatedVectorDrawableCompat avdProgress;
    public static Dialog dialog;
    public static Runnable action;
    public static Shop selectedShop = GlobalData.selectedShop;
    public static CartChoiceModeFragment bottomSheetDialogFragment;
    private List<Cart> list;

    public ViewCartAdapter(List<Cart> list, Context con) {
        this.list = list;
        context = con;
    }

    public static void addCart(HashMap<String, String> map) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.empty_dialog);
        dialog.setCancelable(false);
        dataResponse = false;
        dialog.show();
        Call<AddCart> call = apiInterface.postAddCart(map);
        call.enqueue(new Callback<AddCart>() {
            @Override
            public void onResponse(Call<AddCart> call, Response<AddCart> response) {
                avdProgress.stop();
                dialog.dismiss();
                dataResponse = true;
                if (response != null && !response.isSuccessful() && response.errorBody() != null)
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(context, jObjError.optString("message"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                else if (response.isSuccessful()) {
                    addCart = response.body();
                    GlobalData.addCart = new AddCart();
                    GlobalData.addCart = response.body();
                    CartFragment.viewCartItemList.clear();
                    CartFragment.viewCartItemList.addAll(response.body().getProductList());
                    CartFragment.viewCartAdapter.notifyDataSetChanged();
                    priceAmount = 0;
                    discount = 0;
                    itemQuantity = 0;
                    itemCount = 0;
                    //get Item Count
                    itemCount = addCart.getProductList().size();
                    if (itemCount != 0) {
                        for (int i = 0; i < itemCount; i++) {
                            //Get Total item Quantity
                            itemQuantity = itemQuantity + addCart.getProductList().get(i).getQuantity();
                            //Get addon price
                            if (addCart.getProductList().get(i).getProduct().getPrices().getPrice() != null)
                                priceAmount = priceAmount + (addCart.getProductList().get(i).getQuantity() * addCart.getProductList().get(i).getProduct().getPrices().getPrice());
                            if (addCart.getProductList().get(i).getCartAddons() != null && !addCart.getProductList().get(i).getCartAddons().isEmpty())
                                for (int j = 0; j < addCart.getProductList().get(i).getCartAddons().size(); j++)
                                    priceAmount = priceAmount + (addCart.getProductList().get(i).getQuantity()
                                            * (addCart.getProductList().get(i).getCartAddons().get(j).getQuantity()
                                            * addCart.getProductList().get(i).getCartAddons().get(j).getAddonProduct().getPrice()));
                        }
                        if (response.body().getProductList().get(0).getProduct().getShop().getOfferMinAmount() != null)
                            if (response.body().getProductList().get(0).getProduct().getShop().getOfferMinAmount() < priceAmount) {
                                int offerPercentage = response.body().getProductList().get(0).getProduct().getShop().getOfferPercent();
                                discount = (int) (priceAmount * (offerPercentage * 0.01));
                            }
                        GlobalData.notificationCount = itemQuantity;

                        //      Set Payment details
                        String currency = addCart.getProductList().get(0).getProduct().getPrices().getCurrency();

                        //      RRR item total
                        CartFragment.itemTotalAmount.setText(currency + "" + priceAmount);

                        //      RRR IGST
                        double itemTotalIGST;
                        double igstVal = priceAmount * response.body().getTaxPercentage() / 100;
                        System.out.println("RRR Tax Amt = " + igstVal);

//                        itemTotalIGST = priceAmount + Math.round(igstVal);
//                        CartFragment.serviceTax.setText(currency + String.valueOf(Math.round(igstVal)));
                        itemTotalIGST = priceAmount + igstVal;
                        CartFragment.serviceTax.setText(currency + igstVal);

                        //      RRR Discount
                        double itemTotalDiscount;
                        if (response.body().getProductList().get(0).getProduct().getShop().getOfferMinAmount() != null)
                            if (response.body().getProductList().get(0).getProduct().getShop().getOfferMinAmount() < priceAmount) {
                                int offerPercentage = response.body().getProductList().get(0).getProduct().getShop().getOfferPercent();
                                discount = (itemTotalIGST * offerPercentage) / 100;
                            }
                        itemTotalDiscount = itemTotalIGST - discount;
                        CartFragment.discountAmount.setText("- " + currency + "" + discount);

                        //      RRR Total Payable Amount
                        double topPayAmount = itemTotalDiscount + response.body().getDeliveryCharges();
                        CartFragment.payAmount.setText(currency + "" + Math.round(topPayAmount));

                    } else {
                        GlobalData.notificationCount = itemQuantity;
                        CartFragment.errorLayout.setVisibility(View.VISIBLE);
                        CartFragment.dataLayout.setVisibility(View.GONE);
                        Toast.makeText(context, "Cart is empty", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onFailure(Call<AddCart> call, Throwable t) {

            }
        });

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_product_item, parent, false);

        return new MyViewHolder(itemView);
    }

    public void add(Cart item, int position) {
        list.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(Cart item) {
        int position = list.indexOf(item);
        list.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.cardAddTextLayout.setVisibility(View.GONE);
        holder.cardAddDetailLayout.setVisibility(View.VISIBLE);
        product = list.get(position).getProduct();
        holder.cardTextValueTicker.setCharacterList(NUMBER_LIST);
        holder.dishNameTxt.setText(product.getName());
        holder.cardTextValue.setText(list.get(position).getQuantity().toString());
        holder.cardTextValueTicker.setText(list.get(position).getQuantity().toString());
        priceAmount = list.get(position).getQuantity() * product.getPrices().getPrice();
        if (list.get(position).getCartAddons() != null && !list.get(position).getCartAddons().isEmpty())
            for (int j = 0; j < list.get(position).getCartAddons().size(); j++)
                priceAmount = priceAmount + (list.get(position).getQuantity()
                        * (list.get(position).getCartAddons().get(j).getQuantity()
                        * list.get(position).getCartAddons().get(j).getAddonProduct().getPrice()));
        holder.priceTxt.setText(product.getPrices().getCurrency() + " " + priceAmount);
        if (!product.getFoodType().equalsIgnoreCase("veg"))
            holder.foodImageType.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_nonveg));
        else
            holder.foodImageType.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_veg));
        selectedShop = product.getShop();

        if (product.getAddons().size() > 0) {
            holder.customize.setVisibility(View.VISIBLE);
            holder.addons.setVisibility(View.VISIBLE);
        } else {
            holder.customize.setVisibility(View.GONE);
            holder.addons.setVisibility(View.GONE);
        }

        List<CartAddon> cartAddonList = list.get(position).getCartAddons();
        if (cartAddonList.isEmpty()) holder.addons.setText("");
        else for (int i = 0; i < cartAddonList.size(); i++)
            if (i == 0)
                holder.addons.setText(cartAddonList.get(i).getAddonProduct().getAddon().getName());
            else
                holder.addons.append(", " + cartAddonList.get(i).getAddonProduct().getAddon().getName());


        holder.cardAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("access_token2", GlobalData.accessToken);
                /** Intilaize Animation View Image */
                holder.animationLineCartAdd.setVisibility(View.VISIBLE);
                //Intialize
                avdProgress = AnimatedVectorDrawableCompat.create(context, R.drawable.add_cart_avd_line);
                holder.animationLineCartAdd.setBackground(avdProgress);
                avdProgress.start();
                action = new Runnable() {
                    @Override
                    public void run() {
                        if (!dataResponse) {
                            avdProgress.start();
                            holder.animationLineCartAdd.postDelayed(action, 3000);
                        }

                    }
                };
                holder.animationLineCartAdd.postDelayed(action, 3000);
                /** Press Add Card Add button */
                product = list.get(position).getProduct();
                if (product.getAddons() != null && product.getAddons().size() > 0) {
//                    if (product.getAddons() != null && !product.getAddons().isEmpty()) {
                    GlobalData.isSelectedProduct = product;
                    CartChoiceModeFragment.lastCart = list.get(position);
                    bottomSheetDialogFragment = new CartChoiceModeFragment();
                    bottomSheetDialogFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
                    CartChoiceModeFragment.isViewcart = true;
                    CartChoiceModeFragment.isSearch = false;
                } else {
                    int countValue = Integer.parseInt(holder.cardTextValue.getText().toString()) + 1;
                    holder.cardTextValue.setText("" + countValue);
                    holder.cardTextValueTicker.setText("" + countValue);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("product_id", product.getId().toString());
                    map.put("quantity", holder.cardTextValue.getText().toString());
                    map.put("cart_id", String.valueOf(list.get(position).getId()));
                    Log.e("AddCart_add", map.toString());
                    addCart(map);
                    int quantity = Integer.parseInt(holder.cardTextValue.getText().toString());
                    priceAmount = quantity * product.getPrices().getPrice();
                    if (list.get(position).getCartAddons() != null && !list.get(position).getCartAddons().isEmpty())
                        for (int j = 0; j < list.get(position).getCartAddons().size(); j++)
                            priceAmount = priceAmount + (list.get(position).getQuantity()
                                    * (list.get(position).getCartAddons().get(j).getQuantity()
                                    * list.get(position).getCartAddons().get(j).getAddonProduct().getPrice()));
                    holder.priceTxt.setText(product.getPrices().getCurrency() + " " + priceAmount);
                }
            }
        });

        holder.cardMinusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /** Intilaize Animation View Image */
                holder.animationLineCartAdd.setVisibility(View.VISIBLE);
                //Intialize
                avdProgress = AnimatedVectorDrawableCompat.create(context, R.drawable.add_cart_avd_line);
                holder.animationLineCartAdd.setBackground(avdProgress);
                avdProgress.start();
                action = new Runnable() {
                    @Override
                    public void run() {
                        if (!dataResponse) {
                            avdProgress.start();
                            holder.animationLineCartAdd.postDelayed(action, 3000);
                        }
                    }
                };
                holder.animationLineCartAdd.postDelayed(action, 3000);
                int countMinusValue;
                /** Press Add Card Minus button */
                product = list.get(position).getProduct();
                int quantity = Integer.parseInt(holder.cardTextValue.getText().toString());
                priceAmount = quantity * product.getPrices().getPrice();
                if (list.get(position).getCartAddons() != null && !list.get(position).getCartAddons().isEmpty())
                    for (int j = 0; j < list.get(position).getCartAddons().size(); j++)
                        priceAmount = priceAmount + (list.get(position).getQuantity()
                                * (list.get(position).getCartAddons().get(j).getQuantity()
                                * list.get(position).getCartAddons().get(j).getAddonProduct().getPrice()));
                holder.priceTxt.setText(product.getPrices().getCurrency() + " " + priceAmount);
                if (holder.cardTextValue.getText().toString().equalsIgnoreCase("1")) {
                    countMinusValue = Integer.parseInt(holder.cardTextValue.getText().toString()) - 1;
                    holder.cardTextValue.setText("" + countMinusValue);
                    holder.cardTextValueTicker.setText("" + countMinusValue);
                    productList = list.get(position);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("product_id", product.getId().toString());
                    map.put("quantity", String.valueOf(countMinusValue));
                    map.put("cart_id", String.valueOf(list.get(position).getId()));
                    List<CartAddon> cartAddonList = list.get(position).getCartAddons();
                    for (int i = 0; i < cartAddonList.size(); i++) {
                        CartAddon cartAddon = cartAddonList.get(i);
                        map.put("product_addons[" + "" + i + "]", cartAddon.getAddonProduct().getId().toString());
                        map.put("addons_qty[" + "" + i + "]", cartAddon.getQuantity().toString());
                    }
                    Log.e("AddCart_Minus", map.toString());
                    addCart(map);
                    remove(productList);

                } else {
                    countMinusValue = Integer.parseInt(holder.cardTextValue.getText().toString()) - 1;
                    holder.cardTextValue.setText("" + countMinusValue);
                    holder.cardTextValueTicker.setText("" + countMinusValue);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("product_id", product.getId().toString());
                    map.put("quantity", String.valueOf(countMinusValue));
                    map.put("cart_id", String.valueOf(list.get(position).getId()));
                    List<CartAddon> cartAddonList = list.get(position).getCartAddons();
                    for (int i = 0; i < cartAddonList.size(); i++) {
                        CartAddon cartAddon = cartAddonList.get(i);
                        map.put("product_addons[" + "" + i + "]", cartAddon.getAddonProduct().getId().toString());
                        map.put("addons_qty[" + "" + i + "]", cartAddon.getQuantity().toString());
                    }
                    Log.e("AddCart_Minus", map.toString());
                    addCart(map);
                }
            }
        });

        holder.customize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /** Intilaize Animation View Image */
                holder.animationLineCartAdd.setVisibility(View.VISIBLE);
                //Intialize
                avdProgress = AnimatedVectorDrawableCompat.create(context, R.drawable.add_cart_avd_line);
                holder.animationLineCartAdd.setBackground(avdProgress);
                avdProgress.start();
                action = new Runnable() {
                    @Override
                    public void run() {
                        if (!dataResponse) {
                            avdProgress.start();
                            holder.animationLineCartAdd.postDelayed(action, 3000);
                        }

                    }
                };
                holder.animationLineCartAdd.postDelayed(action, 3000);
                productList = list.get(position);
                GlobalData.isSelectedProduct = product;
                GlobalData.isSelctedCart = productList;
                GlobalData.cartAddons = productList.getCartAddons();
                AddonBottomSheetFragment bottomSheetDialogFragment = new AddonBottomSheetFragment();
                bottomSheetDialogFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
                AddonBottomSheetFragment.selectedCart = list.get(position);
                // Right here!

            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TickerView cardTextValueTicker;
        RelativeLayout cardAddDetailLayout, cardAddTextLayout, cardInfoLayout;
        private ImageView dishImg, foodImageType, cardAddBtn, cardMinusBtn, animationLineCartAdd;
        private TextView dishNameTxt, priceTxt, cardTextValue, cardAddInfoText, cardAddOutOfStock, customizableTxt, addons, customize;

        private MyViewHolder(View view) {
            super(view);
            foodImageType = (ImageView) itemView.findViewById(R.id.food_type_image);
            animationLineCartAdd = (ImageView) itemView.findViewById(R.id.animation_line_cart_add);
            dishNameTxt = (TextView) itemView.findViewById(R.id.dish_name_text);
            priceTxt = (TextView) itemView.findViewById(R.id.price_text);
            customizableTxt = (TextView) itemView.findViewById(R.id.customizable_txt);
            addons = (TextView) itemView.findViewById(R.id.addons);
            customize = (TextView) itemView.findViewById(R.id.customize);
            /*    Add card Button Layout*/
            cardAddDetailLayout = (RelativeLayout) itemView.findViewById(R.id.add_card_layout);
            cardAddTextLayout = (RelativeLayout) itemView.findViewById(R.id.add_card_text_layout);
            cardAddInfoText = (TextView) itemView.findViewById(R.id.avialablity_time);
            cardAddOutOfStock = (TextView) itemView.findViewById(R.id.out_of_stock);
            cardAddBtn = (ImageView) itemView.findViewById(R.id.card_add_btn);
            cardMinusBtn = (ImageView) itemView.findViewById(R.id.card_minus_btn);
            cardTextValue = (TextView) itemView.findViewById(R.id.card_value);
            cardTextValueTicker = (TickerView) itemView.findViewById(R.id.card_value_ticker);
        }
    }


}
