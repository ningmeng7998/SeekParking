package com.graceli.parkinghunt;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.DrawableRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {


    private class marker_Parking {
        public String bay_id;
        public String st_marker_id;
        public String status;
        public String lat;
        public String lon;
    }


    private class marker_Information {
        public String bayid;
        public String deviceid;

        public String description1;
        public String description2;
        public String description3;
        public String description4;

        public String duration1;
        public String duration2;
        public String duration3;
        public String duration4;

        public String endtime1;
        public String endtime2;
        public String endtime3;
        public String endtime4;
    }


    List<marker_Parking> ListParking;
    List<marker_Information> ListParking_Info;

    private GoogleMap mMap;

    private static String Marker_Url = "http://data.melbourne.vic.gov.au/resource/dtpv-d4pf.json";
    private static String Marker_Url2 = "http://data.melbourne.vic.gov.au/resource/ntht-5rk7.json";

    boolean autoFinder = false;
    private Button b_CurentLoc;
    private Button b_AutoFinder;
    private Button b_About;
    private Button b_Search;
    private Button b_Show;

    private Button bt_Clear;
    private Button bt_All;
    private Button bt_Per;
    private Button bt_Uno;

    private LocationManager locationManager;
    private String provider;

    Location srtup_loc;
    boolean gps_ON = false;
    boolean dataReadytoShow = false;

    int AUTOCOMPLETE_REQUEST_CODE = 1;

    double llat = 0;
    double llng = 0;
    int userSelect = 0;
    boolean waitReady = false;

    BooVariable bv = new BooVariable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataReadytoShow = false;
        autoFinder = false;
        gps_ON = false;
        b_AutoFinder = (Button) findViewById(R.id.btn_AutoLoc);
        b_CurentLoc = (Button) findViewById(R.id.btn_AutoLoc2);
        b_About = (Button) findViewById(R.id.btn_About);
        b_Search = (Button) findViewById(R.id.btn_Search);
        b_Show = (Button) findViewById(R.id.btn_show);

        bt_Clear = (Button) findViewById(R.id.b_clear);
        bt_All = (Button) findViewById(R.id.b_all);
        bt_Per = (Button) findViewById(R.id.b_peresent);
        bt_Uno = (Button) findViewById(R.id.b_unoccupied);

        b_CurentLoc.setVisibility(View.GONE);
        b_Show.setVisibility(View.GONE);
        b_About.setVisibility(View.GONE);
       // bt_Clear.setVisibility(View.GONE);


        Places.initialize(getApplicationContext(), "AIzaSyA9TR7G3OlBM_xUezgFS1NvIT64WuHQhtg");
        PlacesClient placesClient = Places.createClient(this);
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            new AlertDialog.Builder(this)
                    .setTitle("GPS Location")
                    .setMessage("Please TurnON your GPS location and access services")
                    //.setIcon()
                    .setCancelable(false)
                    .setPositiveButton("Turn ON", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Not now", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    })
                    .create()
                    .show();
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            onLocationChanged(location);
        }
        srtup_loc = location;
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.g_map);
        mapFragment.getMapAsync(this);
        b_About.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aboutFormShow();
            }
        });
        b_AutoFinder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoFinderLocation();
            }
        });
        b_CurentLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentLocation();

            }
        });
        b_Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchForm();
            }
        });
        b_Show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showlstSel();
            }
        });
        bt_Clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map_Clear();
            }
        });
        bt_All.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map_All();
            }
        });
        bt_Per.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map_pre();
            }
        });
        bt_Uno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map_uno();
            }
        });
        bv.setBoo(false);
        bv.setListener(new BooVariable.ChangeListener() {
            @Override
            public void onChange() {
            }
        });
    }

    private void map_Clear() {
        if (!bv.isBoo()) {
            Toast.makeText(getApplicationContext(), "Please Wait ...", Toast.LENGTH_SHORT).show();
            return;
        }
        mMap.clear();
    }


    private void map_All() {
        try {
            if (!bv.isBoo()) {
                Toast.makeText(getApplicationContext(), "Please Wait ...", Toast.LENGTH_SHORT).show();
                return;
            }
            mMap.clear();
            for (marker_Information info : ListParking_Info) {
                if (info.bayid == null) {
                    info.bayid = "";
                }
                if (info.deviceid == null) {
                    info.deviceid = "";
                }
                if (info.description1 == null) {
                    info.description1 = "";
                }
                if (info.description2 == null) {
                    info.description2 = "";
                }
                if (info.description3 == null) {
                    info.description3 = "";
                }
                if (info.description4 == null) {
                    info.description4 = "";
                }
                if (info.duration1 == null) {
                    info.duration1 = "";
                }
                if (info.duration2 == null) {
                    info.duration2 = "";
                }
                if (info.duration3 == null) {
                    info.duration3 = "";
                }
                if (info.duration4 == null) {
                    info.duration4 = "";
                }
                if (info.endtime1 == null) {
                    info.endtime1 = "";
                }
                if (info.endtime2 == null) {
                    info.endtime2 = "";
                }
                if (info.endtime3 == null) {
                    info.endtime3 = "";
                }
                if (info.endtime4 == null) {
                    info.endtime4 = "";
                }
            }


            int infoFound = 0;
            for (marker_Parking park : ListParking) {
                if (park.bay_id == null) {
                    park.bay_id = "";
                }
                if (park.status == null) {
                    park.status = "";
                }
                if (park.lon == null) {
                    park.lon = "";
                }
                if (park.lat == null) {
                    park.lat = "";
                }
                if (park.st_marker_id == null) {
                    park.st_marker_id = "";
                }
                if ((park.bay_id != "") && (park.lat != "") && (park.lon != "")) {
                    MarkerOptions Mark = new MarkerOptions().position(new LatLng(Double.parseDouble(park.lat), Double.parseDouble(park.lon)));
                    int WID = 0;
                    if (park.status.trim().toLowerCase().equals("present")) {
                        WID = 1;
                    } else if (park.status.trim().toLowerCase().equals("unoccupied")){
                        WID = 2;
                    }else{
                        WID = 3;
                    }

                    infoFound = 0;
                    Mark.title(park.status);
                    for (marker_Information info : ListParking_Info) {
                        if (info.bayid.trim().toLowerCase().equals(park.bay_id.trim().toLowerCase())) {
                            infoFound = 1;
                            String snippetS = "";
                            snippetS = "Description : \n" + info.description1.trim() + "\n" + info.description2.trim() + "\n" + info.description3.trim() + "\n" + info.description4.trim() + "\n" +
                                    "Duration : \n" + info.duration1.trim() + "\n" + info.duration2.trim() + "\n" + info.duration3.trim() + "\n" + info.duration4.trim() + "\n" +
                                    "EndTime : \n" + info.endtime1.trim() + "\n" + info.endtime2.trim() + "\n" + info.endtime3.trim() + "\n" + info.endtime4.trim();
                            snippetS = snippetS.replace(" ", "").trim();
                            snippetS = snippetS.replace("\n\n", "\n");
                            snippetS = snippetS.replace("\n\n", "\n");
                            snippetS = snippetS.replace("\n\n", "\n");
                            snippetS = snippetS.replace("\n\n", "\n");
                            snippetS = snippetS.trim();
                            Mark.snippet(snippetS);
                        }
                        if (infoFound == 1) {
                            break;
                        }
                    }
//                    if (infoFound == 0) {
//                        WID = 3;
//                    }
                    if (WID==1) {Mark.icon(BitmapDescriptorFactory.fromResource(R.drawable.pfre));}
                    if (WID==2) {Mark.icon(BitmapDescriptorFactory.fromResource(R.drawable.pfull));}
                     //if (WID==3) {Mark.icon(BitmapDescriptorFactory.fromResource(R.drawable.pnave));}
                    mMap.addMarker(Mark);
                }
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error Durring Load Markers", Toast.LENGTH_SHORT).show();
        }
    }


    private void map_pre() {
        try {
            if (!bv.isBoo()) {
                Toast.makeText(getApplicationContext(), "Please Wait ...", Toast.LENGTH_SHORT).show();
                return;
            }
            mMap.clear();
            for (marker_Information info : ListParking_Info) {
                if (info.bayid == null) {
                    info.bayid = "";
                }
                if (info.deviceid == null) {
                    info.deviceid = "";
                }
                if (info.description1 == null) {
                    info.description1 = "";
                }
                if (info.description2 == null) {
                    info.description2 = "";
                }
                if (info.description3 == null) {
                    info.description3 = "";
                }
                if (info.description4 == null) {
                    info.description4 = "";
                }
                if (info.duration1 == null) {
                    info.duration1 = "";
                }
                if (info.duration2 == null) {
                    info.duration2 = "";
                }
                if (info.duration3 == null) {
                    info.duration3 = "";
                }
                if (info.duration4 == null) {
                    info.duration4 = "";
                }
                if (info.endtime1 == null) {
                    info.endtime1 = "";
                }
                if (info.endtime2 == null) {
                    info.endtime2 = "";
                }
                if (info.endtime3 == null) {
                    info.endtime3 = "";
                }
                if (info.endtime4 == null) {
                    info.endtime4 = "";
                }
            }
            int infoFinded = 0;
            for (marker_Parking park : ListParking) {
                if (park.status.trim().toLowerCase().equals("present")) {
                    if (park.bay_id == null) {
                        park.bay_id = "";
                    }
                    if (park.status == null) {
                        park.status = "";
                    }
                    if (park.lon == null) {
                        park.lon = "";
                    }
                    if (park.lat == null) {
                        park.lat = "";
                    }
                    if (park.st_marker_id == null) {
                        park.st_marker_id = "";
                    }
                    if ((park.bay_id != "") && (park.lat != "") && (park.lon != "")) {
                        MarkerOptions Mark = new MarkerOptions().position(new LatLng(Double.parseDouble(park.lat), Double.parseDouble(park.lon)));
                        int WID = 0;
                        if (park.status.trim().toLowerCase().equals("present")) {
                            WID = 1;
                        } else if (park.status.trim().toLowerCase().equals("unoccupied")){
                            WID = 2;
                        }else{
                            WID = 3;
                        }
                        infoFinded = 0;
                        Mark.title(park.status);
                        for (marker_Information info : ListParking_Info) {
                            if (info.bayid.trim().toLowerCase().equals(park.bay_id.trim().toLowerCase())) {
                                infoFinded = 1;
                                String snippetS = "";
                                snippetS = "Description : \n" + info.description1.trim() + "\n" + info.description2.trim() + "\n" + info.description3.trim() + "\n" + info.description4.trim() + "\n" +
                                        "Duration : \n" + info.duration1.trim() + "\n" + info.duration2.trim() + "\n" + info.duration3.trim() + "\n" + info.duration4.trim() + "\n" +
                                        "EndTime : \n" + info.endtime1.trim() + "\n" + info.endtime2.trim() + "\n" + info.endtime3.trim() + "\n" + info.endtime4.trim();
                                snippetS = snippetS.replace(" ", "").trim();
                                snippetS = snippetS.replace("\n\n", "\n");
                                snippetS = snippetS.replace("\n\n", "\n");
                                snippetS = snippetS.replace("\n\n", "\n");
                                snippetS = snippetS.replace("\n\n", "\n");
                                snippetS = snippetS.trim();
                                Mark.snippet(snippetS);
                            }
                            if (infoFinded == 1) {
                                break;
                            }
                        }
//                        if (infoFinded == 0) {
//                            WID = 3;
//                        }
                        if (WID == 1) {
                            Mark.icon(BitmapDescriptorFactory.fromResource(R.drawable.pfre));
                        }
                        if (WID == 2) {
                            Mark.icon(BitmapDescriptorFactory.fromResource(R.drawable.pfull));
                        }
//                        if (WID == 3) {
//                            Mark.icon(BitmapDescriptorFactory.fromResource(R.drawable.pnave));
//                        }
                        mMap.addMarker(Mark);
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error Durring Load Markers", Toast.LENGTH_SHORT).show();
        }
    }


    private void map_uno() {
        try {
            if (!bv.isBoo()) {
                Toast.makeText(getApplicationContext(), "Please Wait ...", Toast.LENGTH_SHORT).show();
                return;
            }
            mMap.clear();
            for (marker_Information info : ListParking_Info) {
                if (info.bayid == null) {
                    info.bayid = "";
                }
                if (info.deviceid == null) {
                    info.deviceid = "";
                }
                if (info.description1 == null) {
                    info.description1 = "";
                }
                if (info.description2 == null) {
                    info.description2 = "";
                }
                if (info.description3 == null) {
                    info.description3 = "";
                }
                if (info.description4 == null) {
                    info.description4 = "";
                }
                if (info.duration1 == null) {
                    info.duration1 = "";
                }
                if (info.duration2 == null) {
                    info.duration2 = "";
                }
                if (info.duration3 == null) {
                    info.duration3 = "";
                }
                if (info.duration4 == null) {
                    info.duration4 = "";
                }
                if (info.endtime1 == null) {
                    info.endtime1 = "";
                }
                if (info.endtime2 == null) {
                    info.endtime2 = "";
                }
                if (info.endtime3 == null) {
                    info.endtime3 = "";
                }
                if (info.endtime4 == null) {
                    info.endtime4 = "";
                }
            }
            int infoFinded = 0;
            for (marker_Parking park : ListParking) {
                if (!park.status.trim().toLowerCase().equals("present")) {
                    if (park.bay_id == null) {
                        park.bay_id = "";
                    }
                    if (park.status == null) {
                        park.status = "";
                    }
                    if (park.lon == null) {
                        park.lon = "";
                    }
                    if (park.lat == null) {
                        park.lat = "";
                    }
                    if (park.st_marker_id == null) {
                        park.st_marker_id = "";
                    }
                    if ((park.bay_id != "") && (park.lat != "") && (park.lon != "")) {
                        MarkerOptions Mark = new MarkerOptions().position(new LatLng(Double.parseDouble(park.lat), Double.parseDouble(park.lon)));
                        int WID = 0;
                        if (park.status.trim().toLowerCase().equals("present")) {
                            WID = 1;
                        } else if (park.status.trim().toLowerCase().equals("unoccupied")){
                            WID = 2;
                        }else{
                            WID = 3;
                        }
                        infoFinded = 0;
                        Mark.title(park.status);
                        for (marker_Information info : ListParking_Info) {
                            if (info.bayid.trim().toLowerCase().equals(park.bay_id.trim().toLowerCase())) {
                                infoFinded = 1;
                                String snippetS = "";
                                snippetS = "Description : \n" + info.description1.trim() + "\n" + info.description2.trim() + "\n" + info.description3.trim() + "\n" + info.description4.trim() + "\n" +
                                        "Duration : \n" + info.duration1.trim() + "\n" + info.duration2.trim() + "\n" + info.duration3.trim() + "\n" + info.duration4.trim() + "\n" +
                                        "EndTime : \n" + info.endtime1.trim() + "\n" + info.endtime2.trim() + "\n" + info.endtime3.trim() + "\n" + info.endtime4.trim();
                                snippetS = snippetS.replace(" ", "").trim();
                                snippetS = snippetS.replace("\n\n", "\n");
                                snippetS = snippetS.replace("\n\n", "\n");
                                snippetS = snippetS.replace("\n\n", "\n");
                                snippetS = snippetS.replace("\n\n", "\n");
                                snippetS = snippetS.trim();
                                Mark.snippet(snippetS);
                            }
                            if (infoFinded == 1) {
                                break;
                            }
                        }
//                        if (infoFinded == 0) {
//                            WID = 3;
//                        }
                        if (WID == 1) {
                            Mark.icon(BitmapDescriptorFactory.fromResource(R.drawable.pfre));
                        }
                        if (WID == 2) {
                            Mark.icon(BitmapDescriptorFactory.fromResource(R.drawable.pfull));
                        }
//                        if (WID == 3) {
//                            Mark.icon(BitmapDescriptorFactory.fromResource(R.drawable.pnave));
//                        }
                        mMap.addMarker(Mark);
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error Durring Load Markers", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) try {
                final Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i("EMAS", "Place Found: " + place.getName() + " - " + place.getId());
                final String[] jsonString = {""};
                final Network network = new Network();
                try {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                jsonString[0] = network.getJsonString("https://maps.googleapis.com/maps/api/place/details/json?placeid=" + place.getId() + "&key=AIzaSyA9TR7G3OlBM_xUezgFS1NvIT64WuHQhtg");
                                int g_StartIN = jsonString[0].indexOf("location");
                                if (g_StartIN > 0) {
                                    int g_EndIN = jsonString[0].indexOf("}", g_StartIN + 8);
                                    String lastlatlng = jsonString[0].substring(g_StartIN, g_EndIN + 1);
                                    lastlatlng = lastlatlng.replace("\r\n", "").replace("\r", "").replace("\n", "").replace(" ", "");
                                    String l_Lat = "";
                                    String l_Lng = "";
                                    int ind_S = 0;
                                    int ind_E = 0;
                                    ind_S = lastlatlng.indexOf("\"lat\":") + 6;
                                    ind_E = lastlatlng.indexOf(",");
                                    l_Lat = lastlatlng.substring(ind_S, ind_E);
                                    ind_S = lastlatlng.indexOf("\"lng\":") + 6;
                                    ind_E = lastlatlng.indexOf("}");
                                    l_Lng = lastlatlng.substring(ind_S, ind_E);
                                    llat = Double.parseDouble(l_Lat);
                                    llng = Double.parseDouble(l_Lng);
                                    userSelect = 1;
                                    waitReady = false;
                                } else {
                                    Toast.makeText(getApplicationContext(), "Location Not Founded", Toast.LENGTH_SHORT).show();
                                    userSelect = 0;
                                }
                            } catch (IOException e) {
                                Log.i("EMAS", "Error IO :");
                                Log.i("EMAS", e.toString());
                                userSelect = 0;
                            }
                        }
                    });
                    thread.start();
                } catch (Exception e) {
                    Log.i("EMAS", "Error :");
                    Log.i("EMAS", e.toString());
                    userSelect = 0;
                }
            } catch (Exception e) {
                Log.i("EMAS", e.getMessage());
                Toast.makeText(getApplicationContext(), "Error To Move Camera ...", Toast.LENGTH_SHORT).show();
                userSelect = 0;
            }
            else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("EMAS", status.getStatusMessage());
                userSelect = 0;
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Searching Canceled ...", Toast.LENGTH_SHORT).show();
                userSelect = 0;
            }
        }
    }

    private void showlstSel() {
        userSelect = 1;
        waitReady = false;
        b_AutoFinder.setText("OFF");
        autoFinder = false;
        if (llng == 0) {
            llng = 0;
            llat = 0;
            return;
        }
        boolean shnl = autoFinder;
        Location lctn = new Location("");
        lctn.setLatitude(llat);
        lctn.setLongitude(llng);
        autoFinder = true;
        onLocationChanged(lctn);
        autoFinder = shnl;
    }

    private void searchForm() {
        llat = 0;
        llng = 0;
        b_AutoFinder.setText("OFF");
        autoFinder = false;
        userSelect = 1;
        waitReady = true;
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    private void aboutFormShow() {
        Intent GoActivity = new Intent(MainActivity.this, aboutF.class);
        startActivity(GoActivity);
    }

    private void autoFinderLocation() {
        if (autoFinder == false) {
            b_AutoFinder.setText("ON");
            autoFinder = true;
        } else {
            b_AutoFinder.setText("OFF");
            autoFinder = false;
        }
    }

    private void currentLocation() {
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            new AlertDialog.Builder(this)
                    .setTitle("GPS Location")
                    .setMessage("Please TurnON your GPS location and access services")
                    //.setIcon()
                    .setCancelable(false)
                    .setPositiveButton("Turn ON", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Not now", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    })
                    .create()
                    .show();
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            boolean pDAF = autoFinder;
            autoFinder = true;
            onLocationChanged(location);
            autoFinder = pDAF;
        }
        srtup_loc = location;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);
        while (waitReady == true) {
            if (userSelect == 0) {
                return;
            }
        }
        if (userSelect == 1) {
            userSelect = 0;
            showlstSel();
        }
        Log.i("EMAS", "Resume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);

        Log.i("EMAS", "Pause");
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        final String[] jsonString = {""};
        final String[] jsonString2 = {""};
        mMap = googleMap;
        mMap.clear();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        final Network network = new Network();
        ////////////////////////////////////////////////////////////////////////////////////////////
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        jsonString[0] = network.getJsonString(Marker_Url);
                        Gson gson = new Gson();
                        Type type = new TypeToken<List<marker_Parking>>() {
                        }.getType();
                        ListParking = gson.fromJson(jsonString[0], type);
                        ////////////////////////////////////////////////////////////////////////////
                        try {
                            Thread thread2 = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        jsonString2[0] = network.getJsonString(Marker_Url2);
                                        Gson gson2 = new Gson();
                                        Type type2 = new TypeToken<List<marker_Information>>() {
                                        }.getType();
                                        ListParking_Info = gson2.fromJson(jsonString2[0], type2);
                                        ////////////////////////////////////////////////////////////
                                        bv.setBoo(true);
                                        ////////////////////////////////////////////////////////////
                                    } catch (IOException e) {
                                        Log.i("EMAS", "Error IO2 :");
                                        Log.i("EMAS", e.toString());
                                    }
                                }
                            });
                            thread2.start();
                        } catch (Exception e) {
                            Log.i("EMAS", "Error2 :");
                            Log.i("EMAS", e.toString());
                        }
                        ////////////////////////////////////////////////////////////////////////////
                    } catch (IOException e) {
                        Log.i("EMAS", "Error IO :");
                        Log.i("EMAS", e.toString());
                    }
                }
            });
            thread.start();
        } catch (Exception e) {
            Log.i("EMAS", "Error :");
            Log.i("EMAS", e.toString());
        }
        ////////////////////////////////////////////////////////////////////////////////////////////
        LatLng Mark1LL;
        if (srtup_loc != null) {
            Mark1LL = new LatLng(srtup_loc.getLatitude(), srtup_loc.getLongitude());
        } else {
            Mark1LL = new LatLng(-37.804981, 144.992041);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Mark1LL, 15));
        MarkerInfoWindowAdapter markerInfoWindowAdapter = new MarkerInfoWindowAdapter(getApplicationContext());
        googleMap.setInfoWindowAdapter(markerInfoWindowAdapter);
    }


    @Override
    public void onLocationChanged(Location location) {
        if (autoFinder == true) {
            LatLng Mark1LL;
            if (location != null) {
                Mark1LL = new LatLng(location.getLatitude(), location.getLongitude());
            } else {
                Mark1LL = new LatLng(-37.804981, 144.992041);
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Mark1LL, 15));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        //Toast.makeText(this, "Enabled New Provider " + provider, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Toast.makeText(this, "Disabled Provider " + provider, Toast.LENGTH_SHORT).show();
    }


}




