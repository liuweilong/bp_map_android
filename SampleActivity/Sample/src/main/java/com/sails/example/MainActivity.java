package com.sails.example;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sails.engine.Beacon;
import com.sails.engine.LocationRegion;
import com.sails.engine.MarkerManager;
import com.sails.engine.PinMarkerManager;
import com.sails.engine.SAILS;
import com.sails.engine.SAILSMapView;
import com.sails.engine.core.model.GeoPoint;
import com.sails.engine.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

//import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
//import android.app.ActionBar;

// TODO: long press can have option to start routing
// TODO: legend

public class MainActivity extends ActionBarActivity {

    // Variabls to share with DirectionActivity
    public static SAILS mSails;
    private SAILSMapView mSailsMapView;


//    ImageView zoomin;
//    ImageView zoomout;
    Spinner floorList;
    SpinnerAdapter adapter;
    byte zoomSav = 0;
    byte zooMlevel = 18;

    // Variable for search function
    MenuItem searchViewItem;
    SearchView searchView;
    ListView list;
    ListViewAdapter ladapter;


    // InfoDock
//    View infoDock;
//    TextView floorNameTextView;

    // Toolbar
    private Toolbar toolbar;

    // Button to invoke DirectionActivity
    Button directionButton;
    protected static LocationRegion startLocationRegion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Set Customized Toolbar
        toolBarInitial();

//        zoomin = (ImageView) findViewById(R.id.zoomin);
//        zoomout = (ImageView) findViewById(R.id.zoomout);

        floorList = (Spinner) findViewById(R.id.spinner_nav);

//        zoomin.setOnClickListener(controlListener);
//        zoomout.setOnClickListener(controlListener);

//        floorNameTextView = (TextView) findViewById(R.id.displayFloorName);
//        infoDock = (View) findViewById(R.id.infoDock);

        LocationRegion.FONT_LANGUAGE = LocationRegion.NORMAL;

        initButton();

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
                // 96af8361581f43a1b7a27ba618aa6695 5511570fd98797a814001c1d
                // 5511570fd98797a814001c1d
                mSails.loadCloudBuilding("96af8361581f43a1b7a27ba618aa6695", "5511570fd98797a814001c1d", new SAILS.OnFinishCallback() {
                    @Override
                    public void onSuccess(final String response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast t = Toast.makeText(getBaseContext(), "Map loaded", Toast.LENGTH_SHORT);
                                t.show();
                                mapViewInitial();
                                searchInitial();
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

    // za
    public void initButton() {
        directionButton = (Button) findViewById(R.id.direction);
        directionButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
               goToDirectionActivity();
            }
        });

    }

    public void goToDirectionActivity() {
        Intent myIntent = new Intent(MainActivity.this, DirectionActivity.class);
        MainActivity.this.startActivity(myIntent);
    }

   void toolBarInitial() {
        if (toolbar == null) {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
            }
        }
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
        // First click to set start region and second click to set end region
        // TODO: handleOnRegionClick
        mSailsMapView.setOnRegionClickListener(new SAILSMapView.OnRegionClickListener() {
            @Override
            public void onClick(List<LocationRegion> locationRegions) {
                Toast t;
                LocationRegion lr = locationRegions.get(0);
                t = Toast.makeText(getBaseContext(), lr.getName(), Toast.LENGTH_SHORT);
                t.show();
            }
        });


        // TODO: not sure how this works, double check later
        mSailsMapView.getPinMarkerManager().setOnPinMarkerClickCallback(new PinMarkerManager.OnPinMarkerClickCallback() {
            @Override
            public void OnClick(MarkerManager.LocationRegionMarker locationRegionMarker) {
                mSailsMapView.getMarkerManager().clear();
                Toast.makeText(getApplication(), "(" + Double.toString(locationRegionMarker.locationRegion.getCenterLatitude()) + "," +
                        Double.toString(locationRegionMarker.locationRegion.getCenterLongitude()) + ")", Toast.LENGTH_SHORT).show();
            }
        });

        mSailsMapView.setOnClickNothingListener(new SAILSMapView.OnClickNothingListener() {

            @Override
            public void onClick() {
                mSailsMapView.getMarkerManager().clear();
            }
        });

