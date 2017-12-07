package com.paul.t41popmovies.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Zheng-rt on 2017/11/19.
 */


// completed 2017/12/5 应该添加新的entry，以支持更多项目
public class FavoriteMovieContract {
    public static final String AUTHORITY = "com.paul.t41popmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_MOVIE = "movies";

    public static final class MovieEntry implements BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();
        public static final String TABLE_NAME = "movies";
        public static final String COLUMN_ID = "movie_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_POST_PATH = "post_path";
        public static final String COLUMN_AVERAGE_VOTE = "average_vote";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_FAVORITE = "favorite";

    }
}
