package com.example.assignment5_newsgateway;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;

public class Async_source extends AsyncTask<String, Integer, String> {

    private static final String TAG = "Async_source";
    @SuppressLint("StaticFieldLeak")
    private MainActivity mainActivity;
    private HashMap<String, String> nameToID = new HashMap<>();
    private String category;
    private String source;
    private String id;
    private static final String dataURL = "https://newsapi.org/v2/sources?language=en&country=us&category=&apiKey=8a20eb2141dd44ca9ddb26f244c82287";

    Async_source(MainActivity ma) {
        mainActivity = ma;
    }


    @Override
    protected void onPostExecute(String s) {
        HashMap<String, HashSet<String>> categoryMap = parseJSON(s);
        if (categoryMap != null) {
            mainActivity.setupSource(categoryMap, nameToID);
        }
    }

    @Override
    protected String doInBackground(String... params) {

        Log.d(TAG, "doInBackground: ");
        Uri dataUri = Uri.parse(dataURL);
        String urlToUse = dataUri.toString();
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return sb.toString();
    }


    private HashMap<String, HashSet<String>> parseJSON(String s) {
        HashMap<String, HashSet<String>> categoryMap = new HashMap<>();
        categoryMap.put("all", new HashSet<String>());

        try {
            JSONObject jObjMain = new JSONObject(s);
            JSONArray sources = jObjMain.getJSONArray("sources");

            for (int i = 0; i < sources.length(); i++) {
                JSONObject object = (JSONObject) sources.get(i);
                category = object.getString("category");
                source = object.getString("name");
                id = object.getString("id");
                HashSet<String> allSet = categoryMap.get("all");
                allSet.add(source);
                if (!nameToID.containsKey(source)) {
                    nameToID.put(source, id);
                }

                if (!categoryMap.containsKey(category))
                    categoryMap.put(category, new HashSet<String>());

                HashSet<String> rSet = categoryMap.get(category);
                if (rSet != null) {
                    rSet.add(source);
                }
            }
            return categoryMap;
        } catch (
                Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
