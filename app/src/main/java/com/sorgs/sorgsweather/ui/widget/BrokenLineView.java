package com.sorgs.sorgsweather.ui.widget;

import com.sorgs.sorgsweather.R;
import com.sorgs.sorgsweather.domian.BrokenLineBean;
import com.sorgs.sorgsweather.utils.WeatherIconsUtil;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.v4.util.Pair;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

import static com.sorgs.sorgsweather.utils.ScreenUtils.dp2px;
import static com.sorgs.sorgsweather.utils.ScreenUtils.dp2pxF;
import static com.sorgs.sorgsweather.utils.ScreenUtils.sp2pxF;

/**
 * description: 折线图.
 *
 * @author Sorgs.
 * @date 2018/2/28.
 */

public class BrokenLineView extends View {


    private static int DEFAULT_BULE = 0XFF00BFFF;
    private static int DEFAULT_GRAY = Color.GRAY;

    private Scroller mScroller;
    private ViewConfiguration mViewConfiguration;

    private int mBackgroundColor;
    /**
     * 控件最小高度
     */
    private int mMinViewHeight;
    /**
     * 折线最低点的高度
     */
    private int mMinPointHeight;
    /**
     * 折线线段长度
     */
    private int mLineInterval;
    /**
     * 折线点的半径
     */
    private float mPointRadius;
    /**
     * 字体大小
     */
    private float mTextSize;
    /**
     * 折线单位高度差
     */
    private float mPointGap;
    /**
     * 折线坐标图四周留出来的偏移量
     */
    private int mDefaultPadding;
    /**
     * 天气图标的边长
     */
    private float mIconWidth;

    private int mViewHeight;
    private int mViewWidth;
    private int mScreenWidth;
    private int mScreenHeight;

    /**
     * 元数据
     */
    private List<BrokenLineBean> mData = new ArrayList<>();
    /**
     * 对元数据中天气分组后的集合
     */
    private List<Pair<Integer, String>> mWeatherDates = new ArrayList<>();
    /**
     * 不同天气之间虚线的x坐标集合
     */
    private List<Float> mDashDates = new ArrayList<>();
    /**
     * 折线拐点坐标集合
     */
    private List<PointF> mPoints = new ArrayList<>();
    /**
     * 元数据中的最高温度
     */
    private int mMaxTemperature;
    /**
     * 元数据中的最最低温度
     */
    private int mMinTemperature;
    private Paint mLinePaint;
    private Paint mTextPaint;
    private Paint mCirclePaint;

    private float mLastX = 0;
    private VelocityTracker mVelocityTracker;


    public BrokenLineView(Context context) {
        super(context, null);
    }

