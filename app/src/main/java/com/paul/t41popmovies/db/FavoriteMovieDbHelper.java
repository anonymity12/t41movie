package com.paul.t41popmovies.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Zheng-rt on 2017/11/19.
 */

public class FavoriteMovieDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "favoriteMovie.db";
    private static final int VERSION = 1;
    public FavoriteMovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    //保存id, title, release_date, post_path, vote_average;
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE = "CREATE TABLE " + FavoriteMovieContract.MovieEntry.TABLE_NAME +
                " (" + FavoriteMovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY, " +
                FavoriteMovieContract.MovieEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                FavoriteMovieContract.MovieEntry.COLUMN_NAME +" TEXT," +
                FavoriteMovieContract.MovieEntry.COLUMN_RELEASE_DATE + " TEXT," +
                FavoriteMovieContract.MovieEntry.COLUMN_POST_PATH + " TEXT," +
                FavoriteMovieContract.MovieEntry.COLUMN_AVERAGE_VOTE + " REAL," +
                FavoriteMovieContract.MovieEntry.COLUMN_OVERVIEW + " TEXT," +
                FavoriteMovieContract.MovieEntry.COLUMN_FAVORITE + " INTEGER NOT NULL,"
                + ");";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavoriteMovieContract.MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
