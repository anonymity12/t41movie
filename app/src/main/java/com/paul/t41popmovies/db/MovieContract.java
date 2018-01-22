package com.paul.t41popmovies.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Zheng-rt on 2017/11/19.
 */


// completed 2017/12/5 应该添加新的entry，以支持更多项目
public class MovieContract {
    public static final String AUTHORITY = "com.paul.t41popmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_MOVIE = "movies";//this line will be deleted one day.
    public static final String PATH_FAV_MOVIE = "favorite_movies";
    public static final String PATH_POP_MOVIE = "popular_movies";
    public static final String PATH_VOTE_MOVIE = "vote_movies";

    public static final class MovieEntry implements BaseColumns{
        //we don't want to use it any more, we use following three CONTENT_URI;
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();//tt: this line should be deleted one day.
        //the content uri looks like: content://com.paul.t41popmoives/movies

        public static final String FAV_TABLE_NAME = "fav_movies";//table 1 : the favorite table
        public static final String POP_TABLE_NAME = "pop_movies";//table 2 : the popular table
        public static final String VOTE_TABLE_NAME = "vote_movies";//table 3 : the high vote table
        public static final String COLUMN_ID = "movie_id";// movie id
        public static final String COLUMN_NAME = "name";// movie title
        public static final String COLUMN_RELEASE_DATE = "release_date";// movie release date
        public static final String COLUMN_POST_PATH = "post_path";// movie poster path
        public static final String COLUMN_VOTE_AVERAGE = "average_vote";// movie vote average
        public static final String COLUMN_OVERVIEW = "overview";// movie overview
        public static final String COLUMN_FAVORITE = "favorite"; // is movie favorite?
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";// backdrop path for the viewpager

    }
    public static final class FavMovieEntry implements BaseColumns{
        //the content uri looks like: content://com.paul.t41popmoives/favorite_movies
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAV_MOVIE).build();
    }
    public static final class PopMovieEntry implements BaseColumns{
        //the content uri looks like: content://com.paul.t41popmoives/popular_movies
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_POP_MOVIE).build();
    }
    public static final class VoteMovieEntry implements BaseColumns{
        //the content uri looks like: content://com.paul.t41popmoives/vote_movies
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_VOTE_MOVIE).build();
    }
}
