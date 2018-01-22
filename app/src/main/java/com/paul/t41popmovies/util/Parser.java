package com.paul.t41popmovies.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Zheng-rt on 2017/12/11.
 */

public final class Parser {

    //parse the videos json result
    public static String parseTrailerInJson(String jsonDataContainsTrailerKey){
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
