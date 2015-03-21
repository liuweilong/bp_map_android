package com.sails.example;

import com.sails.engine.LocationRegion;

/**
 * Created by Liu Weilong on 21/3/15.
 */
public class MapItem {
    private String name;
    private String floorName;
    private String floorDescription;
    private LocationRegion locationRegion;

    public MapItem(String name, String floorName, String floorDescription, LocationRegion locationRegion) {
        this.name = name;
        this.floorName = floorName;
        this.floorDescription = floorDescription;
        this.locationRegion = locationRegion;
    }

    public String getName() {
        return name;
    }

    public String getFloorName() {
        return floorName;
    }

    public String getFloorDescription() {
        return floorDescription;
    }

    public LocationRegion getLocationRegion() {
        return locationRegion;
    }
}
