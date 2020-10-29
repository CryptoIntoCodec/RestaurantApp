package com.foodbox.app.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.foodbox.app.activities.FilterActivity;
import com.foodbox.app.activities.HotelViewActivity;
import com.foodbox.app.adapter.BannerAdapter;
import com.foodbox.app.adapter.RestaurantsAdapter;
import com.foodbox.app.models.Banner;
import com.foodbox.app.models.Cuisine;
import com.foodbox.app.HomeActivity;
import com.foodbox.app.R;
import com.foodbox.app.activities.SetDeliveryLocationActivity;
import com.foodbox.app.adapter.DiscoverAdapter;
import com.foodbox.app.adapter.OfferRestaurantAdapter;
import com.foodbox.app.build.api.ApiClient;
import com.foodbox.app.build.api.ApiInterface;
import com.foodbox.app.helper.ConnectionHelper;
import com.foodbox.app.helper.GlobalData;
import com.foodbox.app.models.Address;
import com.foodbox.app.models.Discover;
import com.foodbox.app.models.Restaurant;
import com.foodbox.app.models.RestaurantsData;
import com.foodbox.app.models.Shop;
import com.foodbox.app.utils.Utils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.foodbox.app.helper.GlobalData.addressList;
import static com.foodbox.app.helper.GlobalData.cuisineIdArrayList;
import static com.foodbox.app.helper.GlobalData.cuisineList;
import static com.foodbox.app.helper.GlobalData.isOfferApplied;
import static com.foodbox.app.helper.GlobalData.isPureVegApplied;
import static com.foodbox.app.helper.GlobalData.latitude;
import static com.foodbox.app.helper.GlobalData.longitude;
import static com.foodbox.app.helper.GlobalData.searchShopList;
import static com.foodbox.app.helper.GlobalData.selectedAddress;


/**
 * Created  on 22-08-2017.
 */

