package com.example.dailytrack;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Looper;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class LocationTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private boolean isCancelled = false; // Flag to check if the task is canceled

    private LocationListener locationListener;

    public interface LocationListener {
        void onLocationReceived(Location location);

        void onLocationFailed();
    }

    public LocationTask(Context context, LocationListener locationListener) {
        this.context = context;
        this.locationListener = locationListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        // Perform location retrieval in the background
        getLocation();
        return null;
    }
    private void getLocation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        LocationRequest locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000) // Update interval in milliseconds
                .setFastestInterval(5000); // Fastest update interval in milliseconds

        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    locationListener.onLocationReceived(location);
                } else {
                    // If getLastLocation is null, request location updates
                    fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            if (locationResult != null) {
                                Location location = locationResult.getLastLocation();
                                if (location != null) {
                                    locationListener.onLocationReceived(location);
                                } else {
                                    locationListener.onLocationFailed();
                                }
                            } else {
                                locationListener.onLocationFailed();
                            }
                        }
                    }, Looper.myLooper());
                }
            });
        } else {
            // Permission not granted, handle accordingly
            locationListener.onLocationFailed();
        }
    }
    @Override
    protected void onCancelled() {
        super.onCancelled();
        // Handle cancellation if needed
    }

    public void cancelTask() {
        isCancelled = true;
    }

}
