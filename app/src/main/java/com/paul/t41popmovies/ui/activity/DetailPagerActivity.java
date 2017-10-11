package com.paul.t41popmovies.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.paul.t41popmovies.R;
import com.paul.t41popmovies.db.Movie;
import com.paul.t41popmovies.db.MovieLab;
import com.paul.t41popmovies.ui.fragment.DetailFragment;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by paul on 10/8/17.
 */

/*细节activity，关于如何显示一个详细的电影信息界面，并且支持左右滑动到附近的page，支持分享*/
public class DetailPagerActivity extends AppCompatActivity {
    private static final String EXTRA_MOVIE = DetailPagerActivity.class.getSimpleName();

    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private Toolbar mToolbar;
    private ImageView mBackdropImageView;

    private ViewPager mViewPager;
    private List<Movie> mMovieList;
    private Movie mMovie;

    public static Intent newIntent(Context context, Movie movie) {
        Intent i = new Intent(context, DetailPagerActivity.class);
        i.putExtra(EXTRA_MOVIE, movie);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_pager);//大图片toolbar的布局
        mMovie = getIntent().getParcelableExtra(EXTRA_MOVIE);

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mBackdropImageView = (ImageView) findViewById(R.id.iv_detail_backdrop);//大图片
        mViewPager = (ViewPager) findViewById(R.id.activity_movie_view_pager);

        initCollapsingToolBar();
        initViewPager();
    }

    private void initViewPager() {
        mMovieList = MovieLab.get(this).getMovieList();
        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                Movie movie = mMovieList.get(position);
                return DetailFragment.newInstance(movie);
            }

            @Override
            public int getCount() {
                return mMovieList.size();
            }
        });

        //ViewPager滑动监听//09271638
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            //当选中一个页面后，通过page的位置映射到movelist的对应位置，进行图片加载
            @Override
            public void onPageSelected(int position) {
                mMovie = mMovieList.get(position);
                Picasso.with(DetailPagerActivity.this)
                        .load("http://image.tmdb.org/t/p/w780/" + mMovieList.get(position).getBackdrop_path())
                        .into(mBackdropImageView);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //以下的for循环作用是：ViewPager默认值显示PageAdapter中的第一个列表项
        //要显示列表项，可设置ViewPager当前要显示的列表项为movie数组中指定位置的列表项
        for (int i = 0; i < mMovieList.size(); i++) {
            if (mMovieList.get(i).getId() == mMovie.getId()) {
                mViewPager.setCurrentItem(i);//i是一个指示器吗?恩，指示我们应该把当前item设置到哪个位置
                break;
            }
        }
    }

    //初始化伸展toolbar
    private void initCollapsingToolBar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mCollapsingToolbarLayout.setTitleEnabled(true);
        Picasso.with(this)
                .load("http://image.tmdb.org/t/p/w780/" + mMovie.getBackdrop_path())
                .into(mBackdropImageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                share();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void share() {
        String mineType = "text/plain";
        String title = getString(R.string.share_title);
        ShareCompat.IntentBuilder
                .from(this)
                .setType(mineType)
                .setText(getString(R.string.share_from)
                        + mMovie.getTitle()
                        + getString(R.string.share_release_date)
                        + mMovie.getRelease_date()
                        + getString(R.string.share_overview)
                        + mMovie.getOverview())
                .setChooserTitle(title)
                .startChooser();
    }
}
