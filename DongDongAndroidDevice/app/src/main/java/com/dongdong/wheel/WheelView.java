/*
 *  Android Wheel Control.
 *  https://code.google.com/p/android-wheel/
 *  
 *  Copyright 2010 Yuri Kanivets
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.dongdong.wheel;

import java.util.LinkedList;
import java.util.List;

import com.jr.door.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.Scroller;


/**
 * Numeric wheel view.
 *
 * @author Yuri Kanivets
 */
public class WheelView extends View {
    /**
     * Scrolling duration
     */
    private static final int SCROLLING_DURATION = 400;

    /**
     * Minimum delta for scrolling
     */
    private static final int MIN_DELTA_FOR_SCROLLING = 1;

    /**
     * Current value & mLabel text color
     */
    private static final int VALUE_TEXT_COLOR = 0xF0000000;

    /**
     * Items text color
     */
    private static final int ITEMS_TEXT_COLOR = 0xFF000000;

    /**
     * Top and bottom shadows colors
     */
    private static final int[] SHADOWS_COLORS = new int[]{0xFF111111,
            0x00AAAAAA, 0x00AAAAAA};

    /**
     * Additional items height (is added to standard text item height)
     */
    private static final int ADDITIONAL_ITEM_HEIGHT = 15;

    /**
     * Text size
     */
    private static final int TEXT_SIZE = 20;

    /**
     * Top and bottom items offset (to hide that)
     */
    private static final int ITEM_OFFSET = TEXT_SIZE / 5;

    /**
     * Additional width for items layout
     */
    private static final int ADDITIONAL_ITEMS_SPACE = 10;

    /**
     * Label offset
     */
    private static final int LABEL_OFFSET = 8;

    /**
     * Left and right padding value
     */
    private static final int PADDING = 10;

    /**
     * Default count of visible items
     */
    private static final int DEF_VISIBLE_ITEMS = 5;

    // Wheel Values
    private WheelAdapter mAdapter = null;
    private int mCurrentItem = 0;

    // Widths
    private int mItemsWidth = 0;
    private int mLabelWidth = 0;

    // Count of visible items
    private int mVisibleItems = DEF_VISIBLE_ITEMS;

    // Item height
    private int mItemHeight = 0;

    // Text paints
    private TextPaint mItemsPaint;
    private TextPaint mValuePaint;

    // Layouts
    private StaticLayout mItemsLayout;
    private StaticLayout mLabelLayout;
    private StaticLayout mValueLayout;

    // Label & background
    private String mLabel;
    private Drawable mCenterDrawable;

    // Shadows drawables
    private GradientDrawable mTopShadow;
    private GradientDrawable mBottomShadow;

    // Scrolling
    private boolean isScrollingPerformed;
    private int mScrollingOffset;

    // Scrolling animation
    private GestureDetector mGestureDetector;
    private Scroller mScroller;
    private int mLastScrollY;

    // Cyclic
    boolean isCyclic = false;

    // Listeners
    private List<OnWheelChangedListener> mChangingListeners = new LinkedList<>();
    private List<OnWheelScrollListener> mScrollingListeners = new LinkedList<>();

