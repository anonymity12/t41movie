package com.paul.t41popmovies.db;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zheng-rt on 2017/11/12.
 * 获取reviews的lab，仿造movieLab
 */

public class ReviewsLab {
    private static ReviewsLab sReviewsLab;

    private String[] mReviewsList;

    private ReviewsLab(Context context){
        mReviewsList = new String[]{};
    }
    public static ReviewsLab get(Context context){
        if (sReviewsLab == null){
            sReviewsLab = new ReviewsLab(context);
        }
        return sReviewsLab;
    }
    public void setReviewsList(String [] reviewsList){
        mReviewsList = reviewsList;
    }
    public String [] getReviewsList(){
        return mReviewsList;
    }
}
