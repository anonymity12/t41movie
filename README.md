# t41movie

title: 041PopMovie
date: 2017-10-10 16:02:34
tags: android
categories: code

---

T41PopMovie



[TOC]

# 一，从依赖入手

```
compile 'com.android.support:design:25.3.1'
compile 'com.squareup.picasso:picasso:2.5.2'
compile 'com.squareup.retrofit2:retrofit:2.3.0'
compile 'com.squareup.retrofit2:adapter-rxjava2:2.3.0'
compile 'com.squareup.retrofit2:converter-gson:2.3.0'
compile 'com.squareup.okhttp3:okhttp:3.9.0'
compile 'io.reactivex.rxjava2:rxjava:2.1.3'
compile 'io.reactivex.rxjava2:rxandroid:2.0.1'
```

可以看到几个有名的依赖：

picasso, retrofit2, okhttp3, rxjava2

其使用的地方分别是图片加载，网络，以及有关于异步处理的方面

# 二，从看得见的入手

## MainActivity

这个工程里一般都采用使用一个Activity托管一个fragment的方式，所以MainActivity在继承`SingleFragmentActivity`
后，按照`SingleFragmentActivity`的要求，实现了`abstract Fragment createFragment();`方法。

然后在给定的`activity_fragment`布局里创建了对应位置（仅有一个FrameLayout的那个位置）上的fragment：MainFragment

## MainFragment

这是个需要细细讲解的fragment

### 先从布局来看：

`FrameLayout`里包含一个`SwipeRefreshLayout`（SwipeRefreshLayout里面又有个很主要的`RecyclerView`）

一个textView和一个`ContentLoadingProgressBar`

### 再从代码角度看：

190行代码里

#### 首先从其实现的接口看看：

` LoaderManager.LoaderCallbacks<Void>, MainAdapter.ListItemClickListener, SwipeRefreshLayout.OnRefreshListener`

一个关于Loader的callback，一个适配器的项目点击处理的监听器，一个滑动刷新的监听器

#### 然后看看类内部的：

首先是一些控件对象的变量声明

```
private RecyclerView mMainRecyclerView;
private MainAdapter mMainAdapter;
private List<Movie> mMovieList;

private SwipeRefreshLayout mMainRefresh;
private TextView mMainErrorTextView;
private ContentLoadingProgressBar mMainLoading;
```


然后可以看到一般的fragment的声明周期的前面两个事件：

`onCreate`和`onCreateView`

在`onCreate()`里，就只有一下两行，其目的大概是在设备方向改变是保持fragment状态，和他注释里说的：

```
setRetainInstance(true);
//想让Fragment中的onCreateOptionsMenu生效必须先调用setHasOptionsMenu方法
setHasOptionsMenu(true);
```

在`onCreateView（）`里是对视图的初始化：

```
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
```

再说`onCreateView（）`里面调用到的`initData()`

```java
if (NetworkUtil.isNetworkAvailableAndConnected(getContext())) {
           showSuccessView();
           getLoaderManager().initLoader(0, null, this);//?
       } else {
           showErrorView();
       }
```

除去initData（）里的简单UI代码，我们要好好了解`getLoaderManager().initLoader(0, null, this);`的使用

然后这里给出`initLoader`的声明源码（来自LoaderManager）：

```java
public abstract <D> Loader<D> initLoader(int id, Bundle args,
            LoaderManager.LoaderCallbacks<D> callback);
```


一旦开始了`initData（）`Loader就会被调用（调用的方式就是` getLoaderManager().initLoader(0, null, this);`），这里将被调用的是我们之后会提及的MainLoader.java


如果你需要知道详细的调用以及Loader的工作流程，你应该去查看LoaderManager的源码。

总之，我们需要知道的是，在我们当前讨论的MainFragment里，fragment会调用Loader来进行后台任务（任务在这里提前说一下：1.实例化`MovieLab`，2.解析从网络util包的一个工具类发来的`responseData`，从Json解析到moviewList实例里的每一个movie实例里）

一旦loader好了，就会调用`onLoadFinished（）`，这里面，我们用得到的MovieList数据，采用adapter来控制view的显示（by：` mMainAdapter.refreshMovieList(mMovieList);`）


好了，关于MainFragment我们就说这么多

## DetailPagerActivity

在MainFragment里点击一个gridView般的RecyclerView里的一个item，就会跳转到这里。所以现在来讲讲它。



### 从布局开始说起

我们需要的是`CollapsingToolbarLayout`，使用这个能达到像bilibili播放视频时的那种页面。

