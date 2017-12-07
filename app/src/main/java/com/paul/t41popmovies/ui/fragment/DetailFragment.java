package com.paul.t41popmovies.ui.fragment;

/**
 * Created by paul on 10/8/17.
 */

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.paul.t41popmovies.R;
import com.paul.t41popmovies.db.FavoriteMovieContract;
import com.paul.t41popmovies.db.Movie;
import com.paul.t41popmovies.db.ReviewsLab;
import com.paul.t41popmovies.network.okhttp.NetworkUtil;
import com.paul.t41popmovies.ui.adapter.SecondaryAdapter;
import com.paul.t41popmovies.util.SecondaryLoader;
import com.squareup.picasso.Picasso;

/**
 * 细节Fragment，进行电影细节的显示，选择性加载，lazyLoad+缓存机制。
 *
 *  11.11.2017 we add the recycler view for reviews and we should call all the necessary methods to show the reviews
 *  on recycler view.
 *
 *  All the methods contains:
 *
 *  1.
 */

public class DetailFragment extends LazyLoadFragment implements LoaderManager.LoaderCallbacks<Void> {
    public static final String ARG_MOVIE = "movie";

    private View mView;


    private TextView mTitleTextView;
    private ImageView mPosterImageView;
    private TextView mReleaseDateTextView;
    private TextView mVoteAverageTextView;
    private TextView mOverviewTextView;
    private TextView mDetailErrorTextView;
    private Button mTrailerButton;
    private ImageButton mFavoriteButton;
    private RecyclerView mReviewsRecyclerView;
    private String [] mReviewsList;
    private ContentLoadingProgressBar mReviewsLoading;
    private SecondaryAdapter mReviewsAdapter;

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
            mTrailerButton = (Button) mView.findViewById(R.id.trailer_button);
            mFavoriteButton = (ImageButton) mView.findViewById(R.id.favorite_button);
            mReviewsRecyclerView = (RecyclerView) mView.findViewById(R.id.reviews_recycler_view);
            mReviewsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mReviewsLoading = (ContentLoadingProgressBar) mView.findViewById(R.id.pb_reviews_loading);


            isPrepared = true;
            lazyLoad();

        }
        ViewGroup parent = (ViewGroup) mView.getParent();
        if (parent != null) {
            parent.removeView(mView);
        }
        return mView;
    }

    //关于showErrorView的一个疑问是：什么时候这些不可见的view再次可见呢？
    //是每次最外层的viewPager重新出现后，再次Load这些fragment的时候，这些View才是可见的吗？
    private void showErrorView() {
        mTitleTextView.setVisibility(View.INVISIBLE);
        mPosterImageView.setVisibility(View.INVISIBLE);
        mReleaseDateTextView.setVisibility(View.INVISIBLE);
        mVoteAverageTextView.setVisibility(View.INVISIBLE);
        mOverviewTextView.setVisibility(View.INVISIBLE);
        mDetailErrorTextView.setVisibility(View.VISIBLE);
        mTrailerButton.setVisibility(View.INVISIBLE);
        mReviewsRecyclerView.setVisibility(View.INVISIBLE);
    }
    private void showSuccessView(){
        mReviewsRecyclerView.setVisibility(View.VISIBLE);
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
            //在这里可以选择离线加载
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
        //日期
        mReleaseDateTextView.setText(mMovie.getRelease_date());
        //其他信息：平均评分，概述
        mVoteAverageTextView.setText(String.format("%2.1f", mMovie.getVote_average()));
        mOverviewTextView.setText(mMovie.getOverview());
        //设置YouTube url for the trailer button
        mTrailerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String urlData = "https://www.youtube.com/watch?v=" + mMovie.getTrailer();
                System.out.println(">>>>>>>>>>>>>urlData : "+urlData);
                Uri uri = Uri.parse(urlData);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int movieId = mMovie.getId();
                //2017/11/19 后台数据库以及cp已经准备好，在这里开始收藏功能
                if(movieId == 0) return;
                ContentValues cv = new ContentValues();
                cv.put(FavoriteMovieContract.MovieEntry.COLUMN_ID, movieId);
                cv.put(FavoriteMovieContract.MovieEntry.COLUMN_NAME, mMovie.getTitle());
                cv.put(FavoriteMovieContract.MovieEntry.COLUMN_RELEASE_DATE, mMovie.getRelease_date());
                cv.put(FavoriteMovieContract.MovieEntry.COLUMN_POST_PATH, mMovie.getBackdrop_path());
                Uri uri = getContext().getContentResolver().insert(FavoriteMovieContract.MovieEntry.CONTENT_URI, cv);
                if (uri != null){
                    Toast.makeText(getContext(),"Added to Favorite!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //loading the reviews， secondary Loader.
        getLoaderManager().initLoader(3,null,this);

    }

    // three methods were completed for recyclerView show,
    // we can refer or copy and review
    //the way we implement LoaderCallbacks in MainFragment, to implement the function of loading the reviews of a movie, and
    //show the reviews on recycler view;

    //开始loading，让recyclerView不可见，让progressBar可见，返回一个SecondaryLoader
    @Override
    public Loader<Void> onCreateLoader(int id, Bundle args) {
        mReviewsRecyclerView.setVisibility(View.INVISIBLE);
        mReviewsLoading.setVisibility(View.VISIBLE);
        return new SecondaryLoader(getContext(),mMovie.getId());//loading for reviews and the loader id is 3.
    }

    //loading 结束，我们用reviewList是否为空，来显示成功界面或者错误界面。
    @Override
    public void onLoadFinished(Loader<Void> loader, Void data) {
        mReviewsLoading.setVisibility(View.INVISIBLE);
        mReviewsList = ReviewsLab.get(getContext()).getReviewsList();
        if(mReviewsList != null && mReviewsList.length != 0){
            mReviewsRecyclerView.setVisibility(View.VISIBLE);
            //// reviews data set is here
            updateUI();

        }
    }

    @Override
    public void onLoaderReset(Loader<Void> loader) {
        mReviewsAdapter.refreshReviewList(null);
    }


    private void updateUI() {
        ReviewsLab reviewsLab = ReviewsLab.get(getActivity());
        String[] reviewsInLab = reviewsLab.getReviewsList();

        mReviewsAdapter = new SecondaryAdapter();
        mReviewsAdapter.refreshReviewList(reviewsInLab);
        mReviewsRecyclerView.setAdapter(mReviewsAdapter);


    }


    //useless code goes below, cause we use another Adapter i.e. SecondaryAdapter

//    //adapter for reviews
//    public class ReviewsAdapter extends RecyclerView.Adapter<ReviewHolder>{
//
//        @Override
//        public ReviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            return null;
//        }
//
//        @Override
//        public void onBindViewHolder(ReviewHolder holder, int position) {
//
//        }
//
//        @Override
//        public int getItemCount() {
//            return 0;
//        }
//    }
//    //Reviews 的viewHolder
//    private class ReviewHolder extends RecyclerView.ViewHolder{
//
//        public TextView mReviewTextView;
//        public ReviewHolder(View itemView) {
//            super(itemView);
//            mReviewTextView = (TextView) itemView;
//        }
//    }
}