    public BrokenLineView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init(context, attrs);
    }


    public BrokenLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mScroller = new Scroller(context);
        mViewConfiguration = ViewConfiguration.get(context);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BrokenLineView);
        mMinPointHeight = (int) typedArray.getDimension(R.styleable.BrokenLineView_min_point_height, dp2pxF(context, 60));
        mLineInterval = (int) typedArray.getDimension(R.styleable.BrokenLineView_line_interval, dp2pxF(context, 60));
        mBackgroundColor = typedArray.getColor(R.styleable.BrokenLineView_background_color, Color.WHITE);

        typedArray.recycle();

        setBackgroundColor(mBackgroundColor);

        initSize(context);

        initPaint(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (heightMode == MeasureSpec.EXACTLY) {
            mViewHeight = Math.max(heightSize, mMinViewHeight);
        } else {
            mViewHeight = mMinViewHeight;
        }

        int totalWidth = 0;
        if (mData.size() > 1) {
            totalWidth = 2 * mDefaultPadding + mLineInterval * (mData.size() - 1);
        }
        //默认控件最小为屏幕宽度
        mViewWidth = Math.max(mViewWidth, totalWidth);

        setMeasuredDimension(mViewWidth, mViewHeight);
        calculatePointGap();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initSize(getContext());
        calculatePointGap();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mData.isEmpty()) {
            return;
        }

        drawAxis(canvas);

        drawLinesAndPoints(canvas);

        drawTemperature(canvas);

        drawWeatherDash(canvas);

        drawWeatherIcon(canvas);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        float x;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    //fling还没结束
                    mScroller.abortAnimation();
                }

                mLastX = event.getX();
                return true;
            case MotionEvent.ACTION_MOVE:
                x = event.getX();
                int deltaX = (int) (mLastX - x);
                if (getScrollX() + deltaX < 0) {
                    //越界回复
                    scrollTo(0, 0);
                    return true;
                } else if (getScrollX() + deltaX > mViewWidth - mScreenWidth) {
                    scrollTo(mViewWidth - mScreenWidth, 0);
                    return true;
                }
                scrollBy(deltaX, 0);
                mLastX = x;
                break;
            case MotionEvent.ACTION_UP:
                x = event.getX();
                //计算1秒内滑过多少像素
                mVelocityTracker.computeCurrentVelocity(1000);
                int xVelocity = (int) mVelocityTracker.getXVelocity();
                if (Math.abs(xVelocity) > mViewConfiguration.getScaledMinimumFlingVelocity()) {
                    //滑动速度可被判定为抛动
                    mScroller.fling(getScrollX(), 0, -xVelocity, 0, 0, mViewWidth - mScreenWidth, 0, 0);
                    invalidate();
                }
                break;
            default:
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

    /**
     * 画天气图标和它下方文字
     * 若相邻虚线都在屏幕内，图标的X位置即在两虚线的中间
     * 若有一条虚线在屏幕外，图标的X位置即在屏幕边沿到另一条虚线的中间
     * 若两条都在屏幕外，图标X的位置紧贴某一条虚线或屏幕中间
     */
    private void drawWeatherIcon(Canvas canvas) {
        canvas.save();
        //字体缩小0.9倍
        mTextPaint.setTextSize(0.9f * mTextSize);

        boolean leftUsedScreenLeft = false;
        boolean rightUsedScreenRight = false;

        //范围控制在0~mViewWidth-mScreenWidth
        int scrollX = getScrollX();
        float left, right;
        float iconX, iconY;
        //文字的X坐标跟图标是一样的，无需额外声明
        float textY;
        iconY = mViewHeight - (mDefaultPadding + mMinPointHeight / 2.0f);
        textY = iconY + mIconWidth / 2.0f + dp2pxF(getContext(), 10);
        Paint.FontMetrics metrics = mTextPaint.getFontMetrics();
        for (int i = 0; i < mDashDates.size() - 1; i++) {
            left = mDashDates.get(i);
            right = mDashDates.get(i + 1);

            //一下校正的情况为：两条虚线都在屏幕内或只有一条在屏幕内

            //仅左虚线在屏幕外
            if (left < scrollX && right < scrollX + mScreenWidth) {
                left = scrollX;
                leftUsedScreenLeft = true;
            }

            //仅右虚线在屏幕外
            if (right > scrollX + mScreenWidth && left > scrollX) {
                right = scrollX + mScreenWidth;
                rightUsedScreenRight = true;
            }

            if (right - left > mIconWidth) {
                //经过上诉校正之后左右距离还大于图标宽度
                iconX = left + (right - left) / 2.0f;
            } else {
                //经过上诉校正之后左右距离小于图标宽度，则贴着在屏幕内的虚线
                if (leftUsedScreenLeft) {
                    iconX = right - mIconWidth / 2.0f;
                } else {
                    iconX = left + mIconWidth / 2.0f;
                }
            }
            //以下校正的情况为：两条虚线都在屏幕之外
            if (right < scrollX) {
                //两条都在屏幕左侧，图标紧贴右虚线
                iconX = right - mIconWidth / 2.0f;
            } else if (left > scrollX + mScreenWidth) {
                //两条都在屏幕右侧，图标紧贴左虚线
                iconX = left + mIconWidth / 2.0f;
            } else if (left < scrollX && right > scrollX + mScreenWidth) {
                //一条在屏幕左，一条在屏幕右，图标居中
                iconX = scrollX + (mScreenWidth / 2.0f);
            }
            //获取绘制图标
            Bitmap icon = WeatherIconsUtil.getWeatherIcon(getContext(), mWeatherDates.get(i).second, mIconWidth, mIconWidth);
            //经过上诉校正之后可以得到图标和文字的绘制区域
            RectF iconRect = new RectF(iconX - mIconWidth / 2.0f,
                    iconY - mIconWidth / 2.0f,
                    iconX + mIconWidth / 2.0f,
                    iconY + mIconWidth / 2.0f);
            //绘制图标
            canvas.drawBitmap(icon, null, iconRect, null);
            //绘制图标下方文字
            canvas.drawText(mWeatherDates.get(i).second,
                    iconX,
                    textY - (metrics.ascent + metrics.descent) / 2,
                    mTextPaint);
            //重置标志位
            leftUsedScreenLeft = rightUsedScreenRight = false;
        }
        mTextPaint.setTextSize(mTextSize);
        canvas.restore();
    }

    /**
     * 画不同天气之间的虚线
     */
    private void drawWeatherDash(Canvas canvas) {
        canvas.save();
        mLinePaint.setColor(DEFAULT_GRAY);
        mLinePaint.setStrokeWidth(dp2pxF(getContext(), 0.5f));
        mLinePaint.setAlpha(0xcc);

        //设置画笔画出虚线
        //两个值分别为虚线的实线长度和空白长度
        float[] floats = {dp2pxF(getContext(), 5), dp2pxF(getContext(), 1)};

        DashPathEffect pathEffect = new DashPathEffect(floats, 0);
        mLinePaint.setPathEffect(pathEffect);

        mDashDates.clear();
        int interval = 0;
        float startX, startY, endX, endY;
        endY = mViewHeight - mDefaultPadding;

        //0坐标点的虚线手动画上
        canvas.drawLine(mDefaultPadding, mPoints.get(0).y + mPointRadius + dp2pxF(getContext(), 2),
                mDefaultPadding,
                endY,
                mLinePaint);
        mDashDates.add((float) mDefaultPadding);

        for (int i = 0; i < mWeatherDates.size(); i++) {
            interval += mWeatherDates.get(i).first;
            if (interval > mPoints.size() - 1) {
                interval = mPoints.size() - 1;
            }
            startX = endX = mDefaultPadding + interval * mLineInterval;
            startY = mPoints.get(interval).y + mPointRadius + dp2pxF(getContext(), 2);
            mDashDates.add(startX);
            canvas.drawLine(startX, startY, endX, endY, mLinePaint);
        }

        //这里注意下，当最后一组的连续天气数为1时，是不需要计入虚线集合的，否则会多画一个天气图标
        if (mWeatherDates.get(mWeatherDates.size() - 1).first == 1 && mDashDates.size() > 1) {
            mDashDates.remove(mDashDates.get(mDashDates.size() - 1));
        }

        mLinePaint.setPathEffect(null);
        mLinePaint.setAlpha(0xff);
        canvas.restore();
    }

    /**
     * 画温度描述值
     */
    private void drawTemperature(Canvas canvas) {
        canvas.save();

        //字体放大1.2倍
        mTextPaint.setTextSize(1.2f * mTextSize);

        float centerX, centerY;
        String text;
        for (int i = 0; i < mPoints.size(); i++) {
            text = mData.get(i).temperatureStr;
            centerX = mPoints.get(i).x;
            centerY = mPoints.get(i).y - dp2pxF(getContext(), 13);
            Paint.FontMetrics metrics = mTextPaint.getFontMetrics();
            canvas.drawText(text, centerX, centerY - (metrics.ascent + metrics.descent) / 2,
                    mTextPaint);

        }
        //回复字体大小
        mTextPaint.setTextSize(mTextSize);
        canvas.restore();
    }

    /**
     * 绘制折线和它的拐点
     */
    private void drawLinesAndPoints(Canvas canvas) {
        canvas.save();
        mLinePaint.setColor(DEFAULT_BULE);
        mLinePaint.setStrokeWidth(dp2pxF(getContext(), 1));
        mLinePaint.setStyle(Paint.Style.STROKE);

        //用于绘制折线
        Path linePath = new Path();
        mPoints.clear();
        int baseHeight = mDefaultPadding + mMinPointHeight;
        float centerX;
        float centerY;
        for (int i = 0; i < mData.size(); i++) {
            int tem = mData.get(i).temperature;
            tem = tem - mMinTemperature;
            centerX = mDefaultPadding + i * mLineInterval;
            centerY = (int) (mViewHeight - (baseHeight + tem * mPointGap));
            mPoints.add(new PointF(centerX, centerY));
            if (i == 0) {
                linePath.moveTo(centerX, centerY);
            } else {
                linePath.lineTo(centerX, centerY);
            }
        }
        //画出折线
        canvas.drawPath(linePath, mLinePaint);

        //画折线的拐点的圆点
        float x, y;
        for (int i = 0; i < mPoints.size(); i++) {
            x = mPoints.get(i).x;
            y = mPoints.get(i).y;

            //先画一个颜色为背景颜色的实心圆覆盖折线拐角
            mCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mCirclePaint.setColor(DEFAULT_BULE);
            canvas.drawCircle(x, y, mPointRadius + dp2pxF(getContext(), 1), mCirclePaint);
            //再画出正常的空心圆
            mCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mCirclePaint.setColor(mBackgroundColor);
            canvas.drawCircle(x, y, mPointRadius, mCirclePaint);

        }

        canvas.restore();
    }

    /**
     * 绘制时间轴
     */
    private void drawAxis(Canvas canvas) {
        canvas.save();
        mLinePaint.setColor(DEFAULT_GRAY);
        mLinePaint.setStrokeWidth(dp2px(getContext(), 1));

        canvas.drawLine(mDefaultPadding,
                mViewHeight - mDefaultPadding,
                mViewWidth - mDefaultPadding,
                mViewHeight - mDefaultPadding,
                mLinePaint);

        float centerY = mViewHeight - mDefaultPadding + dp2pxF(getContext(), 15);
        float centerX;
        for (int i = 0; i < mData.size(); i++) {
            String text = mData.get(i).time;
            centerX = mDefaultPadding + i * mLineInterval;
            Paint.FontMetrics metrics = mTextPaint.getFontMetrics();
            canvas.drawText(text, 0, text.length(), centerX, centerY - (metrics.ascent + metrics.descent) / 2, mTextPaint);
        }
        canvas.restore();
    }

    /**
     * 计算折线高度差
     */
    private void calculatePointGap() {
        int lastMaxTem = -Integer.MAX_VALUE;
        int lastMinTem = Integer.MAX_VALUE;
        for (BrokenLineBean brokenLineBean : mData) {
            if (brokenLineBean.temperature > lastMaxTem) {
                mMaxTemperature = brokenLineBean.temperature;
                lastMaxTem = brokenLineBean.temperature;
            }
            if (brokenLineBean.temperature < lastMinTem) {
                mMinTemperature = brokenLineBean.temperature;
                lastMinTem = brokenLineBean.temperature;
            }
        }
        float gap = (mMaxTemperature - mMinTemperature) * 1.0f;
        //保证分母不为零
        gap = (gap == 0.0f ? 1.0f : gap);
        mPointGap = (mViewHeight - mMinPointHeight - 2 * mDefaultPadding) / gap;
    }


    /**
     * 公开方法，用于设置元数据
     */
    public void setData(List<BrokenLineBean> data) {
        if (data == null) {
            return;
        }
        mData = data;
        notifyDataSetChanged();
    }

    public List<BrokenLineBean> getData() {
        return mData;
    }

    public void notifyDataSetChanged() {
        if (mData == null) {
            return;
        }
        mWeatherDates.clear();
        mPoints.clear();
        mDashDates.clear();

        initWeatherMap(); //初始化相邻的相同天气分组
        requestLayout();
        invalidate();
    }

    /**
     * 根据元数据中连续相同的天气数做分组，
     * pair中first值为连续相同天气的数量，second值对应天气
     */
    private void initWeatherMap() {
        mWeatherDates.clear();
        String lastWeather = "";
        int count = 0;
        for (int i = 0; i < mData.size(); i++) {
            BrokenLineBean brokenLineBean = mData.get(i);
            if (i == 0) {
                lastWeather = brokenLineBean.weather;
            }
            if (!brokenLineBean.weather.equals(lastWeather)) {
                //有连续相同的天气
                Pair<Integer, String> pair = new Pair<>(count, lastWeather);
                mWeatherDates.add(pair);
                count = 1;
            } else {
                count++;
            }
            lastWeather = brokenLineBean.weather;

            if (i == mData.size() - 1) {
                Pair<Integer, String> pair = new Pair<>(count, lastWeather);
                mWeatherDates.add(pair);
            }
        }
    }

    private void initPaint(Context context) {
        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStrokeWidth(dp2px(context, 1));

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(Color.WHITE);
        //能够实现文字水平居中，在绘制文字时传入文字中心x的坐标即可，而不需要向左偏移半个文本长度
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setStrokeWidth(dp2pxF(context, 1));
    }

    /**
     * 初始化默认数据
     */
    private void initSize(Context context) {
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;

        //默认3倍
        mMinViewHeight = 3 * mMinPointHeight;

        mPointRadius = dp2pxF(context, 2.5f);
        mTextSize = sp2pxF(context, 10);
        //默认0.5
        mDefaultPadding = (int) (0.5 * mMinPointHeight);
        //默认1/4倍
        mIconWidth = (1.0f / 4.0f) * mLineInterval;
    }


}
