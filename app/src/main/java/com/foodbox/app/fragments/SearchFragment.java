package com.foodbox.app.fragments;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.foodbox.app.models.Search;
import com.foodbox.app.HomeActivity;
import com.foodbox.app.R;
import com.foodbox.app.adapter.ProductsAdapter;
import com.foodbox.app.adapter.ViewPagerAdapter;
import com.foodbox.app.build.api.ApiClient;
import com.foodbox.app.build.api.ApiInterface;
import com.foodbox.app.helper.GlobalData;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.foodbox.app.helper.GlobalData.searchProductList;
import static com.foodbox.app.helper.GlobalData.searchShopList;


/**
 * Created  on 22-08-2017.
 */

public class SearchFragment extends Fragment {

    public static EditText searchEt;
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    Unbinder unbinder;
    @BindView(R.id.related_txt)
    TextView relatedTxt;
    ProgressBar progressBar;
    ImageView searchCloseImg;
    @BindView(R.id.root_layout)
    RelativeLayout rootLayout;
    ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
    ViewPagerAdapter adapter;
    String input = "";
    private Context context;
    private ViewGroup toolbar;
    private View toolbarLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        unbinder = ButterKnife.bind(this, view);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        HomeActivity.updateNotificationCount(context, GlobalData.notificationCount);
        if (!input.equalsIgnoreCase("")) {
            HashMap<String, String> map = new HashMap();
            map.put("name", input);
            if (GlobalData.profileModel != null)
                map.put("user_id", GlobalData.profileModel.getId().toString());
            getSearch(map);
        }
        if (ProductsAdapter.bottomSheetDialogFragment != null)
            ProductsAdapter.bottomSheetDialogFragment.dismiss();

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
        if (toolbar != null) {
            toolbar.removeView(toolbarLayout);
        }
        unbinder.unbind();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        System.out.println("SearchFragment");
        searchShopList = new ArrayList<>();
        searchProductList = new ArrayList<>();
        toolbar = (ViewGroup) getActivity().findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);
        rootLayout.setVisibility(View.GONE);
        GlobalData.searchProductList = new ArrayList<>();
        GlobalData.searchShopList = new ArrayList<>();
        toolbarLayout = LayoutInflater.from(context).inflate(R.layout.toolbar_search, toolbar, false);

        getActivity().findViewById(R.id.fab_map).setVisibility(View.GONE);


        searchEt = (EditText) toolbarLayout.findViewById(R.id.search_et);
        progressBar = (ProgressBar) toolbarLayout.findViewById(R.id.progress_bar);
        searchCloseImg = (ImageView) toolbarLayout.findViewById(R.id.search_close_img);
        //ViewPager Adapter
        adapter = new ViewPagerAdapter(getActivity().getSupportFragmentManager());
        adapter.addFragment(new RestaurantSearchFragment(), "RESTAURANT");
        adapter.addFragment(new ProductSearchFragment(), "DISHES");
        viewPager.setOffscreenPageLimit(2);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setAdapter(adapter);
        //set ViewPager
        tabLayout.setupWithViewPager(viewPager);

        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.length() != 0) {
                    input = s.toString();
                    HashMap<String, String> map = new HashMap();
                    map.put("name", s.toString());
                    if (GlobalData.profileModel != null)
                        map.put("user_id", GlobalData.profileModel.getId().toString());
                    getSearch(map);
                    searchCloseImg.setVisibility(View.VISIBLE);
                    rootLayout.setVisibility(View.VISIBLE);
                    relatedTxt.setText("Related to \"" + s.toString() + "\"");
                } else if (s.length() == 0) {
                    relatedTxt.setText("Related to ");
                    searchCloseImg.setVisibility(View.GONE);
                    rootLayout.setVisibility(View.GONE);
                    searchShopList.clear();
                    searchProductList.clear();
                    relatedTxt.setText(s.toString());
                    RestaurantSearchFragment.restaurantsAdapter.notifyDataSetChanged();
                }

            }
        });
        toolbar.addView(toolbarLayout);
        HomeActivity.updateNotificationCount(context, GlobalData.notificationCount);
        searchCloseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEt.setText("");
                searchShopList.clear();
                searchProductList.clear();
                ProductSearchFragment.productsAdapter.notifyDataSetChanged();
                RestaurantSearchFragment.restaurantsAdapter.notifyDataSetChanged();
            }
        });
        Bundle bundle=getArguments();
        if (bundle!=null){
            if (!TextUtils.isEmpty(bundle.getString("value")))
                searchEt.setText(bundle.getString("value"));
        }


    }

    private void getSearch(HashMap map) {
        progressBar.setVisibility(View.VISIBLE);
        Call<Search> call = apiInterface.getSearch(map);

        call.enqueue(new Callback<Search>() {
            @Override
            public void onResponse(Call<Search> call, Response<Search> response) {
                progressBar.setVisibility(View.GONE);
                if (response != null && !response.isSuccessful() && response.errorBody() != null) {

                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(context, jObjError.optString("message"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else if (response.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    searchShopList.clear();
                    searchProductList.clear();
                    searchShopList.addAll(response.body().getShops());
                    searchProductList.addAll(response.body().getProducts());
                    ProductSearchFragment.productsAdapter.notifyDataSetChanged();
                    RestaurantSearchFragment.restaurantsAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onFailure(Call<Search> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });


    }


}
