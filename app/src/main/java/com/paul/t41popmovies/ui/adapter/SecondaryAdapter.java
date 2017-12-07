package com.paul.t41popmovies.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.paul.t41popmovies.R;

/**
 * Created by Zheng-rt on 2017/11/12.
 */

public class SecondaryAdapter extends RecyclerView.Adapter<SecondaryAdapter.ReviewsHolder>{

    private String[] mReivews;

    @Override
    public SecondaryAdapter.ReviewsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1,parent, false);
        return new ReviewsHolder(view);
    }

    @Override
    public void onBindViewHolder(SecondaryAdapter.ReviewsHolder holder, int position) {
        String review = mReivews[position];
        holder.mReviewsTextView.setText(review);
    }

    @Override
    public int getItemCount() {
        return mReivews.length;
    }

    //  参考refreshMovieList(List<Movie> movies)@MainAdapter.java ，添加一个数据源和当前Adapter的联结
    //从这里注入reviews
    public void refreshReviewList(String[] reviews){
        mReivews = reviews;
        notifyDataSetChanged();
    }


    //我们也可以在Adapter里定义一个viewHolder，也可以像后来学的，在Fragment里定义
    class ReviewsHolder extends RecyclerView.ViewHolder{

        TextView mReviewsTextView;
        public ReviewsHolder(View itemView) {
            super(itemView);
            mReviewsTextView = (TextView) itemView;
        }
    }
}
