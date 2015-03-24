package com.sails.example;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Liu Weilong on 24/3/15.
 */
public class SearchData {
    public static List<MapItem> mapItems = null;

    public static void setMapItems(ArrayList<MapItem> mapItems) {
        SearchData.mapItems = new ArrayList<MapItem>();
        SearchData.mapItems.addAll(mapItems);
    }
}
