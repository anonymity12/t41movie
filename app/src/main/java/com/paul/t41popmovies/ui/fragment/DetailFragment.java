package com.paul.t41popmovies.ui.fragment;

/**
 * Created by paul on 10/8/17.
 */

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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
import com.paul.t41popmovies.db.MovieContract;
import com.paul.t41popmovies.db.Movie;
import com.paul.t41popmovies.db.ReviewsLab;
import com.paul.t41popmovies.network.okhttp.NetworkUtil;
import com.paul.t41popmovies.ui.adapter.SecondaryAdapter;
import com.paul.t41popmovies.util.SecondaryLoader;
import com.squareup.picasso.Picasso;

import static com.paul.t41popmovies.util.Parser.parseTrailerInJson;

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
    private Button mFavoriteButton;
    private RecyclerView mReviewsRecyclerView;
    private String [] mReviewsList;
    private ContentLoadingProgressBar mReviewsLoading;
    private SecondaryAdapter mReviewsAdapter;

    private Movie mMovie;

    //标志位，标志已经初始化完成
    private boolean isPrepared;
    //是否已被加载过一次，第二次就不再去请求数据了
    private boolean mHasLoadedOnce;

    //外人（谁呢？）调用
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
            mTrailerButton.setVisibility(View.INVISIBLE);
            mFavoriteButton = (Button) mView.findViewById(R.id.favorite_button);
            mReviewsRecyclerView = (RecyclerView) mView.findViewById(R.id.reviews_recycler_view);
            mReviewsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mReviewsLoading = (ContentLoadingProgressBar) mView.findViewById(R.id.pb_reviews_loading);


            isPrepared = true;
            lazyLoad();

        }
        ViewGroup parent = (ViewGroup) mView.getParent();
        if (parent != null) {
            parent.removeView(mView);//parent大概就是DetailPagerActivity布局文件里的activity_movie_view_pager吧
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
    //初始化细节,这是在线方式下的 细节加载 ；在线加载网络图片和trailer，离线加载： 日期，评分，概述；
    private void initDetail() {
        //标题
        mTitleTextView.setText(mMovie.getTitle());
        //图片 2017/12/14 11:50 Picasso应加载本地图片。
        Picasso.with(getContext())
                .load("http://image.tmdb.org/t/p/w185/" + mMovie.getPoster_path())
                .into(mPosterImageView);
        //日期
        mReleaseDateTextView.setText(mMovie.getRelease_date());
        //其他信息：平均评分，概述
        mVoteAverageTextView.setText(String.format("%2.1f", mMovie.getVote_average()));
        mOverviewTextView.setText(mMovie.getOverview());
        //为当前movie加载trailer信息。天天标记 2017/12/11 10:57
        //我希望，以下的网络请求应不是异步的，就算OKHTTPS是开启了新的线程，也希望后面的button.setOnClickListener()能够
        //等待网络请求，解析，movie对象的trailer字段设定完成。不然会看到点击button而没有trailer的bug。(i.e. 这里可能存在bug！）
        final int id = mMovie.getId();
        String jsonDataContainsTrailerKey = NetworkUtil.sendRequestForVideoWithOkHttp(id);
        String trailerKeyValue = parseTrailerInJson(jsonDataContainsTrailerKey);
        //following two lines aim to query the movie with id in FavTable.
        Uri movieUri = Uri.parse(MovieContract.FavMovieEntry.CONTENT_URI +"/" + id);
        Cursor movieCursor = getContext().getContentResolver().query(movieUri,null,null,null,null);
        // 通过cursor读到的isFav值来设置按钮样式。
        int isFav = 0;
        if (movieCursor.moveToFirst()){
            int isFavIndex = movieCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_FAVORITE);
            isFav = movieCursor.getInt(isFavIndex);
        }
        mFavoriteButton.setText(isFav == 1 ? R.string.remove_from_favorite : R.string.addtofavorite);

        mMovie.setTrailer(trailerKeyValue);
        //设置YouTube url for the trailer button
        mTrailerButton.setVisibility(View.VISIBLE);
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
        final int finalIsFav = isFav;
        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ContentValues cv = new ContentValues();
                cv.put(MovieContract.MovieEntry.COLUMN_FAVORITE, (finalIsFav == 1 ? 0 : 1));
                // TODO: 2017/12/25 go on update it, please
                Uri uri = getContext().getContentResolver().update(MovieContract.MovieEntry.CONTENT_URI,cv);
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
