package com.sails.example;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Liu Weilong on 25/3/15.
 */
public class SpinnerAdapter extends BaseAdapter {

    List<String> floorList;
    LayoutInflater inflater;

    public SpinnerAdapter(Context context, List<String> floorList) {
        this.inflater = LayoutInflater.from(context);
        this.floorList = floorList;
    }

    @Override
    public int getCount() {
        return floorList.size();
    }

    @Override
    public Object getItem(int position) {
        return floorList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null || !view.getTag().toString().equals("NON_DROPDOWN")) {
            view = inflater.inflate(R.layout.
                    spinner_item, parent, false);
            view.setTag("NON_DROPDOWN");
        }
        TextView textView = (TextView) view.findViewById(R.id.customSpinnerItemTextView);
        textView.setText(floorList.get(position));
        return view;
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {
        if (view == null || !view.getTag().toString().equals("DROPDOWN")) {
            view = inflater.inflate(R.layout.spinner_dropdown_item, parent, false);
            view.setTag("DROPDOWN");
        }

        TextView textView = (TextView) view.findViewById(R.id.customSpinnerDropdownItemTextView);
        textView.setText(floorList.get(position));

        return view;
    }
}