public class HomeFragment extends Fragment implements AdapterView.OnItemSelectedListener,GoogleMap.OnCameraIdleListener, OnMapReadyCallback,
        GoogleMap.OnCameraMoveListener,OnMarkerClickListener {

    public static ArrayList<Integer> cuisineSelectedList = null;
    public static boolean isFilterApplied = false;
    private int mYear, mMonth, mDay, mHour, mMinute;

    @BindView(R.id.animation_line_image)
    ImageView animationLineImage;
    Context context;
    @BindView(R.id.catagoery_spinner)
    Spinner catagoerySpinner;
    @BindView(R.id.title)
    LinearLayout title;
    @BindView(R.id.restaurant_count_txt)
    TextView restaurantCountTxt;
    @BindView(R.id.offer_title_header)
    TextView offerTitleHeader;
    @BindView(R.id.error_layout)
    LinearLayout errorLayout;
    @BindView(R.id.impressive_dishes_layout)
    LinearLayout impressiveDishesLayout;
    @BindView(R.id.restaurants_offer_rv)
    RecyclerView restaurantsOfferRv;
    @BindView(R.id.impressive_dishes_rv)
    RecyclerView bannerRv;
    @BindView(R.id.restaurants_rv)
    RecyclerView restaurantsRv;
    @BindView(R.id.discover_rv)
    RecyclerView discoverRv;
    int ADDRESS_SELECTION = 1;
    int FILTER_APPLIED_CHECK = 2;
    ImageView filterSelectionImage;
    AnimatedVectorDrawableCompat avdProgress;
    String[] catagoery = {"Relevance", "Cost for Two", "Delivery Time", "Rating"};
    ApiInterface apiInterface = ApiClient.getRetrofit().create(ApiInterface.class);
    List<Shop> restaurantList;
    RestaurantsAdapter adapterRestaurant;
    BannerAdapter bannerAdapter;
    List<Banner> bannerList;
    ConnectionHelper connectionHelper;
    Activity activity;
    boolean clicked=false;
    private GoogleMap mMap;
    RelativeLayout mapLayout;
    private LatLngBounds.Builder builder;
    private LatLngBounds bounds;
    FloatingActionButton fab;

    Runnable action = new Runnable() {
        @Override
        public void run() {
            avdProgress.stop();
            if (animationLineImage != null)
                animationLineImage.setVisibility(View.INVISIBLE);
        }
    };
    private SkeletonScreen skeletonScreen, skeletonScreen2, skeletonText1, skeletonText2, skeletonSpinner;
    private TextView addressLabel;
    private TextView addressTxt;
    private LinearLayout locationAddressLayout;
    private RelativeLayout errorLoadingLayout;
    private Button filterBtn;
    private ImageView filterCal;
    private ViewGroup toolbar;
    private View toolbarLayout;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d("receiver", "Got message: " + message);

            errorLoadingLayout.setVisibility(View.GONE);
            locationAddressLayout.setVisibility(View.VISIBLE);
            if (selectedAddress != null && GlobalData.profileModel != null) {
                GlobalData.addressHeader = selectedAddress.getType();
                addressLabel.setText(selectedAddress.getType());
                addressTxt.setText(selectedAddress.getMapAddress());
                latitude = selectedAddress.getLatitude();
                longitude = selectedAddress.getLongitude();
                GlobalData.addressHeader = selectedAddress.getMapAddress();
            } else if (addressList != null && addressList.getAddresses().size() != 0 && GlobalData.profileModel != null) {
                for (int i = 0; i < addressList.getAddresses().size(); i++) {
                    Address address1 = addressList.getAddresses().get(i);
                    if (getDoubleThreeDigits(latitude) == getDoubleThreeDigits(address1.getLatitude()) && getDoubleThreeDigits(longitude) == getDoubleThreeDigits(address1.getLongitude())) {
                        selectedAddress = address1;
                        addressLabel.setText(GlobalData.addressHeader);
                        addressTxt.setText(GlobalData.address);
                        addressLabel.setText(selectedAddress.getType());
                        addressTxt.setText(selectedAddress.getMapAddress());
                        latitude = selectedAddress.getLatitude();
                        longitude = selectedAddress.getLongitude();
                        break;
                    } else {
                        addressLabel.setText(GlobalData.addressHeader);
                        addressTxt.setText(GlobalData.address);
                    }
                }
            } else {
                addressLabel.setText(GlobalData.addressHeader);
                addressTxt.setText(GlobalData.address);
            }



            findRestaurant();
        }
    };
    private String dateTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = getContext();
        activity = getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);



        mapLayout=view.findViewById(R.id.map_layout);
        getActivity().findViewById(R.id.fab_map).setVisibility(View.GONE);
        builder = new LatLngBounds.Builder();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);




        return view;

    }

    void setMapMarker(){
        try {



        if (restaurantList!=null&&restaurantList.size()>0){
            Log.e("11", "setMapMarker: "+restaurantList.size() );
            for(int i = 0 ; i < restaurantList.size() ; i++) {
                if (mMap!=null)

                    createMarker(i,restaurantList.get(i).getLatitude(), restaurantList.get(i).getLongitude(), restaurantList.get(i).getName(),restaurantList.get(i).getDescription(),R.drawable.ic_restaurant_marker);
            }

            LatLng loc = new LatLng(restaurantList.get(0).getLatitude(), restaurantList.get(0).getLongitude());
            //CameraPosition cameraPosition = new CameraPosition.Builder().target(loc).zoom(20).build();
          //  mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
          //  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));


            LatLngBounds bounds = builder.build();

            int width = context.getResources().getDisplayMetrics().widthPixels;
            int height = context.getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.30);

            // Zoom and animate the google map to show all markers

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
            mMap.animateCamera(cu);

        }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    protected Marker createMarker(int pos, double latitude, double longitude, String title, String snippet, int iconResID) {
        MarkerOptions markerOptions=new MarkerOptions();
        markerOptions.position(new LatLng(latitude, longitude))
                .anchor(0.5f,0.5f)
                .title(title)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.fromResource(iconResID));

        builder.include(markerOptions.getPosition());
        mMap.addMarker(markerOptions).setTag(pos);

        return  mMap.addMarker(markerOptions);
    }


    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        System.out.println("HomeFragment");







        connectionHelper = new ConnectionHelper(context);
        toolbar = (ViewGroup) getActivity().findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);
        toolbarLayout = LayoutInflater.from(context).inflate(R.layout.toolbar_home, toolbar, false);
        addressLabel = (TextView) toolbarLayout.findViewById(R.id.address_label);
        addressTxt = (TextView) toolbarLayout.findViewById(R.id.address);
        locationAddressLayout = (LinearLayout) toolbarLayout.findViewById(R.id.location_ll);
        errorLoadingLayout = (RelativeLayout) toolbarLayout.findViewById(R.id.error_loading_layout);
        locationAddressLayout.setVisibility(View.INVISIBLE);
        errorLoadingLayout.setVisibility(View.VISIBLE);
        bannerList = new ArrayList<>();
        bannerAdapter = new BannerAdapter(bannerList, context, getActivity());
        bannerRv.setHasFixedSize(true);
        bannerRv.setItemViewCacheSize(20);
        bannerRv.setDrawingCacheEnabled(true);
        bannerRv.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        bannerRv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        bannerRv.setItemAnimator(new DefaultItemAnimator());
        skeletonScreen2 = Skeleton.bind(bannerRv)
                .adapter(bannerAdapter)
                .load(R.layout.skeleton_impressive_list_item)
                .count(3)
                .show();
        skeletonText1 = Skeleton.bind(offerTitleHeader)
                .load(R.layout.skeleton_label)
                .show();
        skeletonText2 = Skeleton.bind(restaurantCountTxt)
                .load(R.layout.skeleton_label)
                .show();
        skeletonSpinner = Skeleton.bind(catagoerySpinner)
                .load(R.layout.skeleton_label)
                .show();
        HomeActivity.updateNotificationCount(context, GlobalData.notificationCount);

        //Spinner
        //Creating the ArrayAdapter instance having the country shopList
        ArrayAdapter aa = new ArrayAdapter(context, R.layout.spinner_layout, catagoery);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        catagoerySpinner.setAdapter(aa);
        catagoerySpinner.setOnItemSelectedListener(this);

        //Restaurant Adapter
        restaurantsRv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        restaurantsRv.setItemAnimator(new DefaultItemAnimator());
        restaurantsRv.setHasFixedSize(true);
        restaurantList = new ArrayList<>();
        adapterRestaurant = new RestaurantsAdapter(restaurantList, context, getActivity());
        skeletonScreen = Skeleton.bind(restaurantsRv)
                .adapter(adapterRestaurant)
                .load(R.layout.skeleton_restaurant_list_item)
                .count(2)
                .show();

        final ArrayList<Restaurant> offerRestaurantList = new ArrayList<>();
        offerRestaurantList.add(new Restaurant("Madras Coffee House", "Cafe, South Indian", "", "3.8", "51 Mins", "$20", ""));
        offerRestaurantList.add(new Restaurant("Behrouz Biryani", "Biriyani", "", "3.7", "52 Mins", "$50", ""));
        offerRestaurantList.add(new Restaurant("SubWay", "American fast food", "Flat 20% offer on all orders", "4.3", "30 Mins", "$5", "Close soon"));
        offerRestaurantList.add(new Restaurant("Dominoz Pizza", "Pizza shop", "", "4.5", "25 Mins", "$5", ""));
        offerRestaurantList.add(new Restaurant("Pizza hut", "Cafe, Bakery", "", "4.1", "45 Mins", "$5", "Close soon"));
        offerRestaurantList.add(new Restaurant("McDonlad's", "Pizza Food, burger", "Flat 20% offer on all orders", "4.6", "20 Mins", "$5", ""));
        offerRestaurantList.add(new Restaurant("Chai Kings", "Cafe, Bakery", "", "3.3", "36 Mins", "$5", ""));
        offerRestaurantList.add(new Restaurant("sea sell", "Fish, Chicken, mutton", "Flat 30% offer on all orders", "4.3", "20 Mins", "$5", "Close soon"));
        //Offer Restaurant Adapter
        restaurantsOfferRv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        restaurantsOfferRv.setItemAnimator(new DefaultItemAnimator());
        restaurantsOfferRv.setHasFixedSize(true);
        OfferRestaurantAdapter offerAdapter = new OfferRestaurantAdapter(offerRestaurantList, context);
        restaurantsOfferRv.setAdapter(offerAdapter);


        // Discover
        final List<Discover> discoverList = new ArrayList<>();
        discoverList.add(new Discover("Trending now ", "22 options", "1"));
        discoverList.add(new Discover("Offers near you", "51 options", "1"));
        discoverList.add(new Discover("Whats special", "7 options", "1"));
        discoverList.add(new Discover("Pocket Friendly", "44 options", "1"));

        DiscoverAdapter adapterDiscover = new DiscoverAdapter(discoverList, context);
        discoverRv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        discoverRv.setItemAnimator(new DefaultItemAnimator());
        discoverRv.setAdapter(adapterDiscover);
        adapterDiscover.setOnItemClickListener(new DiscoverAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Discover obj = discoverList.get(position);
            }
        });

        locationAddressLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (GlobalData.profileModel != null) {
                    startActivityForResult(new Intent(getActivity(), SetDeliveryLocationActivity.class).putExtra("get_address", true).putExtra("home_page", true), ADDRESS_SELECTION);
                    getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);
                } else {
                    Toast.makeText(context, "Please login", Toast.LENGTH_SHORT).show();
                }
            }
        });
        filterBtn = (Button) toolbarLayout.findViewById(R.id.filter);
        filterCal= (ImageView) toolbarLayout.findViewById(R.id.filter_calender);
        filterSelectionImage = (ImageView) toolbarLayout.findViewById(R.id.filter_selection_image);
        filterBtn.setOnClickListener(v -> {
            startActivityForResult(new Intent(context, FilterActivity.class), FILTER_APPLIED_CHECK);
            getActivity().overridePendingTransition(R.anim.slide_up, R.anim.anim_nothing);
        });
        filterCal.setOnClickListener(v -> {
            datepick();
        });
        toolbar.addView(toolbarLayout);
        //intialize image line
        initializeAvd();

