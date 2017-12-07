package com.paul.t41popmovies.util;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.paul.t41popmovies.db.FavoriteMovieContract;
import com.paul.t41popmovies.db.Movie;
import com.paul.t41popmovies.db.MovieLab;
import com.paul.t41popmovies.network.okhttp.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zheng-rt on 2017/11/22.
 */

public class ThirdLoader extends AsyncTaskLoader<Void> {

    private static final String TAG = "ThirdLoader";
    Cursor mFavoriteMovieData = null;

    public ThirdLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if (mFavoriteMovieData != null){
            queryFromNetwork(mFavoriteMovieData);
        }else {
            forceLoad();
        }

    }

    @Override
    public Void loadInBackground() {

        try {
            mFavoriteMovieData = getContext().getContentResolver().query(FavoriteMovieContract.MovieEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);
        } catch (Exception e) {
            Log.e(TAG, "Failed to asynchronously load data.");
            e.printStackTrace();
        }

        queryFromNetwork(mFavoriteMovieData);
        return null;
    }
    private void queryFromNetwork(Cursor favoriteMovieData){
        int idIndex = favoriteMovieData.getColumnIndex(FavoriteMovieContract.MovieEntry.COLUMN_ID);
        favoriteMovieData.moveToFirst();
        List<Integer> movieIdList = new ArrayList<>();
        //添加所有contentProvider里的id 数据到一个list里
        while(favoriteMovieData.moveToNext()){
            movieIdList.add(favoriteMovieData.getInt(idIndex));
        }
        String responseData = NetworkUtil.sendRequestForMultiMovies(movieIdList);
        //用json数据实例化一个movielist
        MovieLab movieLab = MovieLab.get(getContext());
        movieLab.setMovieList(parseJSON(responseData));

    }
    //解析json数据，返回movieList
    private List<Movie> parseJSON(String jsonData) {
        List<Movie> movies = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject movieObject = jsonArray.getJSONObject(i);
                Movie movie = new Movie();
                movie.setId(movieObject.getInt("id"));
                movie.setTitle(movieObject.getString("title"));
                movie.setBackdrop_path(movieObject.getString("backdrop_path"));
                movie.setOverview(movieObject.getString("overview"));
                movie.setVote_average(movieObject.getDouble("vote_average"));
                movie.setRelease_date(movieObject.getString("release_date"));
                movie.setPoster_path(movieObject.getString("poster_path"));
                movies.add(movie);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return movies;
    }
}
