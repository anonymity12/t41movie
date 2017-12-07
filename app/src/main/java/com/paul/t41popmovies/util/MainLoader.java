package com.paul.t41popmovies.util;

/**
 * Created by paul on 10/8/17.
 */

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.paul.t41popmovies.db.Movie;
import com.paul.t41popmovies.db.MovieLab;
import com.paul.t41popmovies.network.okhttp.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/9/16 0016.
 * Loader是一个值得注意的知识点
 * Loader里，最主要的部分就是 loadInBackground()
 * MainLoader是被MainFragment里initData()函数里的getLoaderManager找到的
 * 根据传入的requestType去获得电影列表，后来也加入了为每一个电影请求留言的okHttp请求
 * 我们会在这里使用到一个独立的movieLab，这个单例模式的movieLab将会为我们提供一个movieList
 *
 */

public class MainLoader extends AsyncTaskLoader<Void> {

    private String mRequestType;


    //构造方法传入了requestType字符串；两个type：流行和评分
    public MainLoader(Context context, String requestType) {
        super(context);
        mRequestType = requestType;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public Void loadInBackground() {

        //得到json数据
        String responseData = NetworkUtil.sendRequestWithOkHttp(mRequestType);

        //用json数据实例化一个movielist
        MovieLab movieLab = MovieLab.get(getContext());
        movieLab.setMovieList(parseJSON(responseData));

        //在上述setMovieList后，我们可以再读取movieList获得各个电影的id，然后就能依据这个id，来得到trailer
        //得到trailer后再为对应的movie设置trailer
        List<Movie> movies =  movieLab.getMovieList();
        for(Movie movie : movies){
            int id = movie.getId();
            String jsonDataContainsTrailerKey = NetworkUtil.sendRequestForVideoWithOkHttp(id);
            String trailerKeyValue = parseTrailerInJson(jsonDataContainsTrailerKey);
            movie.setTrailer(trailerKeyValue);
            /// 下一步需要填充recyclerView。。
        }
        return null;
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
    //parse the videos json result
    private String parseTrailerInJson(String jsonDataContainsTrailerKey){
        String trailerKeyValue = null;
        try{
            JSONObject jsonObject = new JSONObject(jsonDataContainsTrailerKey);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            //we only fetch on result in results
            JSONObject videoObject = jsonArray.getJSONObject(0);
            trailerKeyValue = videoObject.getString("key");
            System.out.println("MainLoader >>>>>>>>>>>we got the key for trailer : " + trailerKeyValue);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return  trailerKeyValue;
    }

}
