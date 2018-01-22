package com.paul.t41popmovies.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Zheng-rt on 2017/11/19.
 */

public class MovieDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Movie.db";
    private static final int VERSION = 1;
    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    //保存id, title, release_date, post_path, vote_average;
    //here at the first launch the app, we create three table;
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE1 = "CREATE TABLE " + MovieContract.MovieEntry.FAV_TABLE_NAME +
                " (" + MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY, " +
                MovieContract.MovieEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                MovieContract.MovieEntry.COLUMN_NAME +" TEXT," +
                MovieContract.MovieEntry.COLUMN_RELEASE_DATE + " TEXT," +
                MovieContract.MovieEntry.COLUMN_POST_PATH + " TEXT," +
                MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE + " REAL," +
                MovieContract.MovieEntry.COLUMN_OVERVIEW + " TEXT," +
                MovieContract.MovieEntry.COLUMN_FAVORITE + " INTEGER NOT NULL,"
                + ");";
        db.execSQL(CREATE_TABLE1);
        final String CREATE_TABLE2 = "CREATE TABLE " + MovieContract.MovieEntry.POP_TABLE_NAME +
                " (" + MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY, " +
                MovieContract.MovieEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                MovieContract.MovieEntry.COLUMN_NAME +" TEXT," +
                MovieContract.MovieEntry.COLUMN_RELEASE_DATE + " TEXT," +
                MovieContract.MovieEntry.COLUMN_POST_PATH + " TEXT," +
                MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE + " REAL," +
                MovieContract.MovieEntry.COLUMN_OVERVIEW + " TEXT," +
                MovieContract.MovieEntry.COLUMN_FAVORITE + " INTEGER NOT NULL,"
                + ");";
        db.execSQL(CREATE_TABLE2);
        final String CREATE_TABLE3 = "CREATE TABLE " + MovieContract.MovieEntry.VOTE_TABLE_NAME +
                " (" + MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY, " +
                MovieContract.MovieEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                MovieContract.MovieEntry.COLUMN_NAME +" TEXT," +
                MovieContract.MovieEntry.COLUMN_RELEASE_DATE + " TEXT," +
                MovieContract.MovieEntry.COLUMN_POST_PATH + " TEXT," +
                MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE + " REAL," +
                MovieContract.MovieEntry.COLUMN_OVERVIEW + " TEXT," +
                MovieContract.MovieEntry.COLUMN_FAVORITE + " INTEGER NOT NULL,"
                + ");";
        db.execSQL(CREATE_TABLE3);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.FAV_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.POP_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.VOTE_TABLE_NAME);
        onCreate(db);
    }
}
