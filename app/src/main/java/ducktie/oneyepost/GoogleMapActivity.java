package ducktie.oneyepost;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GoogleMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private static final String TAG = "GoogleMapActivity";
    GoogleMap map;
    private LatLng globalLoc;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private ProgressBar progressBar;
    private View tintView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        progressBar = (ProgressBar) findViewById(R.id.mapProgressBar);
        tintView = findViewById(R.id.tintView);

        //initialize progressBar
        //progressBar.


       // map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.151589, 71.506995), 13));

        //Location listener
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "IN LOCATION LISTENER");

                //remove ProgressBar and Tint
                tintView.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);


                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };

        //xren znaet zachem, Askhat skazal
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.INTERNET
                }, 10);
                return;
            }
        } else {
            configureButton();
        }
    }

    //!!!geolocation code
    public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions,
                                             @NonNull int[] grantResults){
        switch (requestCode) {
            case 10:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    configureButton();
                return;
        }
    }

    private void configureButton() {

        Log.d(TAG, "DETECTED BUTTON PRESS");
        locationManager.requestLocationUpdates("gps", 10000, 0, locationListener);

    }



    @Override
    public void onMapClick(LatLng latLng) {

//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
        Log.d(TAG, "You tapped at: " + latLng);
        onMapClickRequest(latLng);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setOnMapClickListener(this);
    }

    private void getPostCode(String address) {

        String toFind = address;
        String words[] = toFind.split(" ");
        String urlStart = "api.postcode.kz/api/byAddress/";
        String urlEnd = "?from=0";
        toFind = toFind.replace(" ", "%20");
        String url = "http://" + urlStart + toFind + urlEnd;

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Main Activity", "Object: " + response.toString());

                if (response != null) {
                    try {
                        JSONArray jsonArray = response.getJSONArray("data");

                        JSONObject postCodeObj = jsonArray.getJSONObject(0);
                        String postCode = postCodeObj.getString("postcode");

                        JSONObject full = postCodeObj.getJSONObject("fullAddress");
                        String newCode = full.getString("oldPostcode");

                        //sets a marker
                        if (ActivityCompat.checkSelfPermission(GoogleMapActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(GoogleMapActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        map.setMyLocationEnabled(true);
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(globalLoc, 13));
                        Marker marker = map.addMarker(new MarkerOptions()
                                .title(postCode + " " + newCode)
                                .snippet(postCodeObj.getString("addressRus"))
                                .position(globalLoc));

                        marker.setVisible(false);
                        marker.showInfoWindow();




                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Illegal input. Please, try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Log.d("Main Activity", error.getMessage());
                Toast.makeText(getApplicationContext(), "Illegal input. Please, try again.", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(request);
        Log.d("MainActivity", "Your text is: " + url);
    }

    private void onMapClickRequest(LatLng loc) {
        // String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude + "&key=AIzaSyB5JdIhiqiuzgaXWRburE4uKmv21RjJc_Y";
        globalLoc = loc;

        Log.d(TAG, "IN ONSEARCHCLICK");

        String url;


        url = "https://geocode-maps.yandex.ru/1.x/?format=json&geocode=" + loc.longitude + "," + loc.latitude;

        Log.d(TAG, url);
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                //JSONArray jsonArray = response.getJSONObject("results").getString("formattedString");
                //String formattedAdress = response.getJSONObject("response").getJSONObject("GeoObjectCollection").getJSONObject("metaDataProperty").getJSONObject("GeocoderResponseMetaData").getString("request");
                JSONObject country = null;
                JSONObject area = null;
                JSONObject city = null;
                JSONObject street = null;
                JSONObject houseNumber = null;
                try {
                    country = response.getJSONObject("response").getJSONObject("GeoObjectCollection").getJSONArray("featureMember").getJSONObject(0).getJSONObject("GeoObject").getJSONObject("metaDataProperty").getJSONObject("GeocoderMetaData").getJSONObject("AddressDetails").getJSONObject("Country");
                } catch (JSONException e) {
                    country = null;
                }
                try {
                    area = response.getJSONObject("response").getJSONObject("GeoObjectCollection").getJSONArray("featureMember").getJSONObject(0).getJSONObject("GeoObject").getJSONObject("metaDataProperty").getJSONObject("GeocoderMetaData").getJSONObject("AddressDetails").getJSONObject("Country").getJSONObject("AdministrativeArea");
                } catch (JSONException e) {
                    area = null;
                }
                try {
                    city = response.getJSONObject("response").getJSONObject("GeoObjectCollection").getJSONArray("featureMember").getJSONObject(0).getJSONObject("GeoObject").getJSONObject("metaDataProperty").getJSONObject("GeocoderMetaData").getJSONObject("AddressDetails").getJSONObject("Country").getJSONObject("AdministrativeArea").getJSONObject("Locality");
                } catch (JSONException e) {
                    city = null;
                }
                try {
                    street = response.getJSONObject("response").getJSONObject("GeoObjectCollection").getJSONArray("featureMember").getJSONObject(0).getJSONObject("GeoObject").getJSONObject("metaDataProperty").getJSONObject("GeocoderMetaData").getJSONObject("AddressDetails").getJSONObject("Country").getJSONObject("AdministrativeArea").getJSONObject("Locality").getJSONObject("Thoroughfare");
                } catch (JSONException e) {
                    street = null;
                }
                try {
                    houseNumber = response.getJSONObject("response").getJSONObject("GeoObjectCollection").getJSONArray("featureMember").getJSONObject(0).getJSONObject("GeoObject").getJSONObject("metaDataProperty").getJSONObject("GeocoderMetaData").getJSONObject("AddressDetails").getJSONObject("Country").getJSONObject("AdministrativeArea").getJSONObject("Locality").getJSONObject("Thoroughfare").getJSONObject("Premise");
                } catch (JSONException e) {
                    houseNumber = null;
                }
                String text = "";
                if (country != null) {
//                    try {
//                        text += "Country: " + country.getString("CountryName") + "\n";
//                    } catch (JSONException e) {
//                    }

//                    if (area != null) {
//                        try {
//                            text += "Administrative Area: " + area.getString("AdministrativeAreaName") + "\n";
//                        } catch (JSONException e) {
//                        }

                    if (city != null) {
                        try {
                            text +=city.getString("LocalityName") + " ";
                        } catch (JSONException e) {
                        }

                        if (street != null) {
                            try {
                                text +=street.getString("ThoroughfareName") + " ";
                            } catch (JSONException e) {
                            }

                            if (houseNumber != null) {
                                try {
                                    text +=houseNumber.getString("PremiseNumber");
                                } catch (JSONException e) {
                                }
                            }
                        }
                    }
                }

                //  Log.d(TAG, country);


                getPostCode(text);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.getMessage() + " ERROR!");
            }
        });

        queue.add(request);

    }
}