我们在看到实际的app后，就请把这个布局分成两部分：1：电影图片（对应`AppBarLayout`里的`CollapsingToolbarLayout`的`ImageView`），2：电影的细节的fragment（对应ViewPager）

具体关于`CollapsingToolbarLayout`解释你可以参考一下几篇博客：

http://blog.csdn.net/u010687392/article/details/46906657

http://www.jianshu.com/p/06c0ae8d9a96

http://blog.csdn.net/baidu_31093133/article/details/52807465

***

如果你自己复制原工程的代码，如果没有好好看看原来的`AndroidManifest.xml`，就会导致一些对`DetailPagerActivity`布局的设置冲突问题

（比如，关于toolbar和窗口本身提供ActionBar冲突问题，如下：

`This Activity already has an action bar supplied by the window decor. Do not request Window.FEATURE_SUPPORT_ACTION_BAR and set windowActionBar to false in your theme to use a Toolbar instead.`）

（如果你看不懂了，我来解释一下：原因是我没有为设置DetailPagerActivity设置合适的theme，合适的theme如下（标注  注意这个设置 的是我最开始没有设置的）

```
<style name="DetailTheme" parent="AppTheme">
        <item name="windowActionBar">false</item>注意这个设置
        <item name="windowNoTitle">true</item>注意这个设置
        <item name="android:windowIsTranslucent">true</item>
</style>
```


***

一旦你对布局熟悉一下了，那么就再从代码说起


### 从代码说起

首先说`Intent newIntent（Context context, Movie movie）`这个静态方法

这是一个很好的开启第二个Activity的习惯，你只要多用这种方式开启一个Activity就好，反正就是好。

然后就是初始化`onCreate（）`，这里面会调用`initViewPager()`和`initCollapsingToolBar() `

最后就是对菜单的一些动作，让菜单中包含一个分享按钮

- 简单说一下`initViewPager()`：

ViewPager这个控件会装载上一个`FragmentStatePagerAdapter(fragmentManager)`，简单地说，这个`FragmentStatePagerAdapter(fragmentManager)`能支持大量的fragment的替换动作，每次我们对ViewPager滑动，就会生成新的页面，总结就是：它只保留当前页面，当页面离开视线后，就会被消除，释放其资源；而在页面需要显示时，生成新的页面

在对ViewPager设置监听器`mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()`，并重写`onPageSelected()`方法，在`onPageSelected()`方法里，会使用`Picasso`来加载图片。

```
Picasso.with(DetailPagerActivity.this)
                        .load("http://image.tmdb.org/t/p/w780/" + mMovieList.get(position).getBackdrop_path())
                        .into(mBackdropImageView);
```

还有一点注意：ViewPager默认值显示PageAdapter中的第一个列表项，要显示列表项，可设置ViewPager当前要显示的列表项为movie数组中指定位置的列表项，这里用一个for循环即可：

```
for (int i = 0; i < mMovieList.size(); i++) {
            if (mMovieList.get(i).getId() == mMovie.getId()) {
                mViewPager.setCurrentItem(i);//i是一个指示器吗?恩，指示我们应该把当前item设置到哪个位置
                break;
            }
        }
```


- 简单说一下`initCollapsingToolBar() `

```
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
```

内容不多，就是初始化我们的toolBar，装载图片


# 三，从看不见的再深入剖析这个工程

## 由DetailPagerActivity带出的其他议题

至此我们把能看到的东西都走了一遍，在DetailFragment加载的地方，你大概会注意到一个LazyLoad的地方，这个就是懒加载了。关于懒加载，你可以参考这篇文章：

http://blog.csdn.net/maosidiaoxian/article/details/38300627

讲解了为何我们使用懒加载：一个Activity里面可能会以viewpager（或其他容器）与多个Fragment来组合使用，而如果每个fragment都需要去加载数据，或从本地加载，或从网络加载，那么在这个activity刚创建的时候就变成需要初始化大量资源。这样的结果，我们当然不会满意。那么，能不能做到当切换到这个fragment的时候，它才去初始化呢？答案就在Fragment里的setUserVisibleHint这个方法里。该方法用于告诉系统，这个Fragment的UI是否是可见的。所以我们只需要继承Fragment并重写该方法，即可实现在fragment可见时才进行数据加载操作，即Fragment的懒加载。 

另外值得注意的一个知识点就是parcelable了：

我们在db这个包里，Movie类就是实现parcelable接口的一个类，关于为何使用parcelable和使用它的效果，你可以参考这篇文章：

http://www.cnblogs.com/renqingping/archive/2012/10/25/Parcelable.html



# 结束语

这个精简的工程没有使用到数据库和retrofit。




