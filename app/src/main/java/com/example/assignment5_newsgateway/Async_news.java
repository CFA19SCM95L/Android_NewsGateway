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
import java.util.ArrayList;

public class Async_news extends AsyncTask<String, Integer, String> {
    private static final String TAG = "Async_news";
    @SuppressLint("StaticFieldLeak")
    private NewsService newsService;
    private String dataURLPre = "https://newsapi.org/v2/top-headlines?sources=";
    private static String source;
    private String dataURLPost = "&apiKey=8a20eb2141dd44ca9ddb26f244c82287";

    Async_news(NewsService ns) {
        newsService = ns;
    }

    @Override
    protected void onPostExecute(String s) {
        ArrayList<News> newsList = parseJSON(s);
        newsService.setNews(newsList);
    }


    @Override
    protected String doInBackground(String... params) {
        source = params[0];
        Uri dataUri = Uri.parse(dataURLPre+ source + dataURLPost);
        String urlToUse = dataUri.toString();
        Log.d(TAG, "doInBackground: " + urlToUse);
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

    private ArrayList<News> parseJSON(String s) {
        Log.d(TAG, "parseJSON: ---" );
        ArrayList<News> newsList = new ArrayList<>();
        try {
            JSONObject jObjMain = new JSONObject(s);
            JSONArray articles = jObjMain.getJSONArray("articles");
            for (int i = 0; i < articles.length(); i++) {
                JSONObject newsObj = (JSONObject) articles.get(i);
                News news = new News();
                news.setAuthor(newsObj.getString("author"));
                Log.d(TAG, "parseJSON: ---" + newsObj.getString("author"));
                news.setDescription(newsObj.getString("description"));
                news.setPublishedAt(newsObj.getString("publishedAt"));
                news.setTitle(newsObj.getString("title"));
                news.setUrlToImage(newsObj.getString("urlToImage"));
                news.setUrl(newsObj.getString("url"));
                newsList.add(news);
            }
            return newsList;
        } catch (
                Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
