# PullRefreshListView

效果图：<br/>
<img src="https://github.com/xing16/PullRefreshListView/raw/master/screenshot/GIF.gif" width=350 height=600 alt="Sample App's Launch Screen">

##### 下拉刷新，上拉加载更多完成时，调用 onRefreshComplete（），隐藏 headerView,footerView

```
mListView.setOnRefreshListener(new PullRefreshListView.OnRefreshListener() {
            @Override
            public void onPullRefresh() {

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDataList.add(0, "refresh data ");
                        adapter.notifyDataSetChanged();
                        mListView.onRefreshComplete();
                    }
                }, 2000);
            }

            @Override
            public void onLoadMore() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDataList.add("load more data ");
                        adapter.notifyDataSetChanged();
                        mListView.onRefreshComplete();
                    }
                }, 2000);
            }
        });
```


