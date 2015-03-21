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
	private List<WorldPopulation> worldpopulationlist = null;
	private ArrayList<WorldPopulation> arraylist;
    private List<MapItem> mapItems = null;
    private ArrayList<MapItem> mapItemsBackUp;

	public ListViewAdapter(Context context, List<WorldPopulation> worldpopulationlist, List<MapItem> mapItems) {
		mContext = context;
		this.worldpopulationlist = worldpopulationlist;
		inflater = LayoutInflater.from(mContext);
		this.arraylist = new ArrayList<WorldPopulation>();
		this.arraylist.addAll(worldpopulationlist);
        this.mapItems = mapItems;
        this.mapItemsBackUp = new ArrayList<MapItem>();
        this.mapItemsBackUp.addAll(mapItems);
	}

	public class ViewHolder {
//		TextView rank;
//		TextView country;
//		TextView population;
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
			// Locate the TextViews in listview_item.xml
//			holder.rank = (TextView) view.findViewById(R.id.rank);
//			holder.country = (TextView) view.findViewById(R.id.country);
//			holder.population = (TextView) view.findViewById(R.id.population);
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
//		holder.rank.setText(worldpopulationlist.get(position).getRank());
//		holder.country.setText(worldpopulationlist.get(position).getCountry());
//		holder.population.setText(worldpopulationlist.get(position).getPopulation());

		// Listen for ListView Item Click
//		view.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//
//				// Send single item click data to SingleItemView Class
//				Intent intent = new Intent(mContext, SingleItemView.class);
//				// Pass all data rank
//				intent.putExtra("rank",(worldpopulationlist.get(position).getRank()));
//				// Pass all data country
//				intent.putExtra("country",(worldpopulationlist.get(position).getCountry()));
//				// Pass all data population
//				intent.putExtra("population",(worldpopulationlist.get(position).getPopulation()));
//				// Pass all data flag
//				// Start SingleItemView Class
//				mContext.startActivity(intent);
//			}
//		});

		return view;
	}

	// Filter Class
	public void filter(String searchText) {
		searchText = searchText.toLowerCase(Locale.getDefault());
//		worldpopulationlist.clear();
//		if (charText.length() == 0) {
//			worldpopulationlist.addAll(arraylist);
//		}
//		else
//		{
//			for (WorldPopulation wp : arraylist)
//			{
//				if (wp.getCountry().toLowerCase(Locale.getDefault()).contains(charText))
//				{
//					worldpopulationlist.add(wp);
//				}
//			}
//		}
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
