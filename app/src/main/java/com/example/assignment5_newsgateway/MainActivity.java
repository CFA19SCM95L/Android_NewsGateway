package com.example.assignment5_newsgateway;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public DrawerLayout mDrawerLayout;
    public static int screenWidth, screenHeight;
    private Menu opt_menu;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private List<Fragment> fragments;
    private ViewPager pager;
    private MyPageAdapter pageAdapter;
    private String currentSource;
    private ArrayList<String> sourceDisplayed = new ArrayList<>();
    private HashMap<String, ArrayList<String>> sourceData = new HashMap<>();
    private HashMap<String,String> nameToID;
    public NewsReceiver newsReceiver;
    static final String SOURCESEND = "SOURCESEND";
    static final String NEWSRETURN = "NEWSRETURN";
    public boolean isRunning= false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if (!isRunning) {
            Intent intent = new Intent(MainActivity.this, NewsService.class);
            startService(intent);
            isRunning = true;
        }
        newsReceiver = new NewsReceiver();
        IntentFilter filter = new IntentFilter(MainActivity.NEWSRETURN);
        registerReceiver(newsReceiver, filter);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.drawer_list);

        mDrawerList.setOnItemClickListener(
                new ListView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        selectItem(position);
                        mDrawerLayout.closeDrawer(mDrawerList);
                    }
                }
        );

        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        );

        fragments = new ArrayList<>();
        pageAdapter = new MyPageAdapter(getSupportFragmentManager());
        pager = findViewById(R.id.viewpager);
        pager.setAdapter(pageAdapter);

        if (sourceData.isEmpty())
            new Async_source(this).execute();
    }

    private void selectItem(int position) {
        pager.setBackground(null);
        currentSource = sourceDisplayed.get(position);
        String id = nameToID.get(currentSource);
        Intent intent = new Intent(MainActivity.SOURCESEND);
        intent.putExtra("ID", id);
        sendBroadcast(intent);
        mDrawerLayout.closeDrawer(mDrawerList);

    }

    public void setupSource(HashMap<String, HashSet<String>> sourceMap, HashMap<String,String> idName) {
        this.nameToID = idName;
        sourceData.clear();
        for (String s : sourceMap.keySet()) {
            HashSet<String> hSet = sourceMap.get(s);
            if (hSet == null)
                continue;
            ArrayList<String> subSource = new ArrayList<>(hSet);
            Collections.sort(subSource);
            sourceData.put(s, subSource);
        }
        ArrayList<String> tempList = new ArrayList<>(sourceData.keySet());
        Collections.sort(tempList);
        for (String s : tempList)
            opt_menu.add(s);

        ArrayList<String> lst = sourceData.get(tempList.get(0));
        if (lst != null) {
            sourceDisplayed.addAll(lst);
        }
        mDrawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_item, sourceDisplayed));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    public void setNews(ArrayList<News> newsList) {

        setTitle(currentSource);

        Log.d(TAG, "setNews: start");

        for (int i = 0; i < pageAdapter.getCount(); i++)
            pageAdapter.notifyChangeInPosition(i);

        fragments.clear();

        for (int i = 0; i < newsList.size(); i++) {
            fragments.add(
                    NewsFragment.newInstance(newsList.get(i), i+1, newsList.size()));
//            pageAdapter.notifyChangeInPosition(i);
        }
        pageAdapter.notifyDataSetChanged();
        pager.setCurrentItem(0);
        Log.d(TAG, "setNews: end");

    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            Log.d(TAG, "onOptionsItemSelected: mDrawerToggle " + item);
            return true;
        }

        setTitle(item.getTitle());

        sourceDisplayed.clear();

        ArrayList<String> lst = sourceData.get(item.getTitle().toString());
        if (lst != null) {
            sourceDisplayed.addAll(lst);
        }

        ((ArrayAdapter) mDrawerList.getAdapter()).notifyDataSetChanged();
        return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity, menu);
        opt_menu = menu;
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////
    private class MyPageAdapter extends FragmentPagerAdapter {
        private long baseId = 0;


        MyPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public long getItemId(int position) {
            return baseId + position;
        }
        void notifyChangeInPosition(int n) {
            baseId += getCount() + n;
        }
    }


    class NewsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case NEWSRETURN:
                    ArrayList<News> newsList;
                    if (intent.hasExtra("newsList")) {
                        newsList = (ArrayList<News>) intent.getSerializableExtra("newsList");
                        setNews(newsList);
                    }
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(newsReceiver);
        Intent intent = new Intent(MainActivity.this, NewsReceiver.class);
        stopService(intent);
        super.onDestroy();
    }
}




