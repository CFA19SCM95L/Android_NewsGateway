package com.example.assignment5_newsgateway;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

public class NewsService extends Service {

    private static final String TAG = "NewsService";
    private boolean running = true;
    private ServiceReceiver serviceReceiver;
    private ArrayList<News> newsList = new ArrayList<>();

    public NewsService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceReceiver = new ServiceReceiver();

        IntentFilter filter1 = new IntentFilter(MainActivity.SOURCESEND);
        registerReceiver(serviceReceiver, filter1);

        new Thread(new Runnable() {
            @Override
            public void run() {

                while (running) {
                    while(newsList.isEmpty()){
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Intent intent = new Intent();
                    intent.setAction(MainActivity.NEWSRETURN);
                    intent.putExtra("newsList", newsList);
                    sendBroadcast(intent);
                    newsList.clear();
                }
                Log.i(TAG, "NewsService was properly stopped");
            }
        }).start();


        return Service.START_NOT_STICKY;
    }

    public void setNews(ArrayList<News> list){
        newsList.clear();
        newsList.addAll(list);
    }

    class ServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case MainActivity.SOURCESEND:
                    Log.d(TAG, "onReceive: ID ");

                    String id="";
                    if (intent.hasExtra("ID")) {
                        id = intent.getStringExtra("ID");
                    }
                    Log.d(TAG, "onReceive: ID " + id);
                    new Async_news(NewsService.this).execute(id);
                    break;
            }

        }
    }

}
