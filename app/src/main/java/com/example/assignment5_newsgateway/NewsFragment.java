package com.example.assignment5_newsgateway;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.squareup.picasso.Picasso;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewsFragment extends Fragment {

    private static final String TAG = "NewsFragment";

    public NewsFragment() {
    }

    public static NewsFragment newInstance(News news, int index, int max) {
        Log.d(TAG, "newInstance: ");
        NewsFragment f = new NewsFragment();
        Bundle bdl = new Bundle(1);
        bdl.putSerializable("NewsData", news);
        bdl.putSerializable("INDEX", index);
        bdl.putSerializable("TOTAL_COUNT", max);
        f.setArguments(bdl);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment_layout = inflater.inflate(R.layout.fragment_news, container, false);
        Bundle args = getArguments();
        if (args != null) {
            final News currentNews = (News) args.getSerializable("NewsData");
            if (currentNews == null) {
                return null;
            }
            int index = args.getInt("INDEX");
            int total = args.getInt("TOTAL_COUNT");
            TextView headline = fragment_layout.findViewById(R.id.headline);
            headline.setText(currentNews.getTitle());
            headline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    click(currentNews.getUrl());
                }
            });

            TextView date = fragment_layout.findViewById(R.id.date);
            String d = currentNews.getPublishedAt();
            String formatDate;
            try {
                Date dat =  new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(d);
                String pattern = "MMM dd, yyyy HH:mm";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                formatDate = simpleDateFormat.format(dat);
                date.setText(formatDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            TextView author = fragment_layout.findViewById(R.id.author);
            author.setText(currentNews.getAuthor());
            TextView text = fragment_layout.findViewById(R.id.text);
            text.setText(currentNews.getDescription());
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    click(currentNews.getUrl());
                }
            });

            TextView pageNum = fragment_layout.findViewById(R.id.page);
            pageNum.setText(String.format(Locale.US, "%d of %d", index, total));

            ImageView imageView = fragment_layout.findViewById(R.id.image_news);
            imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            Picasso picasso = new Picasso.Builder(getActivity()).build();
            picasso.load(currentNews.getUrlToImage())
                    .error(R.drawable.not_found)
                    .placeholder(R.drawable.loading)
                    .into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    click(currentNews.getUrl());
                }
            });
            return fragment_layout;
        } else {
            return null;
        }
    }


    public void click(String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
}
