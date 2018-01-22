package com.paul.t41popmovies.util;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.paul.t41popmovies.db.MovieContract;
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
 *
 * Maybe I think this Third Loader is born for offline loading
 */

public class ThirdLoader extends AsyncTaskLoader<Void> {

    private static final String TAG = "ThirdLoader";
    Cursor mMovieCursorData = null;
    private String mTableName = "pop";//default to pop
    public ThirdLoader(Context context,String tableName) {
        super(context);
        this.mTableName = tableName;
    }
/* since we are retrieve all movie info from database, we don't need to preLoad from network;
(almost all info(8 segments), such as
id,
title,
release date,
poster path,
vote average,
overview,
is Favorite?,
the backdrop pic path for view pager)

    @Override
    protected void onStartLoading() {
        if (mMovieCursorData != null){
            queryFromNetwork(mMovieCursorData);
        }else {
            forceLoad();
        }

    }*/

    @Override
    public Void loadInBackground() {
        switch (mTableName){
            //注： case 语句希望后边是一个常量，所以你使用诸如 ： getString(R.string.loader_popular) 的方式会红线！
            case "pop" :
                //retrieve the pop movie table;
                try{
                    mMovieCursorData = getContext().getContentResolver().query(MovieContract.PopMovieEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);
                }catch (Exception e){
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                }
                break;
            case "vote":
                //retrieve the high vote movie table;
                try{
                    mMovieCursorData = getContext().getContentResolver().query(MovieContract.VoteMovieEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);
                }catch (Exception e){
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                }
                break;
            case "fav" :
                //retrieve the favorite movie table;
                try {
                    mMovieCursorData = getContext().getContentResolver().query(MovieContract.FavMovieEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            null);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                }

        }
//        queryFromNetwork(mMovieCursorData);
        //tt: this is how I use new Loader to set new movie list;
        MovieLab movieLab = MovieLab.get(getContext());
        movieLab.setMovieList(getMovieListFromCursor(mMovieCursorData));

        return null;
    }

    private List<Movie> getMovieListFromCursor(Cursor movieCursorData){
        List<Movie> movieList = new ArrayList<>();
        if (movieCursorData != null){
            movieCursorData.moveToFirst();
        }
        int movieId;
        String movieTitle;
        String backdrop_path;
        String overview;
        double vote_average;
        String release_date;
        String poster_path;
        while(movieCursorData.moveToNext()){
            //获取cursor里的数据索引 index
            int idIndex = movieCursorData.getColumnIndex(MovieContract.MovieEntry.COLUMN_ID);
            int titleIndex = movieCursorData.getColumnIndex(MovieContract.MovieEntry.COLUMN_NAME);
            int backdrop_pathIndex = movieCursorData.getColumnIndex(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH);
            int overviewIndex = movieCursorData.getColumnIndex(MovieContract.MovieEntry.COLUMN_OVERVIEW);
            int vote_averageIndex = movieCursorData.getColumnIndex(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE);
            int release_dateIndex = movieCursorData.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
            int poster_pathIndex = movieCursorData.getColumnIndex(MovieContract.MovieEntry.COLUMN_POST_PATH);
            //获取 各个索引对应 的 基本数据类型们
            movieId = movieCursorData.getInt(idIndex);
            movieTitle = movieCursorData.getString(titleIndex);
            backdrop_path = movieCursorData.getString(backdrop_pathIndex);
            overview = movieCursorData.getString(overviewIndex);
            vote_average = movieCursorData.getDouble(vote_averageIndex);
            release_date = movieCursorData.getString(release_dateIndex);
            poster_path = movieCursorData.getString(poster_pathIndex);
            // 封装出一个movie对象
            Movie movie = new Movie();
            movie.setId(movieId);
            movie.setTitle(movieTitle);
            movie.setBackdrop_path(backdrop_path);
            movie.setOverview(overview);
            movie.setVote_average(vote_average);
            movie.setRelease_date(release_date);
            movie.setPoster_path(poster_path);
            //添加上述movie对象到movieList中。
            movieList.add(movie);
        }
        return movieList;
    }
    /*
    * 这个方法是取得一个一个movieId，再去网络请求之，故是在线的方式。
    * */
    private void queryFromNetwork(Cursor favoriteMovieData){
        int idIndex = favoriteMovieData.getColumnIndex(MovieContract.MovieEntry.COLUMN_ID);
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
