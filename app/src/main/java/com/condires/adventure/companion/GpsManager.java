package com.condires.adventure.companion;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

/*
wir vom Adventure Recorder eingesetzt
 */
public class GpsManager extends LocationCallback {

    private FusedLocationProviderClient client;
    private Callback callback;

    public interface Callback {
        void onLocationResult(LocationResult locationResult);
    }

    public boolean start(Context context, Callback callback) {
        this.callback = callback;
        client = LocationServices.getFusedLocationProviderClient(context);
        if (!checkLocationPermission(context)) return false;
        client.requestLocationUpdates(getLocationRequest(), this, null);
        return true;
    }

    public void stop() {
        client.removeLocationUpdates(this);
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
        callback.onLocationResult(locationResult);
    }

    private boolean checkLocationPermission(Context context) {
        int permissionCheck = ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    private LocationRequest getLocationRequest() {
        return LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(2000L)
                .setFastestInterval(1000L);
    }
}