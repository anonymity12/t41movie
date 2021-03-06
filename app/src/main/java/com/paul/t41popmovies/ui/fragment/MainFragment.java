package com.paul.t41popmovies.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.paul.t41popmovies.R;
import com.paul.t41popmovies.db.Movie;
import com.paul.t41popmovies.db.MovieLab;
import com.paul.t41popmovies.db.MoviePreferences;
import com.paul.t41popmovies.network.okhttp.NetworkUtil;
import com.paul.t41popmovies.ui.activity.DetailPagerActivity;
import com.paul.t41popmovies.ui.adapter.MainAdapter;
import com.paul.t41popmovies.ui.view.SpacesItemDecoration;
import com.paul.t41popmovies.util.MainLoader;
import com.paul.t41popmovies.util.ThirdLoader;

import java.util.List;

/**
 * Created by paul on 10/8/17.
 */

public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Void>, MainAdapter.ListItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView mMainRecyclerView;
    private MainAdapter mMainAdapter;
    private List<Movie> mMovieList;

    private SwipeRefreshLayout mMainRefresh;
    private TextView mMainErrorTextView;
    private ContentLoadingProgressBar mMainLoading;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        //想让Fragment中的onCreateOptionsMenu生效必须先调用setHasOptionsMenu方法
        //如果不调用这个方法，则在toolbar不显示菜单
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mMainErrorTextView = (TextView) view.findViewById(R.id.tv_main_error);
        mMainLoading = (ContentLoadingProgressBar) view.findViewById(R.id.pb_main_loading);
        mMainRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);

        mMainRefresh.setColorSchemeResources(R.color.colorPrimary);
        mMainRefresh.setOnRefreshListener(this);

        mMainRecyclerView = (RecyclerView) view.findViewById(R.id.rcv_main);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL);
        mMainRecyclerView.setLayoutManager(layoutManager);
        mMainRecyclerView.setHasFixedSize(true);
        mMainAdapter = new MainAdapter(view.getContext(), this);
        mMainRecyclerView.setAdapter(mMainAdapter);
        //set space for each item
        SpacesItemDecoration decoration = new SpacesItemDecoration(29);
        mMainRecyclerView.addItemDecoration(decoration);

        initData();
        return view;
    }

    /*
    *  2017/12/10 21:06 更新
    *  希望，直接离线从数据库里（ContentProvider的方式）加载数据，（注：此时使用CursorLoader），
    *  当SwipeRefreshLayout被下滑刷新后，进行一次网络请求（注：此时使用MainLoader加载流行和高分；使用ThirdLoader加载收藏；其实和阶段1.5一样。）
    *  在这里值得考虑的细节是：如何做好离线模块和在线模块的分离
    * */
    private void initData() {
        int prefer_loader_id = MoviePreferences.getPreferLoaderId(getContext());
        getLoaderManager().initLoader(prefer_loader_id, null, this);

//        if (NetworkUtil.isNetworkAvailableAndConnected(getContext())) {
//            showSuccessView();
//            getLoaderManager().initLoader(0, null, this);//int id, Bundle args, Callback callback
//        } else {
//            //在这里，如果没有网络，那么从本地的数据加载三种排序：1. 最流行表格，2. 最评分表格 3. 收藏表格
//            showErrorView();
//        }
    }

    private void showSuccessView() {
        mMainErrorTextView.setVisibility(View.INVISIBLE);
        mMainRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorView() {
        mMainRecyclerView.setVisibility(View.INVISIBLE);
        mMainErrorTextView.setVisibility(View.VISIBLE);
    }

    // 我们使用ThirdLoader来进行离线加载 三张表；
    @Override
    public Loader onCreateLoader(int loaderId, Bundle args) {
        mMainRecyclerView.setVisibility(View.INVISIBLE);
        mMainLoading.setVisibility(View.VISIBLE);//ContentLoadingProgressBar
        switch (loaderId){
            // 0 是载入流行，离线方式
            case 0:
                return new ThirdLoader(getContext(), getString(R.string.loader_popular));// AsyncTaskLoader<Void>??yes, an AsyncTaskLoader would be called. @11.11
            // 1 是载入评分，离线方式
            case 1:
                return new ThirdLoader(getContext(), getString(R.string.loader_top_rated));
            //break;注意：这里我们不需要break语句，因为break不可达，因为return在前面。
            //2 是载入收藏，离线方式
            case 2:
                return new ThirdLoader(getContext(),getString(R.string.loader_favorite));


        }
/* 这里的if else是之前的残留， 我们采用新的switch方式进行loaderId的判断。如上所示。
        if (i == 0) {
            return
            return new MainLoader(getContext(), getString(R.string.loader_popular));// AsyncTaskLoader<Void>??yes, an AsyncTaskLoader would be called. @11.11
        } else if (i == 1) {
            return new MainLoader(getContext(), getString(R.string.loader_top_rated));
        } else if ( i == 2) {
            return new ThirdLoader(getContext());
        }*/
        return null;
    }

    //loading结束，我们用movielist是否为空来显示成功界面或者错误界面
    @Override
    public void onLoadFinished(Loader<Void> loader, Void data) {
        mMainLoading.setVisibility(View.INVISIBLE);
        mMovieList = MovieLab.get(getContext()).getMovieList();
        if (mMovieList != null && !mMovieList.isEmpty()) {
            showSuccessView();
            mMainAdapter.refreshMovieList(mMovieList);
        } else {
            showErrorView();
        }
    }
    //重置loading
    @Override
    public void onLoaderReset(Loader loader) {
        mMainAdapter.refreshMovieList(null);
    }
    //在菜单中可以选择是：1：最流行排序，2.最多评分排序；使用了ArrayAdapter
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);

        MenuItem spinnerItem = menu.findItem(R.id.action_spinner);
        Spinner spinner = (Spinner) spinnerItem.getActionView();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.sort_order, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (NetworkUtil.isNetworkAvailableAndConnected(getContext())) {
                    onSortOderChange(i);
                } else {
                    showErrorView();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }
    //这是restartLoader（）；在最开始，创建Loader时有0和1两个loader（后来添加了2）；0是popular；1是top-reated,2是ThirdLoader，载入收藏的电影。
    private void onSortOderChange(int i) {
        switch (i) {
            case 0:
                //int id, Bundle args,LoaderManager.LoaderCallbacks<D> callback
                getLoaderManager().restartLoader(i, null, this);
                break;
            case 1:
                getLoaderManager().restartLoader(i, null, this);
                break;
            //here case 2 is to show the favorite movies
            case 2:
                getLoaderManager().restartLoader(i, null, this);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    //adapter里有一个ViewHolder，这个View的onClickListener会调用这个接口实现，可以说：我们的MainFragment是面向
    //接口编程的。
    //建议更名：onViewHolderClick(...)
    @Override
    public void onListItemClick(int clickedItemIndex) {

        Intent intent = DetailPagerActivity.newIntent(getContext(), mMovieList.get(clickedItemIndex));
        startActivity(intent);
    }

    //有个家伙调用了这个刷新：SwipeRefreshLayout
    @Override
    public void onRefresh() {
        initData();
        mMainRefresh.setRefreshing(false);
    }
}
