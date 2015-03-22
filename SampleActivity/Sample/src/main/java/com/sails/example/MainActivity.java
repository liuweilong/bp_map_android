package com.sails.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.sails.engine.Beacon;
import com.sails.engine.LocationRegion;
import com.sails.engine.SAILS;
import com.sails.engine.MarkerManager;
import com.sails.engine.PathRoutingManager;
import com.sails.engine.PinMarkerManager;
import com.sails.engine.SAILSMapView;
import com.sails.engine.core.model.GeoPoint;
import com.sails.engine.overlay.Marker;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
//import android.app.ActionBar;
import android.app.Activity;
import android.app.Service;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

    static SAILS mSails;
    static SAILSMapView mSailsMapView;
    ImageView zoomin;
    ImageView zoomout;
//    SlidingMenu slidingMenu;
    Spinner floorList;
    ArrayAdapter<String> adapter;
    byte zoomSav = 0;
    byte zooMlevel = 18;

    // Variable for search function
    ListView list;
    ListViewAdapter ladapter;
    EditText editsearch;

    // Toolbar
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Set Customized Toolbar
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        slidingMenu = new SlidingMenu(this);
//        slidingMenu.setMode(SlidingMenu.LEFT);
//        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
//        slidingMenu.setFadeDegree(0.0f);
//        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
//        slidingMenu.setMenu(R.layout.search_view);

        zoomin = (ImageView) findViewById(R.id.zoomin);
        zoomout = (ImageView) findViewById(R.id.zoomout);

        floorList = (Spinner) findViewById(R.id.spinner);

        zoomin.setOnClickListener(controlListener);
        zoomout.setOnClickListener(controlListener);

        LocationRegion.FONT_LANGUAGE = LocationRegion.NORMAL;

        //new a SAILS engine.
        mSails = new SAILS(this);
        //set location mode.
        mSails.setMode(SAILS.BLE_GFP_IMU);
        //set floor number sort rule from descending to ascending.
        mSails.setReverseFloorList(true);
        //create location change call back.
        mSails.setOnLocationChangeEventListener(new SAILS.OnLocationChangeEventListener() {
            @Override
            public void OnLocationChange() {

                if (mSailsMapView.isCenterLock() && !mSailsMapView.isInLocationFloor() && !mSails.getFloor().equals("") && mSails.isLocationFix()) {
                    //set the map that currently location engine recognize.
                    mSailsMapView.getMapViewPosition().setZoomLevel(zooMlevel);
                    mSailsMapView.loadCurrentLocationFloorMap();
                    Toast t = Toast.makeText(getBaseContext(), mSails.getFloorDescription(mSails.getFloor()), Toast.LENGTH_SHORT);
                    t.show();
                }
            }
        });

        mSails.setOnBLEPositionInitialzeCallback(10000,new SAILS.OnBLEPositionInitializeCallback() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFixed() {

            }

            @Override
            public void onTimeOut() {
                if(!mSails.checkMode(SAILS.BLE_ADVERTISING))
                    mSails.stopLocatingEngine();
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Positioning Timeout")
                        .setMessage("Put some time out message!")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialoginterface, int i) {
                                mSailsMapView.setMode(SAILSMapView.GENERAL);
                            }
                        }).setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mSails.startLocatingEngine();
                    }
                }).show();
            }
        });

        mSails.setNoWalkAwayPushRepeatDuration(6000);
        mSails.setOnBTLEPushEventListener(new SAILS.OnBTLEPushEventListener() {
            @Override
            public void OnPush(final Beacon mB) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplication(),mB.push_name,Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void OnNothingPush() {
                Log.e("Nothing Push","true");
            }
        });

        //new and insert a SAILS MapView from layout resource.
        mSailsMapView = new SAILSMapView(this);
        ((FrameLayout) findViewById(R.id.SAILSMap)).addView(mSailsMapView);
        //configure SAILS map after map preparation finish.
        mSailsMapView.post(new Runnable() {
            @Override
            public void run() {
                //please change token and building id to your own building project in cloud.
                // 29008b47625243bca00ffdd4e52af10f 5508f92fd98797a814001afc
                // 96af8361581f43a1b7a27ba618aa6695 55082d4ad98797a814001ace
                mSails.loadCloudBuilding("96af8361581f43a1b7a27ba618aa6695", "55082d4ad98797a814001ace", new SAILS.OnFinishCallback() {
                    @Override
                    public void onSuccess(final String response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast t = Toast.makeText(getBaseContext(), "Map loaded", Toast.LENGTH_SHORT);
                                t.show();
                                mapViewInitial();
//                                routingInitial();
//                                slidingMenuInitial();
                            }
                        });

                    }

                    @Override
                    public void onFailed(String response) {
                        Toast t = Toast.makeText(getBaseContext(), "Load cloud project fail, please check network connection.", Toast.LENGTH_SHORT);
                        t.show();
                    }
                });
            }
        });
    }

    void mapViewInitial() {
        //establish a connection of SAILS engine into SAILS MapView.
        mSailsMapView.setSAILSEngine(mSails);

        //set location pointer icon.
//        mSailsMapView.setLocationMarker(R.drawable.circle, R.drawable.arrow, null, 35);

        //set location marker visible.
        mSailsMapView.setLocatorMarkerVisible(true);

        //load first floor map in package.
        mSailsMapView.loadFloorMap(mSails.getFloorNameList().get(0));
//        actionBar.setTitle("Search");

        //Auto Adjust suitable map zoom level and position to best view position.
        mSailsMapView.autoSetMapZoomAndView();

        //set location region click call back.
        // TODO: handleOnRegionClick
        mSailsMapView.setOnRegionClickListener(new SAILSMapView.OnRegionClickListener() {
            @Override
            public void onClick(List<LocationRegion> locationRegions) {
                LocationRegion lr = locationRegions.get(0);
                //begin to routing
                if (mSails.isLocationEngineStarted()) {
                    //set routing start point to current user location.
                    mSailsMapView.getRoutingManager().setStartRegion(PathRoutingManager.MY_LOCATION);

                    //set routing end point marker icon.
//                    mSailsMapView.getRoutingManager().setTargetMakerDrawable(Marker.boundCenterBottom(getResources().getDrawable(R.drawable.destination)));

                    //set routing path's color.
                    mSailsMapView.getRoutingManager().getPathPaint().setColor(0xFF35b3e5);

//                    endRouteButton.setVisibility(View.VISIBLE);
//                    currentFloorDistanceView.setVisibility(View.VISIBLE);
//                    msgView.setVisibility(View.VISIBLE);

                } else {
//                    mSailsMapView.getRoutingManager().setTargetMakerDrawable(Marker.boundCenterBottom(getResources().getDrawable(R.drawable.map_destination)));
                    mSailsMapView.getRoutingManager().getPathPaint().setColor(0xFF85b038);
//                    if (mSailsMapView.getRoutingManager().getStartRegion() != null)
//                        endRouteButton.setVisibility(View.VISIBLE);
                }

                //set routing end point location.
                mSailsMapView.getRoutingManager().setTargetRegion(lr);

                //begin to route.
//                if (mSailsMapView.getRoutingManager().enableHandler())
//                    distanceView.setVisibility(View.VISIBLE);
            }
        });

        // TODO: not sure how this works, double check later
        mSailsMapView.getPinMarkerManager().setOnPinMarkerClickCallback(new PinMarkerManager.OnPinMarkerClickCallback() {
            @Override
            public void OnClick(MarkerManager.LocationRegionMarker locationRegionMarker) {
                Toast.makeText(getApplication(), "(" + Double.toString(locationRegionMarker.locationRegion.getCenterLatitude()) + "," +
                        Double.toString(locationRegionMarker.locationRegion.getCenterLongitude()) + ")", Toast.LENGTH_SHORT).show();
            }
        });

        //set location region long click call back.
        // TODO: handle regionLongClick
        mSailsMapView.setOnRegionLongClickListener(new SAILSMapView.OnRegionLongClickListener() {
            @Override
            public void onLongClick(List<LocationRegion> locationRegions) {
                if (mSails.isLocationEngineStarted())
                    return;

                mSailsMapView.getMarkerManager().clear();
                mSailsMapView.getRoutingManager().setStartRegion(locationRegions.get(0));
//                mSailsMapView.getMarkerManager().setLocationRegionMarker(locationRegions.get(0), Marker.boundCenter(getResources().getDrawable(R.drawable.start_point)));
            }
        });

        //design some action in floor change call back.
        mSailsMapView.setOnFloorChangedListener(new SAILSMapView.OnFloorChangedListener() {
            @Override
            public void onFloorChangedBefore(String floorName) {
                //get current map view zoom level.
                zoomSav = mSailsMapView.getMapViewPosition().getZoomLevel();
            }

            @Override
            public void onFloorChangedAfter(final String floorName) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        //check is locating engine is start and current brows map is in the locating floor or not.
                        if (mSails.isLocationEngineStarted() && mSailsMapView.isInLocationFloor()) {
                            //change map view zoom level with animation.
                            mSailsMapView.setAnimationToZoom(zoomSav);
                        }
                    }
                };
                new Handler().postDelayed(r, 1000);

                int position = 0;
                for (String mS : mSails.getFloorNameList()) {
                    if (mS.equals(floorName))
                        break;
                    position++;
                }
                floorList.setSelection(position);
            }
        });

        // TODO: They style of choosing the floor need to be updated
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mSails.getFloorDescList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        floorList.setAdapter(adapter);
        floorList.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!mSailsMapView.getCurrentBrowseFloorName().equals(mSails.getFloorNameList().get(position)))
                    mSailsMapView.loadFloorMap(mSails.getFloorNameList().get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

//    void slidingMenuInitial() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                ArrayList<MapItem> items = new ArrayList<MapItem>();
//                for (String floorName : mSails.getFloorNameList()) {
//                    String floorDescription = mSails.getFloorDescription(floorName);
//                    for (LocationRegion lr : mSails.getLocationRegionList(floorName)) {
//                        if (lr.getName() == null || lr.getName().length() == 0)
//                            continue;
//                        MapItem item = new MapItem(lr.getName(), floorName, floorDescription, lr);
//                        items.add(item);
//                    }
//                }
//
//                // Locate the ListView in listview_main.xml
//                list = (ListView) findViewById(R.id.listview);
//                list.setOnItemClickListener(listClickListener);
//
//                // Pass results to ListViewAdapter Class
//                ladapter = new ListViewAdapter(MainActivity.this, items);
//
//                // Binds the Adapter to the ListView
//                list.setAdapter(ladapter);
//
//                // Locate the EditText in listview_main.xml
//                editsearch = (EditText) findViewById(R.id.search);
//
//                // Capture Text in EditText
//                editsearch.addTextChangedListener(new TextWatcher() {
//
//                    @Override
//                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//                    }
//
//                    @Override
//                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//                    }
//
//                    @Override
//                    public void afterTextChanged(Editable editable) {
//                        String text = editsearch.getText().toString().toLowerCase(Locale.getDefault());
//                        ladapter.filter(text);
//                    }
//                });
//            }
//        });
//    }
//    ListView.OnItemClickListener listClickListener = new ListView.OnItemClickListener() {
//        @Override
//        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
//            ListViewAdapter.ViewHolder viewHolder = (ListViewAdapter.ViewHolder)view.getTag();
//            LocationRegion lr = viewHolder.lr;
//
//            if (!lr.getFloorName().equals(mSailsMapView.getCurrentBrowseFloorName())) {
//                mSailsMapView.loadFloorMap(lr.getFloorName());
//                mSailsMapView.getMapViewPosition().setZoomLevel((byte) 19);
//                Toast.makeText(getBaseContext(), mSails.getFloorDescription(lr.getFloorName()), Toast.LENGTH_SHORT).show();
//            }
//            GeoPoint poi = new GeoPoint(lr.getCenterLatitude(), lr.getCenterLongitude());
//            mSailsMapView.setAnimationMoveMapTo(poi);
////            slidingMenu.showContent();
//            // TODO: add highlight on the result place
//        }
//    };

    View.OnClickListener controlListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == zoomin) {
                //set map zoomin function.
                mSailsMapView.zoomIn();
            } else if (v == zoomout) {
                //set map zoomout function.
                mSailsMapView.zoomOut();
//            } else if (v == lockcenter) {
//                if (!mSails.isLocationFix() || !mSails.isLocationEngineStarted()) {
//                    Toast t = Toast.makeText(getBaseContext(), "Location Not Found or Location Engine Turn Off.", Toast.LENGTH_SHORT);
//                    t.show();
//                    return;
//                }
//                if (!mSailsMapView.isCenterLock() && !mSailsMapView.isInLocationFloor()) {
//                    //set the map that currently location engine recognize.
//                    mSailsMapView.loadCurrentLocationFloorMap();
//
//                    Toast t = Toast.makeText(getBaseContext(), "Go Back to Locating Floor First.", Toast.LENGTH_SHORT);
//                    t.show();
//                    return;
//                }
//                //set map mode.
//                //FOLLOW_PHONE_HEADING: the map follows the phone's heading.
//                //LOCATION_CENTER_LOCK: the map locks the current location in the center of map.
//                //ALWAYS_LOCK_MAP: the map will keep the mode even user moves the map.
//                if (mSailsMapView.isCenterLock()) {
//                    if ((mSailsMapView.getMode() & SAILSMapView.FOLLOW_PHONE_HEADING) == SAILSMapView.FOLLOW_PHONE_HEADING)
//                        //if map control mode is follow phone heading, then set mode to location center lock when button click.
//                        mSailsMapView.setMode(mSailsMapView.getMode() & ~SAILSMapView.FOLLOW_PHONE_HEADING);
//                    else
//                        //if map control mode is location center lock, then set mode to follow phone heading when button click.
//                        mSailsMapView.setMode(mSailsMapView.getMode() | SAILSMapView.FOLLOW_PHONE_HEADING);
//                } else {
//                    //if map control mode is none, then set mode to loction center lock when button click.
//                    mSailsMapView.setMode(mSailsMapView.getMode() | SAILSMapView.LOCATION_CENTER_LOCK);
//                }
            }
        }
    };

//    public void restoreActionBar() {
//        ActionBar actionBar = getActionBar();
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//        actionBar.setDisplayShowTitleEnabled(true);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
//        restoreActionBar();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.search:
                // TODO: handle search
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSailsMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSailsMapView.onPause();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
        ((FrameLayout) findViewById(R.id.SAILSMap)).removeAllViews();
    }
}
