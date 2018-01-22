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

import static com.paul.t41popmovies.db.MovieContract.MovieEntry.FAV_TABLE_NAME;
import static com.paul.t41popmovies.db.MovieContract.MovieEntry.POP_TABLE_NAME;
import static com.paul.t41popmovies.db.MovieContract.MovieEntry.VOTE_TABLE_NAME;

/**
 * 此ContentProvider子类将会提供三张表的访问能力
 * 需要好好检查 此 MovieContentProvider 类 ，担心错误的表格交错操作
 * Created by Zheng-rt on 2017/11/19.
 */

//此 contentProvider类主要提供我们一个 保存喜欢的电影的id的insert功能， 一个查询所有喜欢的电影的id integer list的query功能


public class MovieContentProvider extends ContentProvider {
//    public static final int MOVIES = 100;//will not be use
    public static final int FAV_MOVIES = 200;
    public static final int POP_MOVIES = 300;
    public static final int VOTE_MOVIES = 400;
    public static final int FAV_MOVIES_WITH_ID = 201;
    public static final int POP_MOVIES_WITH_ID = 301;
    public static final int VOTE_MOVIES_WITH_ID = 401;

    public static final int MOVIES_WITH_ID = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_FAV_MOVIE,FAV_MOVIES);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_FAV_MOVIE + "/#", FAV_MOVIES_WITH_ID);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_POP_MOVIE,POP_MOVIES);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_POP_MOVIE + "/#", POP_MOVIES_WITH_ID);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_VOTE_MOVIE,VOTE_MOVIES);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_VOTE_MOVIE + "/#", VOTE_MOVIES_WITH_ID);
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

    //query 方法会返回一个cursor对象，
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = movieDbHelper.getReadableDatabase();
        int matcher = sUriMatcher.match(uri);
        Cursor retCursor;
        // COMPLETED: 2017/12/18 请在这里区分uri然后进行db查询，你可以先不用MyApplication里的SQLiteQueryBuilder
        switch(matcher){
            case FAV_MOVIES:
                retCursor = db.query(FAV_TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case POP_MOVIES:
                retCursor = db.query(POP_TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case VOTE_MOVIES:
                retCursor = db.query(VOTE_TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
                //此方法用于DetailFragment  的  收藏 按钮
            case FAV_MOVIES_WITH_ID:
                String moiveWithId = uri.getLastPathSegment();
                String[] selectionArgument = new String[] {moiveWithId};

                retCursor = db.query(MovieContract.MovieEntry.FAV_TABLE_NAME,
                        projection,
                        MovieContract.MovieEntry.COLUMN_ID + " = ? ",
                        selectionArgument,
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

    //insert 方法会为我们保存一个收藏 的 movie的一些信息，仅仅发生在fav table里
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = movieDbHelper.getReadableDatabase();
        int matcher = sUriMatcher.match(uri);
        Uri returnUri;
        long id;
        switch(matcher){
            //我认为1,3case可以删除了，pop和vote表格的insert是不会发生的。我们仅仅想fav table 插入数据
            case POP_MOVIES:
                id = db.insert(POP_TABLE_NAME,null,values);
                if (id > 0){
                    returnUri = ContentUris.withAppendedId(MovieContract.PopMovieEntry.CONTENT_URI, id);
                }else{
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            case FAV_MOVIES:
                id = db.insert(FAV_TABLE_NAME,null,values);
                if (id > 0){
                    returnUri = ContentUris.withAppendedId(MovieContract.FavMovieEntry.CONTENT_URI, id);
                }else{
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            case VOTE_MOVIES:
                id = db.insert(VOTE_TABLE_NAME, null, values);
                if (id > 0){
                    returnUri = ContentUris.withAppendedId(MovieContract.VoteMovieEntry.CONTENT_URI, id);
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

    /*bulkInsert 是  向pop和vote两张表 大量插入的 方法*/
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values){
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();
        int rowsInserted = 0;
        switch (sUriMatcher.match(uri)){
            case POP_MOVIES:
                db.beginTransaction();
                try{
                    for(ContentValues cv : values){
                        long _id = db.insert(MovieContract.MovieEntry.POP_TABLE_NAME, null, cv);
                        if (_id != -1){
                            rowsInserted ++;
                        }
                    }
                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }
                if (rowsInserted > 0) getContext().getContentResolver().notifyChange(uri,null);
                return rowsInserted;
            case VOTE_MOVIES:
                db.beginTransaction();
                rowsInserted = 0;
                try{
                    for(ContentValues cv : values){
                        long _id = db.insert(MovieContract.MovieEntry.VOTE_TABLE_NAME, null, cv);
                        if (_id != -1){
                            rowsInserted ++;
                        }
                    }
                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }
                if (rowsInserted > 0) getContext().getContentResolver().notifyChange(uri,null);
                return rowsInserted;
            default:
                return super.bulkInsert(uri,values);

        }
    }

    //ContentProvider封装的delete方法，虽然好像没有直接的调用（任何contentResolver的方法调用，用Ctrl + B 查看都会直接跳到官方上层源码），但是应该是某种代理的方式会委托到此方法吧。
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = movieDbHelper.getWritableDatabase();
        int numRowsDeleted;

        if(null == selection) selection = "1";
        switch(sUriMatcher.match(uri)){
            //或许此case不需要，因为应该不存在清空fav table 的需求
            case FAV_MOVIES:
                numRowsDeleted = db.delete(MovieContract.MovieEntry.FAV_TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case POP_MOVIES:
                numRowsDeleted = db.delete(MovieContract.MovieEntry.POP_TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case VOTE_MOVIES:
                numRowsDeleted = db.delete(MovieContract.MovieEntry.VOTE_TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            //删除一个单独的，不再收藏的电影 从 fav table 里
            case FAV_MOVIES_WITH_ID:
                String movieId = uri.getLastPathSegment();
                String[] mSelectionArgs = new String[]{movieId};
                numRowsDeleted = db.delete(MovieContract.MovieEntry.FAV_TABLE_NAME,MovieContract.MovieEntry.COLUMN_ID + " = ?",mSelectionArgs);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numRowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return numRowsDeleted;
    }

    //或许确实不需要update方法，我们若是喜欢一个电影，用insert into fav table即可。
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