//Get cuisine values
        if (connectionHelper.isConnectingToInternet()) {
            getCuisines();
        } else {
            Utils.displayMessage(activity, context, getString(R.string.oops_connect_your_internet));
        }


    }

    private void datepick() {
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
                            timePicker();
                            Time chosenDate = new Time();
                            chosenDate.set(dayOfMonth, monthOfYear, year);
                            long dtDob = chosenDate.toMillis(true);


                           GlobalData.selectedDay= (String) DateFormat.format("EEE", dtDob);
                            Toast.makeText(context, ""+GlobalData.selectedDay, Toast.LENGTH_SHORT).show();
                           dateTime=  year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                           GlobalData.selectedDate=dateTime;

                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis()-1000);
            datePickerDialog.show();

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void timePicker( ){
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

                        GlobalData.selectedTime = dateTime+" "+hourOfDay + ":" + minute;
                        Toast.makeText(context, ""+GlobalData.selectedTime, Toast.LENGTH_SHORT).show();

                    }
                }, mHour, mMinute, false);
        timePickerDialog.show();
    }
    private void findRestaurant() {
        HashMap<String, String> map = new HashMap<>();
        map.put("latitude", String.valueOf(latitude));
        map.put("longitude", String.valueOf(longitude));
        //get User Profile Data
        if (GlobalData.profileModel != null) {
            map.put("user_id", String.valueOf(GlobalData.profileModel.getId()));
        }
        if (isFilterApplied) {
            filterSelectionImage.setVisibility(View.VISIBLE);
            if (isOfferApplied)
                map.put("offer", "1");
            if (isPureVegApplied)
                map.put("pure_veg", "1");
            if (cuisineIdArrayList != null && cuisineIdArrayList.size() != 0) {
                for (int i = 0; i < cuisineIdArrayList.size(); i++) {
                    map.put("cuisine[" + "" + i + "]", cuisineIdArrayList.get(i).toString());
                }
            }

        } else {
            filterSelectionImage.setVisibility(View.GONE);
        }

        if (connectionHelper.isConnectingToInternet()) {
            getRestaurant(map);
        } else {
            Utils.displayMessage(activity, context, getString(R.string.oops_connect_your_internet));
        }

    }

    private void getRestaurant(HashMap<String, String> map) {
        Call<RestaurantsData> call = apiInterface.getshops(map);
        call.enqueue(new Callback<RestaurantsData>() {
            @Override
            public void onResponse(@NonNull Call<RestaurantsData> call, @NonNull Response<RestaurantsData> response) {
                skeletonScreen.hide();
                skeletonScreen2.hide();
                skeletonText1.hide();
                skeletonText2.hide();
                skeletonSpinner.hide();

                if (response.isSuccessful()) {
                    if (response.body().getShops().size() == 0) {
                        title.setVisibility(View.GONE);
                        errorLayout.setVisibility(View.VISIBLE);
                    } else {
                        title.setVisibility(View.VISIBLE);
                        errorLayout.setVisibility(View.GONE);
                    }

                    //Check Banner list
                    if (response.body().getBanners().size() == 0 || isFilterApplied)
                        impressiveDishesLayout.setVisibility(View.GONE);
                    else
                        impressiveDishesLayout.setVisibility(View.VISIBLE);
                    GlobalData.shopList = response.body().getShops();
                    restaurantList.clear();
                    restaurantList.addAll(GlobalData.shopList);
                    bannerList.clear();
                    bannerList.addAll(response.body().getBanners());
                    restaurantCountTxt.setText("" + restaurantList.size() + " Restaurants");
                    adapterRestaurant.notifyDataSetChanged();
                    bannerAdapter.notifyDataSetChanged();
                    setMapMarker();

                } else {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(context, jObjError.optString("message"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<RestaurantsData> call, @NonNull Throwable t) {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();

            }
        });


    }

    public void getCuisines() {
        Call<List<Cuisine>> call = apiInterface.getcuCuisineCall();
        call.enqueue(new Callback<List<Cuisine>>() {
            @Override
            public void onResponse(Call<List<Cuisine>> call, Response<List<Cuisine>> response) {
                if (response != null && !response.isSuccessful() && response.errorBody() != null) {
                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(context, jObjError.optString("message"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else if (response.isSuccessful()) {
                    cuisineList = new ArrayList<>();
                    cuisineList.addAll(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Cuisine>> call, Throwable t) {

            }
        });


    }

    public double getDoubleThreeDigits(Double value) {
        return new BigDecimal(value.toString()).setScale(3, RoundingMode.HALF_UP).doubleValue();

    }

    private void initializeAvd() {
        avdProgress = AnimatedVectorDrawableCompat.create(context, R.drawable.avd_line);
        animationLineImage.setBackground(avdProgress);
        repeatAnimation();
    }

    private void repeatAnimation() {
        avdProgress.start();
        animationLineImage.
                postDelayed(action, 3000); // Will repeat animation in every 1 second
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        errorLayout.setVisibility(View.GONE);
        HomeActivity.updateNotificationCount(context, GlobalData.notificationCount);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("location"));
        if (!GlobalData.addressHeader.equalsIgnoreCase("")) {
            errorLoadingLayout.setVisibility(View.GONE);
            locationAddressLayout.setVisibility(View.VISIBLE);
            if (selectedAddress != null && GlobalData.profileModel != null) {
                GlobalData.addressHeader = selectedAddress.getType();
                addressLabel.setText(selectedAddress.getType());
                addressTxt.setText(selectedAddress.getMapAddress());
                latitude = selectedAddress.getLatitude();
                longitude = selectedAddress.getLongitude();
                GlobalData.addressHeader = selectedAddress.getMapAddress();
            } else if (addressList != null && addressList.getAddresses().size() != 0 && GlobalData.profileModel != null) {
                for (int i = 0; i < addressList.getAddresses().size(); i++) {
                    Address address1 = addressList.getAddresses().get(i);
                    if (getDoubleThreeDigits(latitude) == getDoubleThreeDigits(address1.getLatitude()) && getDoubleThreeDigits(longitude) == getDoubleThreeDigits(address1.getLongitude())) {
                        selectedAddress = address1;
                        addressLabel.setText(GlobalData.addressHeader);
                        addressTxt.setText(GlobalData.address);
                        addressLabel.setText(selectedAddress.getType());
                        addressTxt.setText(selectedAddress.getMapAddress());
                        latitude = selectedAddress.getLatitude();
                        longitude = selectedAddress.getLongitude();
                        break;
                    } else {
                        addressLabel.setText(GlobalData.addressHeader);
                        addressTxt.setText(GlobalData.address);
                    }
                }
            } else {
                addressLabel.setText(GlobalData.addressHeader);
                addressTxt.setText(GlobalData.address);
            }
            findRestaurant();

        }

    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        activity = getActivity();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (toolbar != null) {
            toolbar.removeView(toolbarLayout);
        }
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        super.onDestroy();

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADDRESS_SELECTION && resultCode == Activity.RESULT_OK) {
            System.out.print("HomeFragment : Success");
            if (selectedAddress != null) {
                addressLabel.setText(GlobalData.addressHeader);
                addressTxt.setText(GlobalData.address);
                addressLabel.setText(selectedAddress.getType());
                addressTxt.setText(selectedAddress.getMapAddress());
                latitude = selectedAddress.getLatitude();
                longitude = selectedAddress.getLongitude();
                skeletonScreen.show();
                skeletonScreen2.show();
                skeletonText1.show();
                skeletonText2.show();
                skeletonSpinner.show();
                findRestaurant();

            }
        } else if (requestCode == ADDRESS_SELECTION && resultCode == Activity.RESULT_CANCELED) {
            System.out.print("HomeFragment : Failure");

        }

        if (requestCode == FILTER_APPLIED_CHECK && resultCode == Activity.RESULT_OK) {
            System.out.print("HomeFragment : Filter Success");
            skeletonScreen.show();
            skeletonScreen2.show();
            skeletonText1.show();
            skeletonText2.show();
            skeletonSpinner.show();
            findRestaurant();

        } else if (requestCode == ADDRESS_SELECTION && resultCode == Activity.RESULT_CANCELED) {
            System.out.print("HomeFragment : Filter Failure");

        }

    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//        Toast.makeText(context, catagoery[position], Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onCameraIdle() {

    }

    @Override
    public void onCameraMove() {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
         if (mMap != null) {
            mMap.getUiSettings().setCompassEnabled(false);
            mMap.setBuildingsEnabled(true);
            mMap.setOnCameraMoveListener(this);
            mMap.setOnCameraIdleListener(this);
            mMap.getUiSettings().setRotateGesturesEnabled(false);
            mMap.getUiSettings().setTiltGesturesEnabled(false);

            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    int pos = (int) marker.getTag();
                  //  Toast.makeText(context, "hello", Toast.LENGTH_SHORT).show();
                    if (!restaurantList.get(pos).getShopstatus().equalsIgnoreCase("CLOSED")){
                        context.startActivity(new Intent(context, HotelViewActivity.class).putExtra("position", pos));
                        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);

                    }else {
                        Toast.makeText(context, "The Shop is closed", Toast.LENGTH_SHORT).show();
                    }

                  ///  activity.overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);

                    //callRes(pos);

                }
            });




/*
             mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                 @Override
                 public boolean onMarkerClick(Marker marker) {
                     String locAddress = marker.getTitle();
                     context.startActivity(new Intent(context, HotelViewActivity.class).putExtra("position", 0 ));
                     activity.overridePendingTransition(R.anim.slide_in_right, R.anim.anim_nothing);

                     return true;
                 }
             });
*/


         }

    }

    void callRes(int pos){

        Intent intent= new Intent(context, HotelViewActivity.class);
        intent.putExtra("position", pos );
        context.startActivity(intent);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {


        return false;
    }
}