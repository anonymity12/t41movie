package com.paul.t41popmovies.ui.fragment;

/**
 * Created by paul on 10/8/17.
 */

import android.support.v4.app.Fragment;

/**
 *
 * 抽象类，以用户意愿来选择性加载Fragment（lazyLoad）
 */

public abstract class LazyLoadFragment extends Fragment {

    /**
     * Fragment当前状态是否可见
     */
    protected boolean isVisible;


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (getUserVisibleHint()) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInvisible();
        }
    }


    /**
     * 可见
     */
    protected void onVisible() {
        lazyLoad();
    }


    /**
     * 不可见
     */
    protected void onInvisible() {

    }


    /**
     * 延迟加载
     * 子类必须重写此方法
     */
    protected abstract void lazyLoad();
}
