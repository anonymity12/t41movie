package com.paul.t41popmovies.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.paul.t41popmovies.db.Movie;
import com.paul.t41popmovies.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by paul on 10/8/17.
 */

public class MainAdapter  extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    private Context mContext;
    private List<Movie> mMovies = new ArrayList<>();
    private int[] heights;
//final 是什么效果，请仔细思考，实现此接口的MainFragment类，将必然拥有这个不可更改的对象。
    final private ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }


    public MainAdapter(Context context, ListItemClickListener listener) {
        mContext = context;
        mOnClickListener = listener;
        //取消随机高度，使App朴素点
//        this.heights = new int[]{400,450,500,350};
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.main_list_item, parent, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        Movie movie = mMovies.get(position);
//取消随机高度，使App朴素点
//        ViewGroup.LayoutParams params = holder.mMainImageView.getLayoutParams();
//        params.height = heights[(int)(Math.random()*4)];
//        holder.mMainImageView.setLayoutParams(params);

        Picasso.with(mContext)
                .load("http://image.tmdb.org/t/p/w185/" + movie.getPoster_path())
                .into(holder.mMainImageView);
        holder.mTextView.setText(movie.getTitle());

    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }

    public void refreshMovieList(List<Movie> movies) {
        mMovies = movies;
        notifyDataSetChanged();
    }





    //主页面ViewHolder
    class MainViewHolder extends RecyclerView.ViewHolder  implements View.OnClickListener{
        ImageView mMainImageView;
        TextView mTextView;

        MainViewHolder(View itemView) {
            super(itemView);
            mMainImageView = (ImageView) itemView.findViewById(R.id.iv_main);
            mTextView = (TextView) itemView.findViewById(R.id.tv_title);

            itemView.setOnClickListener(this);
        }

        //从adapter里获得位置，传递到ListItemClickListener接口里的onListItemClick(int clickedPosition)
        //方法里
        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);
        }
    }
}
