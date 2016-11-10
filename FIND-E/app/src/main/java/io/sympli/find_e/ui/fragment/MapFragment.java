package io.sympli.find_e.ui.fragment;

import android.Manifest;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.UiThread;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.directions.DirectionsCriteria;
import com.mapbox.directions.MapboxDirections;
import com.mapbox.directions.service.models.DirectionsResponse;
import com.mapbox.directions.service.models.DirectionsRoute;
import com.mapbox.directions.service.models.Waypoint;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.sympli.find_e.ApplicationController;
import io.sympli.find_e.R;
import io.sympli.find_e.databinding.FragmentMapBinding;
import io.sympli.find_e.event.ChangeScreenEvent;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.utils.DateUtil;
import io.sympli.find_e.utils.LocalStorageUtil;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class MapFragment extends Fragment implements com.mapbox.mapboxsdk.maps.OnMapReadyCallback {

    private static final long START_SEARCH_TIMEOUT = TimeUnit.SECONDS.toMillis(2);

    private static final int ANIMATE_DURATION = 5000;
    private static final int ZOOM = 16;

    private FragmentMapBinding binding;
    private MapView mapFragment;
    private MapboxMap mapboxMap;
    private LocationServices locationServices;
    private LocationManager locationManager;
    private LatLng myLocation;
    private LatLng lastPointLocation;
    private Date lastDate;
    private CountDownTimer countDownTimer;

    @Inject
    IBroadcast broadcast;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ApplicationController.getComponent().inject(this);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false);

        lastPointLocation = new LatLng(LocalStorageUtil.getLastPosition().latitude,
                LocalStorageUtil.getLastPosition().longitude);
        lastDate = LocalStorageUtil.getLastPositionTime();

        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        binding.root.setOnClickListener(null);
        binding.myLocationLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.setMyLocation(true);
                toggleGps(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
            }
        });
        binding.lastLocationLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.setMyLocation(false);
                goToLastItemPosition();
            }
        });
        binding.done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                broadcast.postEvent(new ChangeScreenEvent(Screen.NONE, ChangeScreenEvent.ScreenGroup.SHADOWING));
            }
        });

        mapFragment = binding.mapview;
        mapFragment.onCreate(savedInstanceState);
        mapFragment.getMapAsync(this);

        locationServices = LocationServices.getLocationServices(getContext());
        setConnectionLostInfo(DateUtil.getLastConnectionTime(getActivity(), lastDate));
        return binding.getRoot();
    }

    @UiThread
    public void toggleGps(boolean enableGps) {
        if (enableGps) {
            // Check if user has granted location permission
            if (locationServices != null && !locationServices.areLocationPermissionsGranted()) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            } else {
                enableLocation(true);
            }
        } else {
            enableLocation(false);
        }
    }

    protected void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    protected void startTimer() {
        stopTimer();
        countDownTimer = new CountDownTimer(START_SEARCH_TIMEOUT, START_SEARCH_TIMEOUT) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                setConnectionLostInfo(DateUtil.getLastConnectionTime(getActivity(), lastDate));
            }
        }.start();
    }


    private void enableLocation(boolean enabled) {
        if (enabled) {
            locationServices.addLocationListener(new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        binding.setMyLocation(true);
                        myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        animateCameraToPosition(myLocation);
                        getDistanceAsync();
                    } else {
                        Snackbar.make(binding.getRoot(), "Undefined location", Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Snackbar.make(binding.getRoot(), "Please, turn on location to calculate approximate distance and " +
                    "show your position on map", Snackbar.LENGTH_LONG).show();
            binding.setMyLocation(false);
        }
        mapboxMap.setMyLocationEnabled(enabled);
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
        startTimer();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapFragment.onPause();
        stopTimer();
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

        toggleGps(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));

        goToLastItemPosition();
    }

    private void goToLastItemPosition() {
        addRobotMarkerToPosition(lastPointLocation);
        animateCameraToPosition(lastPointLocation);
    }

    private void addRobotMarkerToPosition(LatLng positionToMove) {
        IconFactory iconFactory = IconFactory.getInstance(getContext());
        Drawable iconDrawable = ContextCompat.getDrawable(getContext(), R.drawable.robot_map);
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

    private void getDistanceAsync() {
        if (myLocation != null && isAdded()) {
            Waypoint origin = new Waypoint(lastPointLocation.getLatitude(), lastPointLocation.getLongitude());
            Waypoint destination = new Waypoint(myLocation.getLatitude(), myLocation.getLongitude());

            MapboxDirections client = new MapboxDirections.Builder()
                    .setAccessToken(getString(R.string.mapbox_token))
                    .setOrigin(origin)
                    .setDestination(destination)
                    .setProfile(DirectionsCriteria.PROFILE_DRIVING)
                    .build();

            client.enqueue(new Callback<DirectionsResponse>() {
                @Override
                public void onResponse(Response<DirectionsResponse> response, Retrofit retrofit) {
                    DirectionsRoute route = response.body().getRoutes().get(0);
                    int distance = route.getDistance();
                    setDistanceInfo(distance);
                }

                @Override
                public void onFailure(Throwable t) {
                }
            });
        }
    }

    private void setDistanceInfo(int distanceMeters) {
        if (isAdded()) {
            binding.approximateDistance.setText(String.format(getString(R.string.last_location_text), distanceMeters));
        }
    }

    private void setConnectionLostInfo(String time) {
        if (isAdded()) {
            binding.connectionLost.setText(time);
        }
    }
}
