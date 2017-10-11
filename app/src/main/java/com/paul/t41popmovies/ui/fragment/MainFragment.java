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
import com.paul.t41popmovies.network.okhttp.NetworkUtil;
import com.paul.t41popmovies.ui.activity.DetailPagerActivity;
import com.paul.t41popmovies.ui.adapter.MainAdapter;
import com.paul.t41popmovies.util.MainLoader;

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
        LinearLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        mMainRecyclerView.setLayoutManager(layoutManager);
        mMainRecyclerView.setHasFixedSize(true);
        mMainAdapter = new MainAdapter(view.getContext(), this);
        mMainRecyclerView.setAdapter(mMainAdapter);

        initData();
        return view;
    }

    private void initData() {
        if (NetworkUtil.isNetworkAvailableAndConnected(getContext())) {
            showSuccessView();
            getLoaderManager().initLoader(0, null, this);//?
        } else {
            showErrorView();
        }
    }

    private void showSuccessView() {
        mMainErrorTextView.setVisibility(View.INVISIBLE);
        mMainRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorView() {
        mMainRecyclerView.setVisibility(View.INVISIBLE);
        mMainErrorTextView.setVisibility(View.VISIBLE);
    }

    //利用i来进行两种方式的loading,这是创建过程
    @Override
    public Loader onCreateLoader(int i, Bundle args) {
        mMainRecyclerView.setVisibility(View.INVISIBLE);
        mMainLoading.setVisibility(View.VISIBLE);//ContentLoadingProgressBar
        if (i == 0) {
            return new MainLoader(getContext(), getString(R.string.loader_popular));// AsyncTaskLoader<Void>??
        } else if (i == 1) {
            return new MainLoader(getContext(), getString(R.string.loader_top_rated));
        }
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
    //这是restartLoader（）；在最开始，创建Loader时有0和1两个loader；0是popular；1是top-reated
    private void onSortOderChange(int i) {
        switch (i) {
            case 0:
                //int id, Bundle args,LoaderManager.LoaderCallbacks<D> callback
                getLoaderManager().restartLoader(i, null, this);
                break;
            case 1:
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
    @Override
    public void onListItemClick(int clickedItemIndex) {
        //单页面，tt已删除DetailActivity
        //Intent intent = DetailActivity.newIntent(getContext(), mMovieList.get(clickedItemIndex));
        Intent intent = DetailPagerActivity.newIntent(getContext(), mMovieList.get(clickedItemIndex));
        startActivity(intent);
    }

    //有个家伙调用了这个刷新
    @Override
    public void onRefresh() {
        initData();
        mMainRefresh.setRefreshing(false);
    }
}