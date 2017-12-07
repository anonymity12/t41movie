package com.paul.t41popmovies.network.okhttp;

import android.content.Context;
import android.net.ConnectivityManager;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.CONNECTIVITY_SERVICE;


/**
 *
 *  网络功能类
 *  by paul, 这是真正在使用的，没有用到retrofit
 */

public class NetworkUtil {

    private static final String API_KEY = "3ac64d01d1011b15e9f200670abe0b0d";

    public static String sendRequestWithOkHttp(String typeString) {
        //make BASE_THEMOVIEDB_URL a local variable is good practice
        String BASE_THEMOVIEDB_URL = "https://api.themoviedb.org/3/movie/";
        String requestURL = BASE_THEMOVIEDB_URL + typeString;


        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(requestURL + "?api_key=" + API_KEY)
                    .build();
            //可能如下的url被构造了
            /*https://api.themoviedb.org/3/movie/popular?api_key=3ac64d01d1011b15e9f200670abe0b0d&language=en-US&page=1*/
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String sendRequestForVideoWithOkHttp(int movieId){
        //next we are going to build something like
        //https://api.themoviedb.org/3/movie/346364/videos?api_key=3ac64d01d1011b15e9f200670abe0b0d&language=en-US
        String BASE_the_moviedb_url = "https://api.themoviedb.org/3/movie/";
        String requestUrl = BASE_the_moviedb_url + movieId;
        System.out.println(requestUrl);

        try{
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(requestUrl + "/videos" + "?api_key=" + API_KEY)
                    .build();
            Response response = client.newCall(request).execute();
            String result = response.body().string();
            System.out.println(result);
            return result;
            //here we return Json data that contains YouTube trailer key
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    //this static method is for REVIEWS
    public static String sendRequestForReviewsWithOkHttp(int movieId){
        //https://api.themoviedb.org/3/movie/346364/reviews?api_key=3ac64d01d1011b15e9f200670abe0b0d&language=en-US&page=1
        String BaseTheMovieDbUrl = "https://api.themoviedb.org/3/movie/";
        String requestUrl = BaseTheMovieDbUrl + movieId;
        try{
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(requestUrl + "/reviews" + "?api_key=" + API_KEY)
                    .build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    
    public static String sendRequestForMultiMovies(List<Integer> movieIdList){
        String BaseTheMovieDbUrl = "https://api.themoviedb.org/3/movie/";
        try{
            OkHttpClient client = new OkHttpClient();
            Request request;
            Response response;
            StringBuilder stringBuilder = new StringBuilder("{\"results\":[");
            for (int movieId : movieIdList){
                request = new Request.Builder()
                        .url(BaseTheMovieDbUrl + movieId + "?api_key=" + API_KEY)
                        .build();
                response = client.newCall(request).execute();
                stringBuilder.append(response.body().string()+ ",");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);//remove the last ','
            stringBuilder.append("]}");//now here I think is OK, waiting for test! pass test
            return stringBuilder.toString();

        }catch (IOException e){
            e.printStackTrace();
        }
        return  null;
    }

    public static boolean isNetworkAvailableAndConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        //make isNetworkAvailable a inline variable is good practice
        return isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();
    }
}