    /**
     * Constructor
     */
    public WheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initData(context);
    }

    /**
     * Constructor
     */
    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData(context);
    }

    /**
     * Constructor
     */
    public WheelView(Context context) {
        super(context);
        initData(context);
    }

    /**
     * Initializes class data
     *
     * @param context the context
     */
    private void initData(Context context) {
        mGestureDetector = new GestureDetector(context, gestureListener);
        mGestureDetector.setIsLongpressEnabled(false);

        mScroller = new Scroller(context);
    }

    /**
     * Gets wheel mAdapter
     *
     * @return the mAdapter
     */
    public WheelAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * Sets wheel mAdapter
     *
     * @param mAdapter the new wheel mAdapter
     */
    public void setAdapter(WheelAdapter mAdapter) {
        this.mAdapter = mAdapter;
        invalidateLayouts();
        invalidate();
    }

    /**
     * Set the the specified scrolling interpolator
     *
     * @param interpolator the interpolator
     */
    public void setInterpolator(Interpolator interpolator) {
        mScroller.forceFinished(true);
        mScroller = new Scroller(getContext(), interpolator);
    }

    /**
     * Gets count of visible items
     *
     * @return the count of visible items
     */
    public int getmVisibleItems() {
        return mVisibleItems;
    }

    /**
     * Sets count of visible items
     *
     * @param count the new count
     */
    public void setmVisibleItems(int count) {
        mVisibleItems = count;
        invalidate();
    }

    /**
     * Gets mLabel
     *
     * @return the mLabel
     */
    public String getmLabel() {
        return mLabel;
    }

    /**
     * Sets mLabel
     *
     * @param newLabel the mLabel to set
     */
    public void setmLabel(String newLabel) {
        if (mLabel == null || !mLabel.equals(newLabel)) {
            mLabel = newLabel;
            mLabelLayout = null;
            invalidate();
        }
    }

    /**
     * Adds wheel changing listener
     *
     * @param listener the listener
     */
    public void addChangingListener(OnWheelChangedListener listener) {
        mChangingListeners.add(listener);
    }

    /**
     * Removes wheel changing listener
     *
     * @param listener the listener
     */
    public void removeChangingListener(OnWheelChangedListener listener) {
        mChangingListeners.remove(listener);
    }

    /**
     * Notifies changing listeners
     *
     * @param oldValue the old wheel value
     * @param newValue the new wheel value
     */
    protected void notifyChangingListeners(int oldValue, int newValue) {
        for (OnWheelChangedListener listener : mChangingListeners) {
            listener.onChanged(this, oldValue, newValue);
        }
    }

    /**
     * Adds wheel scrolling listener
     *
     * @param listener the listener
     */
    public void addScrollingListener(OnWheelScrollListener listener) {
        mScrollingListeners.add(listener);
    }

    /**
     * Removes wheel scrolling listener
     *
     * @param listener the listener
     */
    public void removeScrollingListener(OnWheelScrollListener listener) {
        mScrollingListeners.remove(listener);
    }

    /**
     * Notifies listeners about starting scrolling
     */
    protected void notifyScrollingListenersAboutStart() {
        for (OnWheelScrollListener listener : mScrollingListeners) {
            listener.onScrollingStarted(this);
        }
    }

    /**
     * Notifies listeners about ending scrolling
     */
    protected void notifyScrollingListenersAboutEnd() {
        for (OnWheelScrollListener listener : mScrollingListeners) {
            listener.onScrollingFinished(this);
        }
    }

    /**
     * Gets current value
     *
     * @return the current value
     */
    public int getCurrentItem() {
        return mCurrentItem;
    }

    /**
     * Sets the current item. Does nothing when index is wrong.
     *
     * @param index    the item index
     * @param animated the animation flag
     */
    public void setCurrentItem(int index, boolean animated) {
        if (mAdapter == null || mAdapter.getItemsCount() == 0) {
            return; // throw?
        }
        if (index < 0 || index >= mAdapter.getItemsCount()) {
            if (isCyclic) {
                while (index < 0) {
                    index += mAdapter.getItemsCount();
                }
                index %= mAdapter.getItemsCount();
            } else {
                return; // throw?
            }
        }
        if (index != mCurrentItem) {
            if (animated) {
                scroll(index - mCurrentItem, SCROLLING_DURATION);
            } else {
                invalidateLayouts();

                int old = mCurrentItem;
                mCurrentItem = index;

                notifyChangingListeners(old, mCurrentItem);

                invalidate();
            }
        }
    }

    /**
     * Sets the current item w/o animation. Does nothing when index is wrong.
     *
     * @param index the item index
     */
    public void setCurrentItem(int index) {
        setCurrentItem(index, false);
    }

    /**
     * Tests if wheel is cyclic. That means before the 1st item there is shown the last one
     *
     * @return true if wheel is cyclic
     */
    public boolean isCyclic() {
        return isCyclic;
    }

    /**
     * Set wheel cyclic flag
     *
     * @param isCyclic the flag to set
     */
    public void setCyclic(boolean isCyclic) {
        this.isCyclic = isCyclic;

        invalidate();
        invalidateLayouts();
    }

    /**
     * Invalidates layouts
     */
    private void invalidateLayouts() {
        mItemsLayout = null;
        mValueLayout = null;
        mScrollingOffset = 0;
    }

    /**
     * Initializes resources
     */
    private void initResourcesIfNecessary() {
        if (mItemsPaint == null) {
            mItemsPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG
                    | Paint.FAKE_BOLD_TEXT_FLAG);
            //mItemsPaint.density = getResources().getDisplayMetrics().density;
            mItemsPaint.setTextSize(TEXT_SIZE);
        }

        if (mValuePaint == null) {
            mValuePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG
                    | Paint.FAKE_BOLD_TEXT_FLAG | Paint.DITHER_FLAG);
            //mValuePaint.density = getResources().getDisplayMetrics().density;
            mValuePaint.setTextSize(TEXT_SIZE);
            mValuePaint.setShadowLayer(0.1f, 0, 0.1f, 0xFFC0C0C0);
        }

        if (mCenterDrawable == null) {
            mCenterDrawable = ContextCompat.getDrawable(getContext(), R.drawable.wheel_val);
        }

        if (mTopShadow == null) {
            mTopShadow = new GradientDrawable(Orientation.TOP_BOTTOM, SHADOWS_COLORS);
        }

        if (mBottomShadow == null) {
            mBottomShadow = new GradientDrawable(Orientation.BOTTOM_TOP, SHADOWS_COLORS);
        }

        setBackgroundResource(R.drawable.wheel_bg);
    }

    /**
     * Calculates desired height for layout
     *
     * @param layout the source layout
     * @return the desired layout height
     */
    private int getDesiredHeight(Layout layout) {
        if (layout == null) {
            return 0;
        }

        int desired = getItemHeight() * mVisibleItems - ITEM_OFFSET * 2
                - ADDITIONAL_ITEM_HEIGHT;

        // Check against our minimum height
        desired = Math.max(desired, getSuggestedMinimumHeight());

        return desired;
    }

    /**
     * Returns text item by index
     *
     * @param index the item index
     * @return the item or null
     */
    private String getTextItem(int index) {
        if (mAdapter == null || mAdapter.getItemsCount() == 0) {
            return null;
        }
        int count = mAdapter.getItemsCount();
        if ((index < 0 || index >= count) && !isCyclic) {
            return null;
        } else {
            while (index < 0) {
                index = count + index;
            }
        }

        index %= count;
        return mAdapter.getItem(index);
    }

    /**
     * Builds text depending on current value
     *
     * @param useCurrentValue
     * @return the text
     */
    private String buildText(boolean useCurrentValue) {
        StringBuilder itemsText = new StringBuilder();
        int addItems = mVisibleItems / 2 + 1;

        for (int i = mCurrentItem - addItems; i <= mCurrentItem + addItems; i++) {
            if (useCurrentValue || i != mCurrentItem) {
                String text = getTextItem(i);
                if (text != null) {
                    itemsText.append(text);
                }
            }
            if (i < mCurrentItem + addItems) {
                itemsText.append("\n");
            }
        }

        return itemsText.toString();
    }

    /**
     * Returns the max item length that can be present
     *
     * @return the max length
     */
    private int getMaxTextLength() {
        WheelAdapter adapter = getAdapter();
        if (adapter == null) {
            return 0;
        }

        int adapterLength = adapter.getMaximumLength();
        if (adapterLength > 0) {
            return adapterLength;
        }

        String maxText = null;
        int addItems = mVisibleItems / 2;
        for (int i = Math.max(mCurrentItem - addItems, 0);
             i < Math.min(mCurrentItem + mVisibleItems, adapter.getItemsCount()); i++) {
            String text = adapter.getItem(i);
            if (text != null && (maxText == null || maxText.length() < text.length())) {
                maxText = text;
            }
        }

        return maxText != null ? maxText.length() : 0;
    }

    /**
     * Returns height of wheel item
     *
     * @return the item height
     */
    private int getItemHeight() {
        if (mItemHeight != 0) {
            return mItemHeight;
        } else if (mItemsLayout != null && mItemsLayout.getLineCount() > 2) {
            mItemHeight = mItemsLayout.getLineTop(2) - mItemsLayout.getLineTop(1);
            return mItemHeight;
        }

        return getHeight() / mVisibleItems;
    }

    /**
     * Calculates control width and creates text layouts
     *
     * @param widthSize the input layout width
     * @param mode      the layout mode
     * @return the calculated control width
     */
    private int calculateLayoutWidth(int widthSize, int mode) {
        initResourcesIfNecessary();

        int width = widthSize;

        int maxLength = getMaxTextLength();
        if (maxLength > 0) {
            float textWidth = (float) Math.ceil(Layout.getDesiredWidth("0", mItemsPaint));
            mItemsWidth = (int) (maxLength * textWidth);
        } else {
            mItemsWidth = 0;
        }
        mItemsWidth += ADDITIONAL_ITEMS_SPACE; // make it some more

        mLabelWidth = 0;
        if (mLabel != null && mLabel.length() > 0) {
            mLabelWidth = (int) (float) Math.ceil(Layout.getDesiredWidth(mLabel, mValuePaint));
        }

        boolean recalculate = false;
        if (mode == MeasureSpec.EXACTLY) {
            width = widthSize;
            recalculate = true;
        } else {
            width = mItemsWidth + mLabelWidth + 2 * PADDING;
            if (mLabelWidth > 0) {
                width += LABEL_OFFSET;
            }

            // Check against our minimum width
            width = Math.max(width, getSuggestedMinimumWidth());

            if (mode == MeasureSpec.AT_MOST && widthSize < width) {
                width = widthSize;
                recalculate = true;
            }
        }

        if (recalculate) {
            // recalculate width
            int pureWidth = width - LABEL_OFFSET - 2 * PADDING;
            if (pureWidth <= 0) {
                mItemsWidth = mLabelWidth = 0;
            }
            if (mLabelWidth > 0) {
                double newWidthItems = (double) mItemsWidth * pureWidth
                        / (mItemsWidth + mLabelWidth);
                mItemsWidth = (int) newWidthItems;
                mLabelWidth = pureWidth - mItemsWidth;
            } else {
                mItemsWidth = pureWidth + LABEL_OFFSET; // no mLabel
            }
        }

        if (mItemsWidth > 0) {
            createLayouts(mItemsWidth, mLabelWidth);
        }

        return width;
    }

    /**
     * Creates layouts
     *
     * @param widthItems width of items layout
     * @param widthLabel width of mLabel layout
     */
    private void createLayouts(int widthItems, int widthLabel) {
        if (mItemsLayout == null || mItemsLayout.getWidth() > widthItems) {
            mItemsLayout = new StaticLayout(buildText(isScrollingPerformed), mItemsPaint, widthItems,
                    widthLabel > 0 ? Layout.Alignment.ALIGN_OPPOSITE : Layout.Alignment.ALIGN_CENTER,
                    1, ADDITIONAL_ITEM_HEIGHT, false);
        } else {
            mItemsLayout.increaseWidthTo(widthItems);
        }

        if (!isScrollingPerformed && (mValueLayout == null || mValueLayout.getWidth() > widthItems)) {
            String text = getAdapter() != null ? getAdapter().getItem(mCurrentItem) : null;
            mValueLayout = new StaticLayout(text != null ? text : "",
                    mValuePaint, widthItems, widthLabel > 0 ?
                    Layout.Alignment.ALIGN_OPPOSITE : Layout.Alignment.ALIGN_CENTER,
                    1, ADDITIONAL_ITEM_HEIGHT, false);
        } else if (isScrollingPerformed) {
            mValueLayout = null;
        } else {
            mValueLayout.increaseWidthTo(widthItems);
        }

        if (widthLabel > 0) {
            if (mLabelLayout == null || mLabelLayout.getWidth() > widthLabel) {
                mLabelLayout = new StaticLayout(mLabel, mValuePaint,
                        widthLabel, Layout.Alignment.ALIGN_NORMAL, 1,
                        ADDITIONAL_ITEM_HEIGHT, false);
            } else {
                mLabelLayout.increaseWidthTo(widthLabel);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = calculateLayoutWidth(widthSize, widthMode);

        int height;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = getDesiredHeight(mItemsLayout);

            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize);
            }
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mItemsLayout == null) {
            if (mItemsWidth == 0) {
                calculateLayoutWidth(getWidth(), MeasureSpec.EXACTLY);
            } else {
                createLayouts(mItemsWidth, mLabelWidth);
            }
        }

        if (mItemsWidth > 0) {
            canvas.save();
            // Skip padding space and hide a part of top and bottom items
            canvas.translate(PADDING, -ITEM_OFFSET);
            drawItems(canvas);
            drawValue(canvas);
            canvas.restore();
        }

        drawCenterRect(canvas);
        drawShadows(canvas);
    }

    /**
     * Draws shadows on top and bottom of control
     *
     * @param canvas the canvas for drawing
     */
    private void drawShadows(Canvas canvas) {
        mTopShadow.setBounds(0, 0, getWidth(), getHeight() / mVisibleItems);
        mTopShadow.draw(canvas);

        mBottomShadow.setBounds(0, getHeight() - getHeight() / mVisibleItems,
                getWidth(), getHeight());
        mBottomShadow.draw(canvas);
    }

    /**
     * Draws value and mLabel layout
     *
     * @param canvas the canvas for drawing
     */
    private void drawValue(Canvas canvas) {
        mValuePaint.setColor(VALUE_TEXT_COLOR);
        mValuePaint.drawableState = getDrawableState();

        Rect bounds = new Rect();
        mItemsLayout.getLineBounds(mVisibleItems / 2, bounds);

        // draw mLabel
        if (mLabelLayout != null) {
            canvas.save();
            canvas.translate(mItemsLayout.getWidth() + LABEL_OFFSET, bounds.top);
            mLabelLayout.draw(canvas);
            canvas.restore();
        }

        // draw current value
        if (mValueLayout != null) {
            canvas.save();
            canvas.translate(0, bounds.top + mScrollingOffset);
            mValueLayout.draw(canvas);
            canvas.restore();
        }
    }

    /**
     * Draws items
     *
     * @param canvas the canvas for drawing
     */
    private void drawItems(Canvas canvas) {
        canvas.save();

        int top = mItemsLayout.getLineTop(1);
        canvas.translate(0, -top + mScrollingOffset);

        mItemsPaint.setColor(ITEMS_TEXT_COLOR);
        mItemsPaint.drawableState = getDrawableState();
        mItemsLayout.draw(canvas);

        canvas.restore();
    }

    public String getCurrentItemValue() {
        return ((NumericWheelAdapter) getAdapter()).getStrContents()[getCurrentItem()];
    }

    /**
     * Draws rect for current value
     *
     * @param canvas the canvas for drawing
     */
    private void drawCenterRect(Canvas canvas) {
        int center = getHeight() / 2;
        int offset = getItemHeight() / 2;
        mCenterDrawable.setBounds(0, center - offset, getWidth(), center + offset);
        mCenterDrawable.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        WheelAdapter adapter = getAdapter();
        if (adapter == null) {
            return true;
        }

        if (!mGestureDetector.onTouchEvent(event) && event.getAction() == MotionEvent.ACTION_UP) {
            justify();
        }
        return true;
    }

    /**
     * Scrolls the wheel
     *
     * @param delta the scrolling value
     */
    private void doScroll(int delta) {
        mScrollingOffset += delta;

        int count = mScrollingOffset / getItemHeight();
        int pos = mCurrentItem - count;
        if (isCyclic && mAdapter.getItemsCount() > 0) {
            // fix position by rotating
            while (pos < 0) {
                pos += mAdapter.getItemsCount();
            }
            pos %= mAdapter.getItemsCount();
        } else if (isScrollingPerformed) {
            //
            if (pos < 0) {
                count = mCurrentItem;
                pos = 0;
            } else if (pos >= mAdapter.getItemsCount()) {
                count = mCurrentItem - mAdapter.getItemsCount() + 1;
                pos = mAdapter.getItemsCount() - 1;
            }
        } else {
            // fix position
            pos = Math.max(pos, 0);
            pos = Math.min(pos, mAdapter.getItemsCount() - 1);
        }

        int offset = mScrollingOffset;
        if (pos != mCurrentItem) {
            setCurrentItem(pos, false);
        } else {
            invalidate();
        }

        // update offset
        mScrollingOffset = offset - count * getItemHeight();
        if (mScrollingOffset > getHeight()) {
            mScrollingOffset = mScrollingOffset % getHeight() + getHeight();
        }
    }

    // gesture listener
    private SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {
        public boolean onDown(MotionEvent e) {
            if (isScrollingPerformed) {
                mScroller.forceFinished(true);
                clearMessages();
                return true;
            }
            return false;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            startScrolling();
            doScroll((int) -distanceY);
            return true;
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            mLastScrollY = mCurrentItem * getItemHeight() + mScrollingOffset;
            int maxY = isCyclic ? 0x7FFFFFFF : mAdapter.getItemsCount() * getItemHeight();
            int minY = isCyclic ? -maxY : 0;
            mScroller.fling(0, mLastScrollY, 0, (int) -velocityY / 2, 0, 0, minY, maxY);
            setNextMessage(MESSAGE_SCROLL);
            return true;
        }
    };

    // Messages
    private final int MESSAGE_SCROLL = 0;
    private final int MESSAGE_JUSTIFY = 1;

    /**
     * Set next message to queue. Clears queue before.
     *
     * @param message the message to set
     */
    private void setNextMessage(int message) {
        clearMessages();
        animationHandler.sendEmptyMessage(message);
    }

    /**
     * Clears messages from queue
     */
    private void clearMessages() {
        animationHandler.removeMessages(MESSAGE_SCROLL);
        animationHandler.removeMessages(MESSAGE_JUSTIFY);
    }

    // animation handler
    private Handler animationHandler = new Handler() {
        public void handleMessage(Message msg) {
            mScroller.computeScrollOffset();
            int currY = mScroller.getCurrY();
            int delta = mLastScrollY - currY;
            mLastScrollY = currY;
            if (delta != 0) {
                doScroll(delta);
            }

            // scrolling is not finished when it comes to final Y
            // so, finish it manually
            if (Math.abs(currY - mScroller.getFinalY()) < MIN_DELTA_FOR_SCROLLING) {
                currY = mScroller.getFinalY();
                mScroller.forceFinished(true);
            }
            if (!mScroller.isFinished()) {
                animationHandler.sendEmptyMessage(msg.what);
            } else if (msg.what == MESSAGE_SCROLL) {
                justify();
            } else {
                finishScrolling();
            }
        }
    };

    /**
     * Justifies wheel
     */
    private void justify() {
        if (mAdapter == null) {
            return;
        }

        mLastScrollY = 0;
        int offset = mScrollingOffset;
        int itemHeight = getItemHeight();
        boolean needToIncrease = offset > 0 ? mCurrentItem < mAdapter.getItemsCount() : mCurrentItem > 0;
        if ((isCyclic || needToIncrease) && Math.abs((float) offset) > (float) itemHeight / 2) {
            if (offset < 0)
                offset += itemHeight + MIN_DELTA_FOR_SCROLLING;
            else
                offset -= itemHeight + MIN_DELTA_FOR_SCROLLING;
        }
        if (Math.abs(offset) > MIN_DELTA_FOR_SCROLLING) {
            mScroller.startScroll(0, 0, 0, offset, SCROLLING_DURATION);
            setNextMessage(MESSAGE_JUSTIFY);
        } else {
            finishScrolling();
        }
    }

    /**
     * Starts scrolling
     */
    private void startScrolling() {
        if (!isScrollingPerformed) {
            isScrollingPerformed = true;
            notifyScrollingListenersAboutStart();
        }
    }

    /**
     * Finishes scrolling
     */
    void finishScrolling() {
        if (isScrollingPerformed) {
            notifyScrollingListenersAboutEnd();
            isScrollingPerformed = false;
        }
        invalidateLayouts();
        invalidate();
    }


    /**
     * Scroll the wheel
     *
     * @param itemsToScroll items to scroll
     * @param time          scrolling duration
     */
    public void scroll(int itemsToScroll, int time) {
        mScroller.forceFinished(true);

        mLastScrollY = mScrollingOffset;
        int offset = itemsToScroll * getItemHeight();

        mScroller.startScroll(0, mLastScrollY, 0, offset - mLastScrollY, time);
        setNextMessage(MESSAGE_SCROLL);

        startScrolling();
    }

}
