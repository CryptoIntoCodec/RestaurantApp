package com.foodbox.app.fragments;


import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.foodbox.app.adapter.RestaurantsAdapter;
import com.foodbox.app.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.foodbox.app.helper.GlobalData.searchShopList;

/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantSearchFragment extends Fragment implements GoogleMap.OnCameraIdleListener, OnMapReadyCallback, GoogleMap.OnCameraMoveListener {

    public static RestaurantsAdapter restaurantsAdapter;

    Unbinder unbinder;
    Context context;
    private GoogleMap mMap;

    boolean clicked=false;
    //    public static SkeletonScreen skeletonScreen;
    @BindView(R.id.restaurants_rv)
    RecyclerView restaurantsRv;

    FloatingActionButton fab;
    RelativeLayout mapLayout;

    public RestaurantSearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_restaurant, container, false);
        unbinder = ButterKnife.bind(this, view);
        context = getActivity();
      //  fab=view.findViewById(R.id.fab_map);
        mapLayout=view.findViewById(R.id.map_layout);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


/*
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clicked){
                    clicked=false;
                    restaurantsRv.setVisibility(View.VISIBLE);
                    mapLayout.setVisibility(View.GONE);

                }
                else {
                    clicked=true;
                    restaurantsRv.setVisibility(View.GONE);
                    mapLayout.setVisibility(View.VISIBLE);



                }

            }
        });
*/




        return view;
    }

   void setMapMarker(){

        if (searchShopList!=null&&searchShopList.size()>0){
            Log.e("11", "setMapMarker: "+searchShopList.size() );
            for(int i = 0 ; i < searchShopList.size() ; i++) {
                if (mMap!=null)
                createMarker(searchShopList.get(i).getLatitude(), searchShopList.get(i).getLongitude(), searchShopList.get(i).getName(),searchShopList.get(i).getDescription(),R.drawable.ic_restaurant_marker);
            }

            LatLng loc = new LatLng(searchShopList.get(0).getLatitude(), searchShopList.get(0).getLongitude());
            //CameraPosition cameraPosition = new CameraPosition.Builder().target(loc).zoom(20).build();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 20.0f));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 20));
        }


   }

    protected Marker createMarker(double latitude, double longitude, String title, String snippet, int iconResID) {

        return mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .anchor(0.5f, 0.5f)
                .title(title)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.fromResource(iconResID)));
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Restaurant Adapter
        restaurantsRv.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        restaurantsRv.setItemAnimator(new DefaultItemAnimator());
        restaurantsRv.setHasFixedSize(true);
        restaurantsAdapter = new RestaurantsAdapter(searchShopList, context, getActivity());
        restaurantsRv.setAdapter(restaurantsAdapter);


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onCameraIdle() {
        CameraPosition cameraPosition = mMap.getCameraPosition();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        Log.e("11", "setMapMarker: "+searchShopList.size() );
        if (mMap != null) {
            mMap.getUiSettings().setCompassEnabled(false);
            mMap.setBuildingsEnabled(true);
            mMap.setOnCameraMoveListener(this);
            mMap.setOnCameraIdleListener(this);
            mMap.getUiSettings().setRotateGesturesEnabled(false);
            mMap.getUiSettings().setTiltGesturesEnabled(false);

            setMapMarker();
        }

    }

    @Override
    public void onCameraMove() {

    }
}
