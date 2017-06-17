package com.xing.pullrefreshlistview;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;

import com.xing.pullrefreshlistviewlib.PullRefreshListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PullRefreshListView mListView;

    private List<String> mDataList;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        setListener();
    }

    private void setListener() {
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
    }

    private void initData() {
        mDataList = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            mDataList.add("测试数据 - " + i);
        }
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mDataList);
        mListView.setAdapter(adapter);
    }

    private void initView() {
        mListView = (PullRefreshListView) findViewById(R.id.list_view);
    }


}
