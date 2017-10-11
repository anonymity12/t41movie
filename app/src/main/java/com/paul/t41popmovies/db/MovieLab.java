package com.paul.t41popmovies.db;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/9/18 0018.
 */


/*
* MoviewLab提供的一个功能是：
*
* 在电影列表访问者之间，和各个电影之间形成桥梁
* */
public class MovieLab {
    private static MovieLab sMovieLab;

    private List<Movie> mMovieList;

    //修改为private是preferred
    private MovieLab(Context context) {
        mMovieList = new ArrayList<>();
    }

    //单例模式，但是不是线程安全的单例模式
    public static MovieLab get(Context context) {
        if (sMovieLab == null) {
            sMovieLab = new MovieLab(context);
        }
        return sMovieLab;
    }

    public void setMovieList(List<Movie> movieList) {
        mMovieList = movieList;
    }

    public List<Movie> getMovieList() {
        return mMovieList;

    }

    public Movie getMovie(int id) {
        for (Movie movie :
                mMovieList) {
            if (movie.getId() == id) {
                return movie;
            }
        }
        return null;
    }
}
