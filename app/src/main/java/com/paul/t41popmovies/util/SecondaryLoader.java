package com.paul.t41popmovies.util;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.paul.t41popmovies.db.ReviewsLab;
import com.paul.t41popmovies.network.okhttp.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Zheng-rt on 2017/11/11.
 *
 * this loader is for loading reviews.
 */

public class SecondaryLoader  extends AsyncTaskLoader<Void> {
    private int mMovieId = 0;
    public SecondaryLoader(Context context, int movieId) {
        super(context);
        mMovieId = movieId;

    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }


    //在这里能分离地放置好数据在一个reviewsLab里。
    @Override
    public Void loadInBackground() {
        ReviewsLab reviewsLab = ReviewsLab.get(getContext());//Loader 的 an application context；
        String jsonDataContainReviewsContent = NetworkUtil.sendRequestForReviewsWithOkHttp(mMovieId);
        System.out.println("SecondaryLoader >>>>>>>>>>>>>>>>" +jsonDataContainReviewsContent);
        String[] reviews = parseReviewContentInJson(jsonDataContainReviewsContent);
        reviewsLab.setReviewsList(reviews);
        return null;
    }

    //parse the reviews json result
    private String[] parseReviewContentInJson(String jsonDataContainsReviewContent){
        String[] reviewContents = new String[40];
        try{
            JSONObject jsonObject = new JSONObject(jsonDataContainsReviewContent);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            for(int i = 0; i < jsonArray.length(); i ++){
                JSONObject reviewObject = jsonArray.getJSONObject(i);
                reviewContents[i] = reviewObject.getString("content");
            }

        }catch (JSONException e){
            e.printStackTrace();
        }
        return reviewContents;

    }
}
