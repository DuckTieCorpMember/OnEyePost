package ducktie.oneyepost;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView postCodeTextView;
    private EditText addressEditText;
    private FloatingActionButton findLocationButton;
    private TextView oldPostCodeTextView;
    private TextView inputTextView;
    private ImageView imageView;

    private String city;
    private String street;
    private String house;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private Button mapButton;

    //animations
    Animation animationFadeIn;
    Animation animationFadeOut;

    //map
    public static FragmentManager fragmentManager;


    String apiKey = "cf1a6625b1588dd0c754b9f84e677870";
    String sharedSecret = "821a6d2136a53ee0";

    ArrayList<Bitmap> allBacks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        postCodeTextView = (TextView) findViewById(R.id.postCodeTextView);
        addressEditText = (EditText) findViewById(R.id.addressEditText);
        findLocationButton = (FloatingActionButton) findViewById(R.id.findLocationButton);
        oldPostCodeTextView = (TextView) findViewById(R.id.oldPostCodeTextView);
        inputTextView = (TextView) findViewById(R.id.inputTextView);
        imageView = (ImageView) findViewById(R.id.imageView);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mapButton = (Button) findViewById(R.id.mapButton);

        Typeface thin = Typeface.createFromAsset(getAssets(), "fonts/robotoThin.ttf");
        Typeface light = Typeface.createFromAsset(getAssets(), "fonts/robotoLight.ttf");
        Typeface regular = Typeface.createFromAsset(getAssets(), "fonts/robotoRegular.ttf");
        Typeface bold = Typeface.createFromAsset(getAssets(), "fonts/robotoBold.ttf");

        postCodeTextView.setTypeface(regular);
        oldPostCodeTextView.setTypeface(light);
        inputTextView.setTypeface(regular);


        //animations
        animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
        animationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout);

        allBacks.add(BitmapFactory.decodeResource(getResources(), R.drawable.a1));

        setImage();

        addressEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {

                    //animations
                    fadeOutAnimation();

                    getPostCode();
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(addressEditText.getWindowToken(), 0);
                    handled = true;
                }
                return handled;
            }
        });


        //Location listener
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "IN LOCATION LISTENER");
                onSearchClick(location);
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
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.INTERNET
                }, 10);
                return;
            }
        } else {
            configureButton();
        }

//        mapButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onMapButtonClick();
//            }
//        });

        //map
        fragmentManager = getSupportFragmentManager();

    }

    private void onMapButtonClick() {
        Intent mapActivity = new Intent(this, GoogleMapActivity.class);
        startActivity(mapActivity);
    }

    @Override
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


        findLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fadeOutAnimation();

                Log.d(TAG, "DETECTED BUTTON PRESS");
                locationManager.requestLocationUpdates("gps", 10000, 0, locationListener);
            }
        });
    }

    private void setImage() {
        Random r = new Random();
        int ran = r.nextInt();
        ran = ran % allBacks.size();
        Bitmap bitmap = allBacks.get(ran);
        bitmap = fastblur(bitmap, 0.3f, 5);
        imageView.setImageBitmap(bitmap);
    }

//    public static Bitmap getBitmapFromURL(String src) {
//        try {
//            URL url = new URL(src);
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setDoInput(true);
//            connection.connect();
//            InputStream input = connection.getInputStream();
//            Bitmap myBitmap = BitmapFactory.decodeStream(input);
//            return myBitmap;
//        } catch (IOException e) {
//            // Log exception
//            return null;
//        }
//    }

    public Bitmap fastblur(Bitmap sentBitmap, float scale, int radius) {

        int width = Math.round(sentBitmap.getWidth() * scale);
        int height = Math.round(sentBitmap.getHeight() * scale);
        sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false);

        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (bitmap);
    }

    private void getPostCode() {

        String toFind = addressEditText.getText().toString();
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

                        //city house street

                        postCodeTextView.setText(postCode);
                        oldPostCodeTextView.setText(newCode);

                        inputTextView.setText(postCodeObj.getString("addressRus"));

//                        inputTextView.setText(city + ", " + street + ", " + house + ".");
                        TextViewFadeIn();

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

    private void onSearchClick(Location loc) {
        // String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latitude + "," + longitude + "&key=AIzaSyB5JdIhiqiuzgaXWRburE4uKmv21RjJc_Y";


        Log.d(TAG, "IN ONSEARCHCLICK");

        String url;


        url = "https://geocode-maps.yandex.ru/1.x/?format=json&geocode=" + loc.getLongitude() + "," + loc.getLatitude();

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

                addressEditText.setText(text);
                getPostCode();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.getMessage() + " ERROR!");
            }
        });

        queue.add(request);

    }

    private void TextViewFadeIn()
    {
        postCodeTextView.startAnimation(animationFadeIn);
        oldPostCodeTextView.startAnimation(animationFadeIn);
        inputTextView.startAnimation(animationFadeIn);
    }

    private void TextViewFadeOut()
    {
        postCodeTextView.startAnimation(animationFadeOut);
        oldPostCodeTextView.startAnimation(animationFadeOut);
        inputTextView.startAnimation(animationFadeOut);
    }

    protected void fadeOutAnimation() {
        animationFadeOut.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
                TextViewFadeIn();
            }
            @Override
            public void onAnimationRepeat(Animation animation) {

            }

        });

        TextViewFadeOut();

    }

}
