package com.paul.t41popmovies.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.paul.t41popmovies.db.MovieContract.MovieEntry.TABLE_NAME;

/**
 * Created by Zheng-rt on 2017/11/19.
 */

//此 contentProvider类主要提供我们一个 保存喜欢的电影的id的insert功能， 一个查询所有喜欢的电影的id integer list的query功能

public class MovieContentProvider extends ContentProvider {
    public static final int MOVIES = 100;
    public static final int MOVIES_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIE,MOVIES);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIE + "/#", MOVIES_WITH_ID);
        return uriMatcher;
    }
    private MovieDbHelper movieDbHelper;
    @NonNull
    @Override
    public boolean onCreate() {
        Context context = getContext();
        movieDbHelper = new MovieDbHelper(context);
        return true;
    }

    //query 方法会返回一个cursor对象，你可以在其地方（可能是loader）使用这个cursor来获取movieId  integer list数据，供查询使用。
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = movieDbHelper.getReadableDatabase();
        int matcher = sUriMatcher.match(uri);
        Cursor retCursor;
        switch(matcher){
            case MOVIES:
                retCursor = db.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknow uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(),uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //insert 方法会为我们保存一个movie的一些信息：
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = movieDbHelper.getReadableDatabase();
        int matcher = sUriMatcher.match(uri);
        Uri returnUri;
        switch(matcher){
            case MOVIES:
                long id = db.insert(TABLE_NAME,null,values);
                if (id > 0){
                    returnUri = ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI, id);
                }else{
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    //ContentProvider封装的delete方法，虽然好像没有直接的调用（任何contentResolver的方法调用，用Ctrl + B 查看都会直接跳到官方上层源码），但是应该是某种代理的方式会委托到此方法吧。
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();
        int numRowsDeleted;

        if(null == selection) selection = "1";
        switch(sUriMatcher.match(uri)){
            case MOVIES:
                numRowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numRowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return numRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
