package com.paul.t41popmovies.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

/**
 * 提供工厂
 * MoviewLab提供的一个功能是：
 *
 * 在电影列表访问者之间，和电影列表之间形成桥梁
 *
 * movieLab构造方法里面有一个movieList，这就是lab的作用
 *
 *  2017/12/08 12:19  更新
 *  这次更新将使用数据库来作为 电影列表的提供者 ， 我们仍旧会给View层返回一个movieList，但是这次要对这个
 *  movieList进行数据库方面的封装
 */

public class MovieLab {
    private static MovieLab sMovieLab;

    private List<Movie> mMovieList;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    //单例模式，但是不是线程安全的单例模式
    public static MovieLab get(Context context) {
        if (sMovieLab == null) {
            sMovieLab = new MovieLab(context);
        }
        return sMovieLab;
    }

    private MovieLab(Context context) {
        //tt: 2017/12/07 21:29 我打算不需要这个MovieList做暂存，将会有一个movieList作为结果返回，但现在，不需要这个MovieList做暂存，我们会
        //使用SQLite 做存储；
//        mMovieList = new ArrayList<>();
        mDatabase = new MovieDbHelper(mContext).getWritableDatabase();
    }

    //tt: 添加一个获取单个movie的所有信息的便捷方法
    private static ContentValues getContentValues(Movie movie){
        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.COLUMN_ID, movie.getId());
        values.put(MovieContract.MovieEntry.COLUMN_NAME, movie.getTitle());
        values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, movie.getRelease_date());
        values.put(MovieContract.MovieEntry.COLUMN_POST_PATH, movie.getPoster_path());
        values.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, movie.getVote_average());
        values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, movie.getOverview());
        values.put(MovieContract.MovieEntry.COLUMN_FAVORITE, movie.getFavorite());//favorite is 0 or 1, a byte variable;
        return values;

    }
    // TODO: 2017/12/7 效仿criminal Intent app的数据库作为最后底层的架构


    /*
    * 删除（清空原有数据）和插入数据ContentProvider，此方法在网络请求结束的时刻，由网络请求结束接口 调用
    *
    * */
    public void syncMovies(ContentValues[] contentValues){
        if (contentValues != null && contentValues.length != 0){
            ContentResolver movieContentResolver = mContext.getContentResolver();
            movieContentResolver.delete(MovieContract.MovieEntry.CONTENT_URI,
                    null,
                    null);
            movieContentResolver.bulkInsert(MovieContract.MovieEntry.CONTENT_URI,
                    contentValues);
            //删除（清空原有数据）和插入已经完成，可选：外加通知   2017/12/08 21:31

        }

    }

    public void setMovieList(List<Movie> movieList) {
        mMovieList = movieList;
    }

    public List<Movie> getMovieList() {

        return mMovieList;
    }

}
