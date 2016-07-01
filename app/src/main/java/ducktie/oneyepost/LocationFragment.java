package ducktie.oneyepost;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ktauk on 7/1/2016.
 */
public class LocationFragment extends Fragment implements GoogleMap.OnMapClickListener, OnMapReadyCallback {

    private static final String TAG = "LocationFragment";
    private static View view;
    /**
     * Note that this may be null if the Google Play services APK is not
     * available.
     */

    private static GoogleMap mMap;
    private static Double latitude, longitude;
    private LatLng globalLoc;
    private Marker marker;
    private GoogleMap map;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        if (container == null) {
            return null;
        }
        view = (RelativeLayout) inflater.inflate(R.layout.location_fragment, container, false);
        // Passing harcoded values for latitude & longitude. Please change as per your need. This is just used to drop a Marker on the Map

        SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.location_map);
        mapFragment.getMapAsync(this);

        latitude = 26.78;
        longitude = 72.56;


        //setUpMapIfNeeded(); // For setting up the MapFragment

        return view;
    }

    /***** Sets up the map if it is possible to do so *****/
//    public static void setUpMapIfNeeded() {
//        // Do a null check to confirm that we have not already instantiated the map.
//        if (mMap == null) {
//            // Try to obtain the map from the SupportMapFragment.
//            mMap = ((SupportMapFragment) MainActivity.fragmentManager
//                    .findFragmentById(R.id.location_map)).getMap();
//            // Check if we were successful in obtaining the map.
//            if (mMap != null)
//                setUpMap();
//        }
//    }

    /**
     * This is where we can add markers or lines, add listeners or move the
     * camera.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap}
     * is not null.
     */
//    private static void setUpMap() {
//        // For showing a move to my loction button
//
//        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        mMap.setMyLocationEnabled(true);
//        // For dropping a marker at a point on the Map
//        mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("My Home").snippet("Home Address"));
//        // For zooming automatically to the Dropped PIN Location
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude,
//                longitude), 12.0f));
//    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
//        if (mMap != null)
//            setUpMap();
//
//        if (mMap == null) {
//            // Try to obtain the map from the SupportMapFragment.
//            SupportMapFragment mMap = ((SupportMapFragment) MainActivity.fragmentManager
//                    .findFragmentById(R.id.location_map));
//
//            mMap.getMapAsync(this);
//            // getMap is deprecated
//            // Check if we were successful in obtaining the map.
//            if (mMap != null)
//                setUpMap();
//        }



    }

    /**** The mapfragment's id must be removed from the FragmentManager
     **** or else if the same it is passed on the next time then
     **** app will crash ****/
    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        if (mMap != null) {
//            MainActivity.fragmentManager.beginTransaction()
//                    .remove(MainActivity.fragmentManager.findFragmentById(R.id.location_map)).commit();
//            mMap = null;
//        }
//    }

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
        onMapClickRequest(latLng);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        map.setOnMapClickListener(this);
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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


    }

    private void getPostCode(String address) {

        String toFind = address;
        String words[] = toFind.split(" ");
        String urlStart = "api.postcode.kz/api/byAddress/";
        String urlEnd = "?from=0";
        toFind = toFind.replace(" ", "%20");
        String url = "http://" + urlStart + toFind + urlEnd;

        RequestQueue queue = Volley.newRequestQueue(getContext());

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
                        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }

                        //map.moveCamera(CameraUpdateFactory.newLatLngZoom(globalLoc, 13));

                        if(marker != null) {
                            marker.remove();
                        }

                        String[] address = postCodeObj.getString("addressRus").split(", ");
                        String addressString = "";

                        for (String addr : address)
                        {
                            addressString += addr + "\n";
                        }

                        marker = map.addMarker(new MarkerOptions()
                                .title(postCode)
                                .snippet(newCode + "\n\n" + addressString)
                                .position(globalLoc));


                        //marker.setVisible(false);
                        marker.showInfoWindow();




                    } catch (JSONException e) {
                        e.printStackTrace();
                        //Toast.makeText(getApplicationContext(), "Illegal input. Please, try again.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Log.d("Main Activity", error.getMessage());
                //Toast.makeText(getApplicationContext(), "Illegal input. Please, try again.", Toast.LENGTH_SHORT).show();
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
        RequestQueue queue = Volley.newRequestQueue(getContext());
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
