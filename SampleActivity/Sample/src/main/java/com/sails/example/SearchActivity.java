package com.sails.example;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Build;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class SearchActivity extends ActionBarActivity {

    private Toolbar searchBar;
    private ListView resultList;
    private Menu menu;
    private ListViewAdapter listViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        searchBar = (Toolbar) findViewById(R.id.search_bar);
        searchBar.setTitle(R.string.title_search);
        searchBar.setNavigationIcon(R.drawable.ic_up);
        searchBar.setNavigationOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View view) {
                navigateUpTo(new Intent(SearchActivity.this, MainActivity.class));
            }
        });
        setSupportActionBar(searchBar);

        resultList = (ListView) findViewById(R.id.listview);
        if (SearchData.mapItems != null) {
            listViewAdapter = new ListViewAdapter(SearchActivity.this, SearchData.mapItems);
            resultList.setAdapter(listViewAdapter);
            Toast t = Toast.makeText(getBaseContext(), String.valueOf(SearchData.mapItems.size()), Toast.LENGTH_SHORT);
            t.show();
        }
        else {
            Toast t = Toast.makeText(getBaseContext(), "Data is not ready.", Toast.LENGTH_SHORT);
            t.show();
        }
    }


    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_search, menu);

        this.menu = menu;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

            SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();

            search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));

            search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    listViewAdapter.filter(query);
                    Toast t = Toast.makeText(getBaseContext(), "Data is not ready.", Toast.LENGTH_SHORT);
                    t.show();
                    return true;

                }

            });

        }

        return true;

    }
}
