package com.xing.pullrefreshlistviewlib;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2017/6/17.
 */

public class PullRefreshListView extends ListView implements AbsListView.OnScrollListener {

    private final int STATE_PULL_REFRESH = 0;  // 下拉刷新状态

    private final int STATE_REFRESHING = 1;  // 正在刷新

    private final int STATE_RELEASE_REFRESH = 2;  // 松开刷新

    private int curState = 0;   // 当前刷新状态

    private Context mContext;

    private View headerView;

    private View footerView;

    private LayoutInflater inflater;

    private int headerHeight;   // headerView 高度

    private int footerHeight;   // footerView 高度

    private ProgressBar headerBar;  // headerView 刷新圆圈

    private ImageView headerArrow;  // headerView 下拉箭头

    private RotateAnimation upAnimation;

    private RotateAnimation downAnimation;

    private TextView headerStateView;

    private TextView headerTimeView;

    private boolean isLoadingMore = false;


    public PullRefreshListView(Context context) {
        this(context, null);
    }

    public PullRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        inflater = LayoutInflater.from(mContext);
        initHeaderView();
        initFooterView();
        initAnimation();  // 初始化动画


    }

    /**
     * 初始化下拉箭头动画
     */
    private void initAnimation() {
        //向上旋转动画
        upAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        upAnimation.setDuration(300);
        upAnimation.setFillAfter(true);
        //向下旋转动画
        downAnimation = new RotateAnimation(-180, -360, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        downAnimation.setDuration(300);
        downAnimation.setFillAfter(true);

    }

    private void initHeaderView() {
        setOnScrollListener(this);
        headerView = inflater.inflate(R.layout.layout_pull_header, null);
        headerArrow = (ImageView) headerView.findViewById(R.id.iv_header_arrow);
        headerBar = (ProgressBar) headerView.findViewById(R.id.iv_header_bar);
        headerStateView = (TextView) headerView.findViewById(R.id.tv_header_refresh_state);
        headerTimeView = (TextView) headerView.findViewById(R.id.tv_header_refresh_time);
        headerTimeView.setText("当前时间:" + formatCurTime());
        headerView.measure(0, 0);  // 调用该方法通知系统测量 headerview 宽高
        headerHeight = headerView.getMeasuredHeight();
        headerView.setPadding(0, -headerHeight, 0, 0);  // 通过 padding 的方式将 headerView 隐藏在顶部
        addHeaderView(headerView);  // 为 ListView 添加headerView


    }

    private void initFooterView() {
        footerView = inflater.inflate(R.layout.layout_pull_footer, null);
        footerView.measure(0, 0);
        footerHeight = footerView.getMeasuredHeight();
        footerView.setPadding(0, 0, 0, -footerHeight);
        addFooterView(footerView);
    }


    private int downY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("debug", "curState = " + curState);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:  // 手指移动的过程中，随时更新当前状态
                // 如果下拉到最大距离时，就不再响应 move 事件，否则 topPadding 将一直增大,不拦截 move 事件，
                // 将事件传递给 ListView 滚动处理
                if (curState == STATE_REFRESHING) {
                    break;
                }
                int deltaY = (int) (event.getY() - downY);
                int topPadding = -headerHeight + deltaY;
                Log.i("debug", "topPadding = " + topPadding);
                /**
                 * topPadding = 0 : 刚好完全显示出 headerView
                 * -headerHeight < topPadding < 0 : 未完全显示出 headerView, 下拉刷新
                 * topPadding > 0 : 显示高度超过 headerView 高度，松开刷新
                 *
                 */
                // topPadding > -headerHeight表示向下拉产生了位移
                if (topPadding > -headerHeight && getFirstVisiblePosition() == 0) {
                    if (topPadding > 0 && curState == STATE_PULL_REFRESH) {
                        curState = STATE_RELEASE_REFRESH;
                        refreshHeaderView();
                    } else if (topPadding < 0 && curState == STATE_RELEASE_REFRESH) {
                        curState = STATE_PULL_REFRESH;
                        refreshHeaderView();
                    }
                    if (topPadding > headerHeight * 0.5) {
                        return super.onTouchEvent(event);
                    }
                    headerView.setPadding(0, topPadding, 0, 0);
                    return true;
                }


                break;
            case MotionEvent.ACTION_UP:
                // 因为在 move 过程中已经随时更新了当前状态，所以在 up 过程中不用再判断 topPadding 值，只需判断当前状态即可
                if (curState == STATE_PULL_REFRESH) {  // 在下拉过程中松开手指，隐藏 headerView,不执行刷新操作
                    headerView.setPadding(0, -headerHeight, 0, 0);
                } else if (curState == STATE_RELEASE_REFRESH) {  // 在松开刷新过程松开手指，执行刷新操作。
                    headerView.setPadding(0, 0, 0, 0);
                    curState = STATE_REFRESHING;  // 状态更改为正在刷新
                    // 调用刷新接口
                    if (listener != null) {
                        listener.onPullRefresh();
                    }
                }
                refreshHeaderView();
                // 不会在刷新过程中抬起手指，因为在刷新过程中，down 事件已经返回false,不会执行up事件
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 根据当前状态 curState 刷新 headerView 显示 UI
     */
    private void refreshHeaderView() {
        switch (curState) {
            case STATE_PULL_REFRESH:
                headerStateView.setText("下拉刷新");
                headerTimeView.setText("当前时间:" + formatCurTime());
                headerArrow.startAnimation(downAnimation);

                break;
            case STATE_REFRESHING:
                headerStateView.setText("正在刷新");
                headerArrow.clearAnimation();  // 清除动画，才能设置 view.visibility 有效
                headerArrow.setVisibility(View.INVISIBLE);
                headerBar.setVisibility(View.VISIBLE);
                headerTimeView.setText("最近更新:" + formatCurTime());
                break;
            case STATE_RELEASE_REFRESH:
                headerStateView.setText("松开刷新");
                headerArrow.startAnimation(upAnimation);
                break;
        }
    }

    /**
     * 刷新完成，重置状态
     */
    public void onRefreshComplete() {
        //重置headerView
        headerView.setPadding(0, -headerHeight, 0, 0);
        curState = STATE_PULL_REFRESH;
        headerStateView.setText("下拉刷新");
        headerArrow.setVisibility(View.VISIBLE);
        headerBar.setVisibility(View.INVISIBLE);

        //重置footerView
        footerView.setPadding(0, 0, 0, -footerHeight);
        isLoadingMore = false;


    }


    private String formatCurTime() {
        String currentTime = "";
        currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        return currentTime;
    }

    OnRefreshListener listener;

    public interface OnRefreshListener {
        void onPullRefresh();  // 下拉刷新

        void onLoadMore();  // 加载更多
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        this.listener = listener;
    }


    // ListView 滑动监听事件
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE && getLastVisiblePosition() == (getCount() - 1) && !isLoadingMore) {
            isLoadingMore = true;
            //显示footerView
            footerView.setPadding(0, 0, 0, 0);
            setSelection(getCount());  //同时将最后设为选中，即显示在屏幕第一条，这样footerView才能显示出来。否则需要再上拉才显示
            if (listener != null) {
                listener.onLoadMore();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }
}
