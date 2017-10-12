package com.paul.t41popmovies.ui.fragment;

/**
 * Created by paul on 10/8/17.
 */

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.paul.t41popmovies.R;
import com.paul.t41popmovies.db.Movie;
import com.paul.t41popmovies.network.okhttp.NetworkUtil;
import com.squareup.picasso.Picasso;

/**
 *
 *
 * 细节Fragment，进行电影细节的显示，选择性加载，lazyLoad+缓存机制。
 */

public class DetailFragment extends LazyLoadFragment {
    public static final String ARG_MOVIE = "movie";

    private View mView;

    private TextView mTitleTextView;
    private ImageView mPosterImageView;
    private TextView mReleaseDateTextView;
    private TextView mVoteAverageTextView;
    private TextView mOverviewTextView;
    private TextView mDetailErrorTextView;

    private Movie mMovie;

    //标志位，标志已经初始化完成
    private boolean isPrepared;
    //是否已被加载过一次，第二次就不再去请求数据了
    private boolean mHasLoadedOnce;

    //外人调用
    public static DetailFragment newInstance(Movie movie) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_MOVIE, movie);
        DetailFragment detailFragment = new DetailFragment();
        detailFragment.setArguments(args);
        return detailFragment;
    }

    //初始化，实例周期第一步。
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);//保留Fragment的状态
        mMovie = getArguments().getParcelable(ARG_MOVIE);//取出实现Parcelable接口的movie对象
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.fragment_detail, container, false);
            mTitleTextView = (TextView) mView.findViewById(R.id.tv_detail_title);
            mPosterImageView = (ImageView) mView.findViewById(R.id.iv_detail_poster);
            mReleaseDateTextView = (TextView) mView.findViewById(R.id.tv_detail_release_date);
            mVoteAverageTextView = (TextView) mView.findViewById(R.id.tv_detail_vote_average);
            mOverviewTextView = (TextView) mView.findViewById(R.id.tv_detail_overview);
            mDetailErrorTextView = (TextView) mView.findViewById(R.id.tv_detail_error);
            isPrepared = true;
            lazyLoad();

        }
        ViewGroup parent = (ViewGroup) mView.getParent();
        if (parent != null) {
            parent.removeView(mView);
        }
        return mView;
    }

    private void showErrorView() {
        mTitleTextView.setVisibility(View.INVISIBLE);
        mPosterImageView.setVisibility(View.INVISIBLE);
        mReleaseDateTextView.setVisibility(View.INVISIBLE);
        mVoteAverageTextView.setVisibility(View.INVISIBLE);
        mOverviewTextView.setVisibility(View.INVISIBLE);
        mDetailErrorTextView.setVisibility(View.VISIBLE);
    }


    //onVisible()才会lazyLoad()

    @Override
    protected void lazyLoad() {
        //如果没有准备好，不是可见的，或者已经load过了，就直接返回，啥都不干
        //那么这说明，有缓存的存在！！！
        if (!isPrepared || !isVisible || mHasLoadedOnce) {
            return;
        }

        if (NetworkUtil.isNetworkAvailableAndConnected(getContext())) {
            initDetail();
            mHasLoadedOnce = true;
        } else {
            showErrorView();
        }
    }
    //初始化细节
    private void initDetail() {
        //标题
        mTitleTextView.setText(mMovie.getTitle());
        //图片
        Picasso.with(getContext())
                .load("http://image.tmdb.org/t/p/w185/" + mMovie.getPoster_path())
                .into(mPosterImageView);
        //日期信息
        mReleaseDateTextView.setText(mMovie.getRelease_date());
        //其他信息
        mVoteAverageTextView.setText(String.format("%2.1f", mMovie.getVote_average()));
        mOverviewTextView.setText(mMovie.getOverview());
    }
}
