package com.paul.t41popmovies.ui.activity;

import android.support.v4.app.Fragment;

import com.paul.t41popmovies.ui.fragment.MainFragment;

public class MainActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new MainFragment();
    }
}
