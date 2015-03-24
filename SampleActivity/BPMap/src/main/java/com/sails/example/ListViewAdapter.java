package com.sails.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.view.View.OnClickListener;

import com.sails.engine.LocationRegion;

public class ListViewAdapter extends BaseAdapter {

	// Declare Variables
	Context mContext;
	LayoutInflater inflater;
    private List<MapItem> mapItems = null;
    private ArrayList<MapItem> mapItemsBackUp;

	public ListViewAdapter(Context context, List<MapItem> mapItems) {
		mContext = context;
		inflater = LayoutInflater.from(mContext);
        this.mapItems = mapItems;
        this.mapItemsBackUp = new ArrayList<MapItem>();
        this.mapItemsBackUp.addAll(mapItems);
	}

	public class ViewHolder {
        TextView mapItemName;
        TextView floorName;
        LocationRegion lr;
	}

	@Override
	public int getCount() {
		return mapItems.size();
	}

	@Override
	public MapItem getItem(int position) {
		return mapItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View view, ViewGroup parent) {
		final ViewHolder holder;
		if (view == null) {
			holder = new ViewHolder();
			view = inflater.inflate(R.layout.listview_item, null);
            holder.mapItemName = (TextView) view.findViewById(R.id.mapItem);
            holder.floorName = (TextView) view.findViewById(R.id.floorName);;
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		// Set the results into TextViews
        holder.mapItemName.setText(mapItems.get(position).getName());
        holder.floorName.setText(mapItems.get(position).getFloorName());
        holder.lr = mapItems.get(position).getLocationRegion();

		// Listen for ListView Item Click
//		view.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//			}
//		});

		return view;
	}

	// Filter Class
	public void filter(String searchText) {
		searchText = searchText.toLowerCase(Locale.getDefault());

        mapItems.clear();
        if (searchText.length() == 0) {
            mapItems.addAll(mapItemsBackUp);
        } else {
            for (MapItem item: mapItemsBackUp) {
                if (item.getName().toLowerCase(Locale.getDefault()).contains(searchText)) {
                    mapItems.add(item);
                }
            }
        }
		notifyDataSetChanged();
	}

}
