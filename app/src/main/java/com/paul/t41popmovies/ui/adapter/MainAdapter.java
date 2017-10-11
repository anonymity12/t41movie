package com.paul.t41popmovies.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.paul.t41popmovies.db.Movie;
import com.paul.t41popmovies.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 10/8/17.
 */

public class MainAdapter  extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {

    private Context mContext;
    private List<Movie> mMovies = new ArrayList<>();

    final private ListItemClickListener mOnClickListener;

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }


    public MainAdapter(Context context, ListItemClickListener listener) {
        mContext = context;
        mOnClickListener = listener;
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
        Picasso.with(mContext)
                .load("http://image.tmdb.org/t/p/w185/" + movie.getPoster_path())
                .into(holder.mMainImageView);
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
        public ImageView mMainImageView;

        public MainViewHolder(View itemView) {
            super(itemView);
            mMainImageView = (ImageView) itemView.findViewById(R.id.iv_main);
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