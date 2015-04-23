package com.sails.example;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.sails.engine.LocationRegion;
import com.sails.engine.MarkerManager;
import com.sails.engine.PinMarkerManager;
import com.sails.engine.SAILS;
import com.sails.engine.SAILSMapView;
import com.sails.engine.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

//import android.widget.SearchView;

//import android.support.v7.widget.SearchView;


public class DirectionActivity extends ActionBarActivity {

    // Sails
    SAILS mSails = MainActivity.mSails;
    private SAILSMapView mSailsMapView;
//    SAILSMapView mSailsMapView = MainActivity.mSailsMapView;

    // Search
    MenuItem searchViewItem;
    SearchView searchView;
    SearchView searchView1;
    ListView list;
    ListViewAdapter ladapter;

    // Start and End location
    LocationRegion startLocation;
    LocationRegion endLocation;

    // Toolbar
    private Toolbar toolbar;

    // Button
    private Button startButton;
    private Button endButton;
    private Button routeButton;

    // Toggle start and end locations
    boolean hasStartLocation;
    boolean hasEndLocation;
    boolean currentIsStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction);

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

        toolBarInitial();
        locationButtonInitial();
    }


    void toolBarInitial() {
        if (toolbar == null) {
            toolbar = (Toolbar) findViewById(R.id.direction_actionbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
            }
        }
    }

    // Initial buttons to set start location, end location and to start routing
    void locationButtonInitial() {

        startButton = (Button) findViewById(R.id.button_start_location);
        endButton = (Button) findViewById(R.id.button_end_location);
        routeButton = (Button) findViewById(R.id.button_route);

        if (MainActivity.startLocationRegion != null) {
            hasStartLocation = true;
            startLocation = MainActivity.startLocationRegion;
            startButton.setText(startLocation.getName());
        } else {
            hasStartLocation = false;
        }
        hasEndLocation = false;

        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                currentIsStart = true;
                searchView.setIconified(false);
                searchView.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.VISIBLE);
                list.setVisibility(View.VISIBLE);
            }
        });

        endButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                currentIsStart = false;
                searchView.setIconified(false);
                searchView.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.VISIBLE);
                list.setVisibility(View.VISIBLE);
            }
        });

        routeButton.setOnClickListener(new View.OnClickListener() {
            public  void onClick(View v) {
                if (!(hasStartLocation && hasEndLocation)) {
                    Toast.makeText(getBaseContext(), "Fill in Start and End Regions.", Toast.LENGTH_SHORT).show();
                }
                else if (startLocation.getFloorNumber() != endLocation.getFloorNumber()) {
                    Toast.makeText(getBaseContext(), "Routing is only availble within the same level.", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (!mSailsMapView.getCurrentBrowseFloorName().equals(startLocation.getFloorName())) {
                        mSailsMapView.loadFloorMap(startLocation.getFloorName());
                    }
                    mSailsMapView.setVisibility(View.VISIBLE);
                    list.setVisibility(View.INVISIBLE);
                    route(startLocation, endLocation);
                }
            }
        });
    }

    void route(LocationRegion start, LocationRegion end) {
        if (start==null || end==null) return;
        mSailsMapView.getRoutingManager().setStartRegion(start);
        mSailsMapView.getRoutingManager().setTargetRegion(end);
        mSailsMapView.getRoutingManager().getPathPaint().setColor(0xFF85b038);
        Log.d("Location", "getStartRegion: " + mSailsMapView.getRoutingManager().getStartRegion().label);
        Log.d("Location", "getEndRegion: " + mSailsMapView.getRoutingManager().getTargetRegion().label);
        mSailsMapView.getRoutingManager().enableHandler();  // Start routing
//        mSailsMapView.clearFocus();
        mSailsMapView.autoSetMapZoomAndView();
        Log.d("Location", "path distance: " + mSailsMapView.getRoutingManager().getPathDistance());
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

        // set markers for start region and end region
        mSailsMapView.getRoutingManager().setStartMakerDrawable(Marker.boundCenter(getResources().getDrawable(R.drawable.destination)));
        mSailsMapView.getRoutingManager().setTargetMakerDrawable(Marker.boundCenterBottom(getResources().getDrawable(R.drawable.map_destination)));

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
                if (mSails.isLocationEngineStarted())
                    return;

                mSailsMapView.getMarkerManager().clear();
                mSailsMapView.getRoutingManager().setStartRegion(locationRegions.get(0));
                mSailsMapView.getMarkerManager().setLocationRegionMarker(locationRegions.get(0), Marker.boundCenter(getResources().getDrawable(R.drawable.map_destination)));
            }
        });
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

                Log.d("Search", "num of itmes: " + items.size());
                SearchData.setMapItems(items);

                list = (ListView) findViewById(R.id.listview);
                Log.d("Initializa", "Init list");
                list.setVisibility(View.INVISIBLE);

                // Locate the ListView in listview_main.xml
                list.setOnItemClickListener(listClickListener);

                // Pass results to ListViewAdapter Class
                ladapter = new ListViewAdapter(DirectionActivity.this, SearchData.mapItems);
                Log.d("Initializa", "Init ladapter");

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
            toolbar.setVisibility(View.GONE);

            ListViewAdapter.ViewHolder viewHolder = (ListViewAdapter.ViewHolder)view.getTag();
            LocationRegion lr = viewHolder.lr;
            mSailsMapView.getRoutingManager().disableHandler();

//            Toast.makeText(getBaseContext(), "lr: " + lr.getName(), Toast.LENGTH_SHORT).show();

            if (currentIsStart) {
                startButton.setText(lr.getName());
                startLocation = lr;
                hasStartLocation = true;
            }
            else {
                endLocation = lr;
                endButton.setText(lr.getName());
                hasEndLocation = true;
            }
        }
    };

        @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_direction, menu);

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
                            toolbar.setVisibility(View.GONE);
                            return false;
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
            case R.id.search:
//                onSearchRequested();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

//        return super.onOptionsItemSelected(item);
    }
}
