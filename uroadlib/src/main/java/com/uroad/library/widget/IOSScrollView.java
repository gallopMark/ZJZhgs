package com.uroad.library.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

/**
 * 仿IOS上拉下拉弹性效果ScrollView
 *
 * @author yangmbin
 * created at 2016/12/4 16:48
 */
public class IOSScrollView extends NestedScrollView {

    // ScrollView子布局
    private View contentView;

    // 手势按下Y坐标
    private float startY;

    // 手势按下时，是否可以下拉/上拉标志
    private boolean isCanPullDown, isCanPullUp;

    // 保存ScrollView子布局的初始位置信息
    private int leftPosition, topPosition, rightPosition, bottomPosition;

    public IOSScrollView(Context context) {
        super(context);
    }

    public IOSScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 获取ScrollView的子布局
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 0) {
            contentView = getChildAt(0);
        }
    }

    /**
     * 获取初始子布局的位置信息
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (contentView != null) {
            leftPosition = contentView.getLeft();
            topPosition = contentView.getTop();
            rightPosition = contentView.getRight();
            bottomPosition = contentView.getBottom();
        }
    }

    /**
     * 判断是否在ScrollView顶部，在顶部时可以下拉
     */
    private boolean isScrollViewTop() {
        return getScrollY() == 0;
    }

    /**
     * 判断是否在ScrollView底部，在顶部时可以上拉
     */
    private boolean isScrollViewBottom() {
        return contentView.getMeasuredHeight() <= getScrollY() + getHeight();
    }

    /**
     * 触摸事件处理
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (contentView == null)
            return super.dispatchTouchEvent(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = ev.getY();
                isCanPullDown = isScrollViewTop();
                isCanPullUp = isScrollViewBottom();
                break;

            case MotionEvent.ACTION_UP:
                float endY = ev.getY();
                // 手势放开时，采用动画形式返回原位置
                if (endY > startY && isCanPullDown || endY < startY && isCanPullUp) {
                    ObjectAnimator animator = ObjectAnimator.ofFloat(contentView, "translationY", contentView.getTop(), topPosition);
                    animator.setDuration(500);
                    animator.setInterpolator(new AccelerateInterpolator());
                    animator.start();
                    // 设置布局到正常位置
                    contentView.layout(leftPosition, topPosition, rightPosition, bottomPosition);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // 如果不在ScrollView的最顶部或最底部（startY需要是在最顶部或最底部时按下的坐标）
                if (!isCanPullUp && !isCanPullDown) {
                    startY = ev.getY();
                    isCanPullDown = isScrollViewTop();
                    isCanPullUp = isScrollViewBottom();
                    break;
                }
                // 在最上部或最底部时，拉动移动布局
                // 1、下拉 2、上拉 3、布局内容比ScrollView小，则既可以上拉，也可以下拉
                if (isCanPullDown && ev.getY() > startY || isCanPullUp && ev.getY() < startY || isCanPullDown && isCanPullUp) {
                    int deltaY = (int) (ev.getY() - startY);
                    contentView.layout(leftPosition, topPosition + deltaY, rightPosition, bottomPosition + deltaY);
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
}
