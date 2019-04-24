package com.graceli.parkinghunt;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import com.google.maps.android.clustering.ClusterManager;


import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, NavigationView.OnNavigationItemSelectedListener {

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
    private ImageButton b_Reload;
    private LocationManager locationManager;
    private String provider;
    private Location srtup_loc;
    private int AUTOCOMPLETE_REQUEST_CODE = 1;
    private double llat = 0;
    private double llng = 0;
    private boolean showSelLoc = false;
    private String showSelLoc_Name = "";
    private int userSelect = 0;
    private boolean waitReady = false;
    private Toolbar mToolbar;
    private BooVariable bv = new BooVariable();
    private LinearLayout lyt_info;
    private FrameLayout FM;
    private String pub_Lat = "";
    private String pub_lng = "";
    private Button btn_parkhere;
    private Button btn_navigation;

    ClusterManager<MarkerClusterItem> clusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showSelLoc = false;
        b_Reload = (ImageButton) findViewById(R.id.btn_Reload);
        lyt_info = findViewById(R.id.lyt_info);
        lyt_info.setVisibility(View.GONE);
        FM = (FrameLayout) findViewById(R.id.fl_filter);
        btn_parkhere = findViewById(R.id.btn_ParkHere);
        btn_navigation = findViewById(R.id.btn_Navigation);
        FM.setVisibility(View.GONE);
        pub_Lat = "";
        pub_lng = "";
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
            lyt_info.setVisibility(View.GONE);
            onLocationChanged(location);
        }
        srtup_loc = location;
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.g_map);
        mapFragment.getMapAsync(this);
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setHint("Parking Search ...");
        autocompleteFragment.setCountry("AU");
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(final Place place) {
                lyt_info.setVisibility(View.GONE);
                llat = 0;
                llng = 0;
                autoFinder = false;
                userSelect = 1;
                waitReady = true;
                Log.i("EMAS", "Place Found: " + place.getName() + " - " + place.getId());
                showSelLoc_Name = "";
                showSelLoc_Name = place.getName();
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
                                    Toast.makeText(getApplicationContext(), "Location Not Found", Toast.LENGTH_SHORT).show();
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
            }
            @Override
            public void onError(Status status) {
                lyt_info.setVisibility(View.GONE);
                userSelect = 0;
                waitReady = true;
                Toast.makeText(getApplicationContext(), "Error to show place", Toast.LENGTH_SHORT).show();
            }
        });
        btn_parkhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    TextView txtlstdure = findViewById(R.id.txt_LastDuration);
                    TextView bayidtxt = findViewById(R.id.txt_bayno);
                    Intent GoActivity = new Intent(MainActivity.this, CounterConfig.class);
                    GoActivity.putExtra("TimeMin",txtlstdure.getText());
                    GoActivity.putExtra("mLat",pub_Lat);
                    GoActivity.putExtra("Mlon",pub_lng);
                    GoActivity.putExtra("bayNo",bayidtxt.getText());
                    startActivity(GoActivity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        btn_navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if((pub_Lat.equals("")) || (pub_lng.equals(""))) {return;}
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + pub_Lat + "," + pub_lng);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        b_Reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    lyt_info.setVisibility(View.GONE);
                    btnReloadClick();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        bv.setBoo(false);
        bv.setListener(new BooVariable.ChangeListener() {
            @Override
            public void onChange() {
            }
        });
        mToolbar = (Toolbar) findViewById(R.id.m_toolbar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        ImageButton hamMenu = findViewById(R.id.m_t_Menu);
        hamMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lyt_info.setVisibility(View.GONE);
                DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawerLayout.openDrawer(Gravity.START);
            }
        });
        ImageButton filterM = findViewById(R.id.m_t_Filter);
        filterM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lyt_info.setVisibility(View.GONE);
                FrameLayout FM = (FrameLayout) findViewById(R.id.fl_filter);
                if(FM.getVisibility()==View.GONE)
                {FM.setVisibility(View.VISIBLE); }
                else
                { FM.setVisibility(View.GONE); }
            }
        });
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        SeekBar seekbar = findViewById(R.id.rl_seekbar);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                map_uno();
            }
        });
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        lyt_info.setVisibility(View.GONE);
        FM.setVisibility(View.GONE);
        try{
            DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawerLayout.closeDrawer(Gravity.START);
            switch(menuItem.getItemId()){
                case R.id.m_about:
                    Intent GoActivity = new Intent(MainActivity.this, About.class);
                    startActivity(GoActivity);
                    break;
                case R.id.m_help:
                    Intent GoActivity2 = new Intent(MainActivity.this, HelpPage.class);
                    startActivity(GoActivity2);
                    break;
                case R.id.m_Userquide:
                    Intent GoActivity3 = new Intent(MainActivity.this, UserGuide.class);
                    startActivity(GoActivity3);
                    break;
                case R.id.m_exit:
                    finish();
                    break;
            }
        } catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "Error to run activity", Toast.LENGTH_SHORT).show();
        }
        return true;
    }


    private void map_uno() {
        lyt_info.setVisibility(View.GONE);
        int ParkType = 3; // 2P
        int Dur1 = 0;
        int Dur2 = 0;
        int Dur3 = 0;
        int Dur4 = 0;
        int addNewM = 0;
        try {
            if (!bv.isBoo()) { Toast.makeText(getApplicationContext(), "Please Wait ...", Toast.LENGTH_SHORT).show(); return; }
            try{
                SeekBar seekbar = findViewById(R.id.rl_seekbar);
                ParkType = seekbar.getProgress();
            }catch (Exception e) { ParkType = 3; }
            ParkType = ParkType + 1;
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
            clusterManager.clearItems();
            List<MarkerClusterItem> clusterItems = new ArrayList<>();
            for (marker_Parking park : ListParking) {
                infoFound = 0;
                Dur1 = 0;
                Dur2 = 0;
                Dur3 = 0;
                Dur4 = 0;
                addNewM = 0;
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
                        infoFound = 0;
                        Mark.title("Your Selected Parking");
                        for (marker_Information info : ListParking_Info) {
                            if (info.bayid.trim().toLowerCase().equals(park.bay_id.trim().toLowerCase())) {
                                infoFound = 1;
                                String snippetS = "";
                                if(info.description1.trim()==""){info.description1="NULL";}
                                if(info.description2.trim()==""){info.description2="NULL";}
                                if(info.description3.trim()==""){info.description3="NULL";}
                                if(info.description4.trim()==""){info.description4="NULL";}
                                if(info.duration1.trim()==""){info.duration1="NULL";}
                                if(info.duration2.trim()==""){info.duration2="NULL";}
                                if(info.duration3.trim()==""){info.duration3="NULL";}
                                if(info.duration4.trim()==""){info.duration4="NULL";}
                                snippetS = park.bay_id.trim() + "#" +
                                        info.description1.trim() + "@" + info.duration1.trim() + "!" +
                                        info.description2.trim() + "@" + info.duration2.trim() + "!" +
                                        info.description3.trim() + "@" + info.duration3.trim() + "!" +
                                        info.description4.trim() + "@" + info.duration4.trim();
                                snippetS = snippetS.replace(" ", "").trim();
                                snippetS = snippetS.replace("\n\n", "\n");
                                snippetS = snippetS.replace("\n\n", "\n");
                                snippetS = snippetS.replace("\n\n", "\n");
                                snippetS = snippetS.replace("\n\n", "\n");
                                snippetS = snippetS.trim();
                                Mark.snippet(snippetS);
                                try{
                                    Dur1=Integer.parseInt(info.duration1.trim().replaceAll("[\\D]",""));
                                }catch (Exception e){ Dur1 = 0; }

                                try{
                                    Dur2=Integer.parseInt(info.duration2.trim().replaceAll("[\\D]",""));
                                }catch (Exception e){ Dur2 = 0; }

                                try{
                                    Dur3=Integer.parseInt(info.duration3.trim().replaceAll("[\\D]",""));
                                }catch (Exception e){ Dur3 = 0; }

                                try{
                                    Dur4=Integer.parseInt(info.duration4.trim().replaceAll("[\\D]",""));
                                }catch (Exception e){ Dur4 = 0; }
                            }
                            if (infoFound == 1) {
                                break;
                            }
                        }
//                        if (WID == 1) {
//                            Mark.icon(BitmapDescriptorFactory.fromResource(R.drawable.pfre));
//                        }
                        if (WID == 2) {
                            Mark.icon(BitmapDescriptorFactory.fromResource(R.drawable.green_parking_1));
                        }
                        addNewM = 0;
                        if (infoFound==1)
                        {
                            switch (ParkType)
                            {
                                case 1: // 1/4 P
                                {
                                    if((Dur1>0) && (Dur1<=15)) { addNewM = 1; }
                                    if((Dur2>0) && (Dur2<=15)) { addNewM = 1; }
                                    if((Dur3>0) && (Dur3<=15)) { addNewM = 1; }
                                    if((Dur4>0) && (Dur4<=15)) { addNewM = 1; }
                                    break;
                                }

                                case 2: // 1/2 P
                                {
                                    if((Dur1>15) && (Dur1<=30)) { addNewM = 1; }
                                    if((Dur2>15) && (Dur2<=30)) { addNewM = 1; }
                                    if((Dur3>15) && (Dur3<=30)) { addNewM = 1; }
                                    if((Dur4>15) && (Dur4<=30)) { addNewM = 1; }
                                    break;
                                }

                                case 3: // 1 P
                                {
                                    if((Dur1>30) && (Dur1<=60)) { addNewM = 1; }
                                    if((Dur2>30) && (Dur2<=60)) { addNewM = 1; }
                                    if((Dur3>30) && (Dur3<=60)) { addNewM = 1; }
                                    if((Dur4>30) && (Dur4<=60)) { addNewM = 1; }
                                    break;
                                }

                                case 4: // 2 P
                                {
                                    if((Dur1>60) && (Dur1<=120)) { addNewM = 1; }
                                    if((Dur2>60) && (Dur2<=120)) { addNewM = 1; }
                                    if((Dur3>60) && (Dur3<=120)) { addNewM = 1; }
                                    if((Dur4>60) && (Dur4<=120)) { addNewM = 1; }
                                    break;
                                }

                                case 5: // 3 P
                                {
                                    if((Dur1>120) && (Dur1<=180)) { addNewM = 1; }
                                    if((Dur2>120) && (Dur2<=180)) { addNewM = 1; }
                                    if((Dur3>120) && (Dur3<=180)) { addNewM = 1; }
                                    if((Dur4>120) && (Dur4<=180)) { addNewM = 1; }
                                    break;
                                }

                                case 6: // 4 P
                                {
                                    if((Dur1>180) && (Dur1<=240)) { addNewM = 1; }
                                    if((Dur2>180) && (Dur2<=240)) { addNewM = 1; }
                                    if((Dur3>180) && (Dur3<=240)) { addNewM = 1; }
                                    if((Dur4>180) && (Dur4<=240)) { addNewM = 1; }
                                    break;
                                }

                                case 7: // All
                                {
                                    addNewM = 1;
                                    break;
                                }
                            }
                        }
                        else
                        {
                            String snippetS = "";
                            snippetS = park.bay_id.trim() + "#" +
                                    "NULL@NULL" + "!" +
                                    "NULL@NULL" + "!" +
                                    "NULL@NULL" + "!" +
                                    "NULL@NULL" ;
                            snippetS = snippetS.replace(" ", "").trim();
                            snippetS = snippetS.replace("\n\n", "\n");
                            snippetS = snippetS.replace("\n\n", "\n");
                            snippetS = snippetS.replace("\n\n", "\n");
                            snippetS = snippetS.replace("\n\n", "\n");
                            snippetS = snippetS.trim();
                            Mark.snippet(snippetS);
                            if (ParkType==4) { addNewM = 1; }
                            if (ParkType==7) { addNewM = 1; }
                        }
                        if (addNewM==1) {
                            clusterItems.add(new MarkerClusterItem(Mark));
                        }
                    }
                }
            }
            clusterManager.addItems(clusterItems);
            mMap.moveCamera(CameraUpdateFactory.zoomIn());
            mMap.moveCamera(CameraUpdateFactory.zoomOut());
            if (showSelLoc == true)
            {
                MarkerOptions Mark = new MarkerOptions().position(new LatLng(llat, llng));
                Mark.icon(BitmapDescriptorFactory.fromResource(R.drawable.green_marker_destination));
                Mark.title(showSelLoc_Name);
                mMap.addMarker(Mark);
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Failed to Load Markers", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        lyt_info.setVisibility(View.GONE);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) try {
            } catch (Exception e) {
                Log.i("EMAS", e.getMessage());
                Toast.makeText(getApplicationContext(), "Failed to Move Camera ...", Toast.LENGTH_SHORT).show();
                userSelect = 0;
            }
            else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("EMAS", status.getStatusMessage());
                userSelect = 0;
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Searching Cancelled ...", Toast.LENGTH_SHORT).show();
                userSelect = 0;
            }
        }
    }

    private void showlstSel() {
        lyt_info.setVisibility(View.GONE);
        userSelect = 1;
        waitReady = false;
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
        mMap.clear();
        showSelLoc = false;
        map_uno();
        MarkerOptions Mark = new MarkerOptions().position(new LatLng(llat, llng));
        Mark.icon(BitmapDescriptorFactory.fromResource(R.drawable.green_marker_destination));
        Mark.title(showSelLoc_Name);
        Mark.snippet("- Your Selected Location");
        mMap.addMarker(Mark);
        showSelLoc = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);
        while (waitReady) {
            if (userSelect == 0) {
                break;
            }
        }
        if (userSelect == 1) {
            userSelect = 0;
            lyt_info.setVisibility(View.GONE);
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
        lyt_info.setVisibility(View.GONE);
        FM.setVisibility(View.GONE);
        mMap = googleMap;
        mMap.clear();
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        LatLng Mark1LL;
        Mark1LL = new LatLng(-37.8186630, 144.9630143);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Mark1LL, 15));
        clusterManager = new ClusterManager<>(this, mMap);
        mMap.setOnCameraIdleListener(clusterManager);
        ClusterRenderer clusterRenderer = new ClusterRenderer(this, googleMap, clusterManager); // No need  to use clusterManager.setRenderer method since it is in constructor

        MarkerInfoWindowAdapter markerInfoWindowAdapter = new MarkerInfoWindowAdapter(getApplicationContext());
        mMap.setInfoWindowAdapter(markerInfoWindowAdapter);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                try{
                    pub_Lat = "";
                    pub_lng = "";
                    int ParkKKing = 0;
                    try{
                        SeekBar seekbar = findViewById(R.id.rl_seekbar);
                        ParkKKing = seekbar.getProgress();
                    }catch (Exception e) { ParkKKing = 3; }
                    ParkKKing = ParkKKing + 1;
                    lyt_info.setVisibility(View.GONE);
                    if (marker.getTitle().contains("Your Selected Parking"))
                    {
                        TextView bayno = findViewById(R.id.txt_bayno);
                        bayno.setText("Bay No. None");
                        TextView txt_LastDuration = findViewById(R.id.txt_LastDuration);
                        bayno.setText("0");
                        RelativeLayout p1rv = findViewById(R.id.p_info_1p);
                        TextView p1rv_desc = findViewById(R.id.p_info_1p_desc);
                        TextView p1rv_dur = findViewById(R.id.p_info_1p_dur);
                        p1rv.setVisibility(View.GONE);
                        p1rv_desc.setText("None");
                        p1rv_dur.setText("0");
                        RelativeLayout p2rv = findViewById(R.id.p_info_2p);
                        TextView p2rv_desc = findViewById(R.id.p_info_2p_desc);
                        TextView p2rv_dur = findViewById(R.id.p_info_2p_dur);
                        p2rv.setVisibility(View.GONE);
                        p2rv_desc.setText("None");
                        p2rv_dur.setText("0");
                        RelativeLayout p3rv = findViewById(R.id.p_info_3p);
                        TextView p3rv_desc = findViewById(R.id.p_info_3p_desc);
                        TextView p3rv_dur = findViewById(R.id.p_info_3p_dur);
                        p3rv.setVisibility(View.GONE);
                        p3rv_desc.setText("None");
                        p3rv_dur.setText("0");
                        RelativeLayout p4rv = findViewById(R.id.p_info_4p);
                        TextView p4rv_desc = findViewById(R.id.p_info_4p_desc);
                        TextView p4rv_dur = findViewById(R.id.p_info_4p_dur);
                        p4rv.setVisibility(View.GONE);
                        p4rv_desc.setText("None");
                        p4rv_dur.setText("0");
                        int desc1 = 0;int desc2 = 0;int desc3 = 0;int desc4 = 0;int cnt = 0;int lstdur = 0;
                        try{
                            Log.i("EMAS", marker.getSnippet());
                            String[] str1 = marker.getSnippet().split("#");
                            bayno.setText("Bay No. " + str1[0].trim());
                            Log.i("EMAS", str1[1].trim());
                            String[] str2 = str1[1].trim().split("!");
                            for(int j=0;j<4;j++)
                            {
                                if (str2[j]!=null)
                                {
                                    String[] str3 = str2[j].trim().split("@");
                                    if(str3.length>0)
                                    {
                                        if(str3[0]==null){str3[0]="";}
                                        if(str3[1]==null){str3[1]="";}
                                        if(str3[0].equals("NULL")){str3[0]="";}
                                        if(str3[1].equals("NULL")){str3[1]="";}
                                        if (!str3[0].trim().equals(""))
                                        {
                                            cnt++;
                                            switch (cnt){
                                                case 1:{
                                                    p1rv_desc.setText("  " + str3[0].trim());
                                                    p1rv_dur.setText(str3[1].trim());
                                                    int lodu = Integer.parseInt(str3[1].trim());
                                                    if (lodu>=lstdur) {lstdur=lodu;}
                                                    desc1 = 1;
                                                    p1rv.setVisibility(View.VISIBLE);
                                                    break;
                                                }
                                                case 2:{
                                                    p2rv_desc.setText("  " + str3[0].trim());
                                                    p2rv_dur.setText(str3[1].trim());
                                                    int lodu = Integer.parseInt(str3[1].trim());
                                                    if (lodu>=lstdur) {lstdur=lodu;}
                                                    desc2 = 1;
                                                    p2rv.setVisibility(View.VISIBLE);
                                                    break;
                                                }
                                                case 3:{
                                                    p3rv_desc.setText("  " + str3[0].trim());
                                                    p3rv_dur.setText(str3[1].trim());
                                                    int lodu = Integer.parseInt(str3[1].trim());
                                                    if (lodu>=lstdur) {lstdur=lodu;}
                                                    desc3 = 1;
                                                    p3rv.setVisibility(View.VISIBLE);
                                                    break;
                                                }
                                                case 4:{
                                                    p4rv_desc.setText("  " + str3[0].trim());
                                                    p4rv_dur.setText(str3[1].trim());
                                                    int lodu = Integer.parseInt(str3[1].trim());
                                                    if (lodu>=lstdur) {lstdur=lodu;}
                                                    desc4 = 1;
                                                    p4rv.setVisibility(View.VISIBLE);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e){
                            Log.i("EMAS", e.getMessage());
                            switch (ParkKKing){
                                case 1:{ p1rv_dur.setText("15"); }
                                case 2:{ p1rv_dur.setText("30"); }
                                case 3:{ p1rv_dur.setText("60"); }
                                case 4:{ p1rv_dur.setText("120"); }
                                case 5:{ p1rv_dur.setText("180"); }
                                case 6:{ p1rv_dur.setText("240"); }
                                case 7:{ p1rv_dur.setText("120"); }
                            }
                            lstdur = 0;
                            p1rv_desc.setText("  Mon - Sun");
                            p1rv.setVisibility(View.VISIBLE);
                            p2rv.setVisibility(View.GONE);
                            p3rv.setVisibility(View.GONE);
                            p4rv.setVisibility(View.GONE);
                        }
                        if((desc1==0) && (desc2==0) && (desc3==0) && (desc4==0))
                        {
                            switch (ParkKKing){
                                case 1:{ p1rv_dur.setText("15"); }
                                case 2:{ p1rv_dur.setText("30"); }
                                case 3:{ p1rv_dur.setText("60"); }
                                case 4:{ p1rv_dur.setText("120"); }
                                case 5:{ p1rv_dur.setText("180"); }
                                case 6:{ p1rv_dur.setText("240"); }
                                case 7:{ p1rv_dur.setText("120"); }
                            }
                            lstdur = 0;
                            p1rv_desc.setText("  Mon - Sun");
                            p1rv.setVisibility(View.VISIBLE);
                            p2rv.setVisibility(View.GONE);
                            p3rv.setVisibility(View.GONE);
                            p4rv.setVisibility(View.GONE);
                        }
                        if(lstdur<=0)
                        {
                            switch (ParkKKing){
                                case 1:{ lstdur = 15; }
                                case 2:{ lstdur = 30; }
                                case 3:{ lstdur = 60; }
                                case 4:{ lstdur = 120; }
                                case 5:{ lstdur = 180; }
                                case 6:{ lstdur = 240; }
                                case 7:{ lstdur = 120; }
                            }
                        }
                        txt_LastDuration.setText("0");
                        txt_LastDuration.setText(String.valueOf(lstdur));
                        pub_Lat = String.valueOf(marker.getPosition().latitude);
                        pub_lng = String.valueOf(marker.getPosition().longitude);
                        lyt_info.setVisibility(View.VISIBLE);
                    }
                    marker.showInfoWindow();
                } catch (Exception e){
                    lyt_info.setVisibility(View.GONE);
                }
                return true;
            }
        });
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                FM.setVisibility(View.GONE);
                lyt_info.setVisibility(View.GONE);
                pub_Lat = "";
                pub_lng = "";
            }
        });
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i("EMAS", "Auto Reload Start");
                    lyt_info.setVisibility(View.GONE);
                    btnReloadClick();
                    Log.i("EMAS", "Auto Reload End");
                } catch (InterruptedException e) {
                    Log.i("EMAS", "Auto Reload Error");
                }
            }
        }, 2000);
    }


    @Override
    public void onLocationChanged(Location location) {
        if (autoFinder == true) {
            LatLng Mark1LL;
            if (location != null) {
                Mark1LL = new LatLng(location.getLatitude(), location.getLongitude());
            } else {
                Mark1LL = new LatLng(-37.8186630, 144.9630143);
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Mark1LL, 18));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }


    private void btnReloadClick() throws InterruptedException {
        lyt_info.setVisibility(View.GONE);
        mMap.clear();
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Parking Hunt");
        progressDialog.setMessage("Fetching Data ... ");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                reloadParkData();
                map_uno();
                progressDialog.dismiss();
            }
        }, 1000);
    }

    private void reloadParkData()
    {
        lyt_info.setVisibility(View.GONE);
        final String[] jsonString = {""};
        final String[] jsonString2 = {""};
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
                    } catch (IOException e) {
                        Log.i("EMAS", "Error IO :");
                        Log.i("EMAS", e.toString());
                    }
                }
            });
            Log.i("EMAS", "T1 Start");
            thread.start();
            thread.join();
            Log.i("EMAS", "T1 End");
            ////////////////////////////////////////////////////////////////////////////
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
            Log.i("EMAS", "T2 Start");
            thread2.start();
            thread2.join();
            Log.i("EMAS", "T2 End");
        } catch (Exception e) {
            Log.i("EMAS", "Error :");
            Log.i("EMAS", e.toString());
        }
        ////////////////////////////////////////////////////////////////////////////////////////////
    }

}






