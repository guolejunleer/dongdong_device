package com.dongdong.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.dongdong.base.BaseApplication;
import com.dongdong.utils.DDLog;
import com.dongdong.utils.DeviceInfoUtils;
import com.jr.door.R;

import java.util.ArrayList;
import java.util.List;

public class RippleView extends View {

    private Paint paint;
    private int maxWidth = 255;

    // 是否运行
    private boolean isStarting = false;
    private List<String> alphaList = new ArrayList<String>();
    private List<String> startWidthList = new ArrayList<String>();
    private float mScreenHight;
    private float mScreenWidth;
    private Bitmap mCallingState;
    private float mCallingLeft;
    private float mCallingTop;

    private int mViewWidth;
    private int mViewHight;

    public RippleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public RippleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RippleView(Context context) {
        super(context);
        init();
    }

    private void init() {
        paint = new Paint();
        // 设置博文的颜色
        paint.setColor(0xFFFFFF);
        alphaList.add("255");// 圆心的不透明度
        startWidthList.add("0");
        mScreenHight = DeviceInfoUtils.getScreenHeight();
        mScreenWidth = DeviceInfoUtils.getScreenWidth();
        int statusBar = DeviceInfoUtils.getStatusBarHeight(BaseApplication.context());
        DDLog.i("RippleView.clazz--->>>init........mScreenHight:"
                + mScreenHight + ",mScreenWidth:" + mScreenWidth + ",statusBar:" + statusBar);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mCallingState = BitmapFactory.decodeResource(getResources(), R.mipmap.calling_state);
        DDLog.i("RippleView.clazz--->>>onAttachedToWindow...........");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mCallingState.recycle();
        DDLog.i("RippleView.clazz--->>>onDetachedFromWindow...........");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        DDLog.i("RippleView.clazz--->>>onMeasure...........widthSize:"
                + widthSize + ",heightSize:" + heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int width = mViewWidth = right - left;
        int height = mViewHight = bottom - top;
        mCallingLeft = (width - mCallingState.getWidth()) / 2.0f;
        mCallingTop = (height - mCallingState.getHeight()) / 2.0f;
        DDLog.i("RippleView.clazz--->>>onLayout...........width:"
                + width + ",height:" + height);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isStarting) {
            //setBackgroundColor(Color.TRANSPARENT);// 颜色：完全透明
            // 依次绘制同心圆
            for (int i = 0; i < alphaList.size(); i++) {
                int alpha = Integer.parseInt(alphaList.get(i));
                // 圆半径
                int startWidth = Integer.parseInt(startWidthList.get(i));
                paint.setAlpha(alpha);

                // 这个半径决定你想要多大的扩散面积
                //canvas.drawCircle(mScreenWidth / 2, mScreenHight / 2, startWidth + 30, paint);
                //leer add for fix bug
                canvas.drawCircle(mViewWidth / 2, mViewHight / 2, startWidth + 30, paint);

                // 同心圆扩散
                if (isStarting && alpha > 0 && startWidth < maxWidth) {
                    alphaList.set(i, (alpha - 1) + "");
                    startWidthList.set(i, (startWidth + 1) + "");
                }
            }
            if (isStarting && Integer.parseInt(startWidthList.
                    get(startWidthList.size() - 1)) == maxWidth / 5) {
                alphaList.add("255");
                startWidthList.add("0");

            }
            // 同心圆数量达到10个，删除最外层圆
            if (isStarting && startWidthList.size() == 10) {
                startWidthList.remove(0);
                alphaList.remove(0);

            }
            //画中心标识
            canvas.drawBitmap(mCallingState, mCallingLeft, mCallingTop, null);
        }
        // 刷新界面
        invalidate();
    }

    // 执行动画
    public void start() {
        isStarting = true;
    }

    // 停止动画
    public void stop() {
        isStarting = false;
        alphaList.clear();
        startWidthList.clear();
        alphaList.add("255");// 圆心的不透明度
        startWidthList.add("0");
    }

    // 判断是否在执行
    public boolean isStarting() {
        return isStarting;
    }

}