        //set location region long click call back.
        mSailsMapView.setOnRegionLongClickListener(new SAILSMapView.OnRegionLongClickListener() {
            @Override
            public void onLongClick(List<LocationRegion> locationRegions) {
                startLocationRegion = locationRegions.get(0);
                mSailsMapView.getMarkerManager().clear();
//                mSailsMapView.getRoutingManager().setStartRegion(locationRegions.get(0));
                mSailsMapView.getMarkerManager().setLocationRegionMarker(locationRegions.get(0), Marker.boundCenter(getResources().getDrawable(R.drawable.map_destination)));
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
//                floorNameTextView.setText(mSailsMapView.getCurrentBrowseFloorName());
            }
        });

        // Setup spinner
        // TODO: They style of choosing the floor need to be updated
//        adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, mSails.getFloorDescList());
//        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        adapter = new SpinnerAdapter(MainActivity.this, mSails.getFloorDescList());
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

//        floorNameTextView.setText(mSailsMapView.getCurrentBrowseFloorName());
    }

    void searchInitial() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayList<MapItem> items = new ArrayList<MapItem>();
                for (String floorName : mSails.getFloorNameList()) {
                    String floorDescription = mSails.getFloorDescription(floorName);
                    for (LocationRegion lr : mSails.getLocationRegionList(floorName)) {
                        if (lr.getName() == null || lr.getName().length() == 0)
                            continue;
                        MapItem item = new MapItem(lr.getName(), floorName, floorDescription, lr);
                        items.add(item);
                    }
                }

                SearchData.setMapItems(items);

                list = (ListView) findViewById(R.id.listview);
                list.setVisibility(View.INVISIBLE);

                // Locate the ListView in listview_main.xml
                list.setOnItemClickListener(listClickListener);

                // Pass results to ListViewAdapter Class
                ladapter = new ListViewAdapter(MainActivity.this, SearchData.mapItems);

                // Binds the Adapter to the ListView
                list.setAdapter(ladapter);
            }
        });
    }
    ListView.OnItemClickListener listClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            searchView.onActionViewCollapsed();
            list.setVisibility(View.INVISIBLE);
            floorList.setVisibility(View.VISIBLE);
//            infoDock.setVisibility(View.VISIBLE);

            ListViewAdapter.ViewHolder viewHolder = (ListViewAdapter.ViewHolder)view.getTag();
            LocationRegion lr = viewHolder.lr;

            if (!lr.getFloorName().equals(mSailsMapView.getCurrentBrowseFloorName())) {
                mSailsMapView.loadFloorMap(lr.getFloorName());
                mSailsMapView.getMapViewPosition().setZoomLevel((byte) 19);
                Toast.makeText(getBaseContext(), mSails.getFloorDescription(lr.getFloorName()), Toast.LENGTH_SHORT).show();
            }
            GeoPoint poi = new GeoPoint(lr.getCenterLatitude(), lr.getCenterLongitude());
            mSailsMapView.setAnimationToZoom((byte) 14);
            mSailsMapView.setAnimationMoveMapTo(poi);
            mSailsMapView.getMarkerManager().clear();
            mSailsMapView.getMarkerManager().setLocationRegionMarker(lr, Marker.boundCenterBottom(getResources().getDrawable(R.drawable.map_destination)));
//            floorNameTextView.setText(lr.getName());
        }
    };

//    View.OnClickListener controlListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            if (v == zoomin) {
//                //set map zoomin function.
//                mSailsMapView.zoomIn();
//            } else if (v == zoomout) {
//                //set map zoomout function.
//                mSailsMapView.zoomOut();
//            }
//        }
//    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Setup search
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            searchViewItem = menu.findItem(R.id.search);
            if (searchViewItem != null) {
                SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
                searchView = (SearchView) searchViewItem.getActionView();
                if (searchView != null) {
                    searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
                    searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                        @Override
                        public boolean onClose() {
                            list.setVisibility(View.GONE);
                            floorList.setVisibility(View.VISIBLE);
//                            infoDock.setVisibility(View.VISIBLE);
                            return false;
                        }
                    });

                    searchView.setOnSearchClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            list.setVisibility(View.VISIBLE);
                            floorList.setVisibility(View.GONE);
//                            infoDock.setVisibility(View.GONE);
                        }
                    });

                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String s) {
                            searchView.clearFocus();
                            if (s != null && !s.equals("")) {
                                ladapter.filter(s);
                            }
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String query) {
                            ladapter.filter(query);
                            return true;
                        }

                    });
                }
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
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
