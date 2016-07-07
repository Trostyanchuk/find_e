package io.sympli.find_e.ui.fragment;

import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import javax.inject.Inject;

import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.R;
import io.sympli.find_e.databinding.FragmentMapBinding;
import io.sympli.find_e.event.ChangeScreenEvent;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.utils.LocalStorageUtil;

public class MapFragment extends Fragment implements com.mapbox.mapboxsdk.maps.OnMapReadyCallback {

    private static final int ANIMATE_DURATION = 7000;
    private static final int ZOOM = 12;

    private FragmentMapBinding binding;
    private MapView mapFragment;
    private MapboxMap mapboxMap;

    @Inject
    IBroadcast broadcast;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ApplicationController.getComponent().inject(this);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false);

        binding.myLocationLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.setMyLocation(true);
            }
        });
        binding.lastLocationLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.setMyLocation(false);
            }
        });
        binding.done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                broadcast.postEvent(new ChangeScreenEvent(Screen.NONE, ChangeScreenEvent.ScreenGroup.SHADOWING));
            }
        });
        binding.setMyLocation(true);

        mapFragment = binding.mapview;
        mapFragment.onCreate(savedInstanceState);
        mapFragment.getMapAsync(this);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapFragment.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapFragment.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapFragment.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapFragment.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapFragment.onDestroy();
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        MapFragment.this.mapboxMap = mapboxMap;

        LatLng positionToMove = new LatLng(LocalStorageUtil.getLastPosition().latitude,
                LocalStorageUtil.getLastPosition().longitude);

        addRobotMarkerToPosition(positionToMove);
        animateCameraToPosition(positionToMove);
    }

    private void addRobotMarkerToPosition(LatLng positionToMove) {
        IconFactory iconFactory = IconFactory.getInstance(getContext());
        Drawable iconDrawable = ContextCompat.getDrawable(getContext(), R.drawable.robot_smile);
        Icon icon = iconFactory.fromDrawable(iconDrawable);

        // Add the custom icon marker to the map
        mapboxMap.addMarker(new MarkerOptions()
                .position(positionToMove)
                .title("I've lost here")
                .icon(icon));
    }

    private void animateCameraToPosition(LatLng positionToMove) {
        CameraPosition position = new CameraPosition.Builder()
                .target(positionToMove)
                .zoom(ZOOM)
                .tilt(0.0)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), ANIMATE_DURATION);
    }
}
