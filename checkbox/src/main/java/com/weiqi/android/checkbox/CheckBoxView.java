package com.weiqi.android.checkbox;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Checkable;


public class CheckBoxView extends View implements Checkable {

    public interface OnCheckedChangeListener {
        void onCheckedChanged(CheckBoxView view, boolean isChecked);
    }

    private OnCheckedChangeListener listener;

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.listener = listener;
    }

    private static final String TAG = "CheckBoxView";

    public static final int DEFAULT_INNER_CIRCLE_COLOR = 0xFFFFFFFF;
    public static final int DEFAULT_OUT_CIRCLE_COLOR = 0xFF949495;
    public static final int DEFAULT_CHECK_COLOR = 0xFFF53341;
    private static final float DEFAULT_OFFSET_LENGTH = 3;
    private static final int DEFAULT_MIN_WIDTH_HEIGHT = 15;

    private int mWidth, mHeight;
    private float mRadius;

    private Paint mOutCirclePaint;
    private int mOutCircleColor;
    private float mOutCircleScale = 1.0f;

    private Paint mInnerCirclePaint;
    private int mInnerCircleColor;
    private float mInnerCircleScale = 1.0f;

    private Paint mTickPaint;
    private int mTickColor;
    private Point[] points;
    private Path mTickPath;
    private boolean isTickDrawer;
    private float offSetTickDistance;
    private float leftTickDistance, rightTickDistance, drawTickDistance;

    //外圆与内圆的距离
    private float mBorderWidth;
    //边框颜色
    private int mBorderColor;
    //选中颜色
    private int mCheckColor;
    //动画时长
    private int mAnimDuration = 300;


    private boolean mChecked;

    public CheckBoxView(Context context) {
        this(context, null);
    }

    public CheckBoxView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckBoxView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs);
    }

    private void initialize(Context context, AttributeSet attrs) {

        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CheckBoxView);

            int temp;

            //对勾颜色
            mTickColor = array.getColor(R.styleable.CheckBoxView_tick_color, Color.WHITE);
            temp = array.getResourceId(R.styleable.CheckBoxView_tick_color, 0);
            if (temp != 0) {
                mTickColor = ContextCompat.getColor(context, temp);
            }

            //内圆颜色
            mInnerCircleColor = array.getColor(R.styleable.CheckBoxView_inner_color, DEFAULT_INNER_CIRCLE_COLOR);
            temp = array.getResourceId(R.styleable.CheckBoxView_inner_color, 0);
            if (temp != 0) {
                mInnerCircleColor = ContextCompat.getColor(context, temp);
            }

            //边框颜色
            mBorderColor = array.getColor(R.styleable.CheckBoxView_border_color, DEFAULT_OUT_CIRCLE_COLOR);
            temp = array.getResourceId(R.styleable.CheckBoxView_border_color, 0);
            if (temp != 0) {
                mBorderColor = ContextCompat.getColor(context, temp);
            }

            //勾选颜色
            mCheckColor = array.getColor(R.styleable.CheckBoxView_check_color, DEFAULT_CHECK_COLOR);
            temp = array.getResourceId(R.styleable.CheckBoxView_check_color, 0);
            if (temp != 0) {
                mCheckColor = ContextCompat.getColor(context, temp);
            }
            //动画时长
            mAnimDuration = array.getColor(R.styleable.CheckBoxView_anim_duration, mAnimDuration);
            array.recycle();
        }

        mOutCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOutCirclePaint.setStyle(Paint.Style.FILL);
        mOutCircleColor = mBorderColor;

        mInnerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerCirclePaint.setStyle(Paint.Style.FILL);

        mTickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTickPaint.setStyle(Paint.Style.STROKE);
        mTickPaint.setStrokeCap(Paint.Cap.ROUND);
        mTickPaint.setColor(mTickColor);


        points = new Point[3];
        points[0] = new Point();
        points[1] = new Point();
        points[2] = new Point();

        mTickPath = new Path();

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle();
                isTickDrawer = false;
                if (isChecked()) {
                    startCheckAnim();
                } else {
                    startUnCheckAnim();
                    drawTickDistance = 0;
                }
            }
        });

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w - getPaddingStart() - getPaddingEnd();
        mHeight = h - getPaddingTop() - getPaddingBottom();
        mRadius = mWidth / 2f;

        mBorderWidth = mWidth * 2 / 44f;
        float mTickPathStroke = mWidth * 4 / 44f;
        mTickPaint.setStrokeWidth(mTickPathStroke);

        offSetTickDistance = (float) mWidth / DEFAULT_MIN_WIDTH_HEIGHT < DEFAULT_OFFSET_LENGTH ?
                DEFAULT_OFFSET_LENGTH : (float) mWidth / DEFAULT_MIN_WIDTH_HEIGHT;

        points[0].set(10 * mWidth / 44, 25 * mHeight / 44);
        points[1].set(19 * mWidth / 44, 14 * mHeight / 44);
        points[2].set(35 * mWidth / 44, 31 * mHeight / 44);

        leftTickDistance = (float) Math.sqrt(
                Math.pow(points[1].x - points[0].x, 2) +
                        Math.pow(points[1].y - points[0].y, 2)
        );
        rightTickDistance = (float) Math.sqrt(
                Math.pow(points[2].x - points[1].x, 2) +
                        Math.pow(points[2].y - points[1].y, 2)
        );

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(getPaddingStart(), getPaddingTop());

        drawOutCircle(canvas);
        drawInnerCircle(canvas);
        drawTick(canvas);

        canvas.restore();
    }

    private void drawOutCircle(Canvas canvas) {

        canvas.save();
        canvas.translate(mRadius, mRadius);
        mOutCirclePaint.setColor(mOutCircleColor);
        canvas.drawCircle(0, 0, mRadius * mOutCircleScale, mOutCirclePaint);
        canvas.restore();

    }

    private void drawInnerCircle(Canvas canvas) {

        canvas.save();
        canvas.translate(mRadius, mRadius);
        mInnerCirclePaint.setColor(mInnerCircleColor);
        canvas.drawCircle(0, 0, (mRadius - mBorderWidth) * mInnerCircleScale, mInnerCirclePaint);
        canvas.restore();

    }

    private void drawTick(Canvas canvas) {
        if (isTickDrawer && isChecked()) {

            canvas.save();
            canvas.translate(0, mHeight);
            canvas.scale(1, -1);

            mTickPath.reset();

            float mPointX;
            float mPointY;
            if (drawTickDistance < leftTickDistance) {
                //画左边勾勾

                drawTickDistance += offSetTickDistance;

                mPointX = points[0].x + (points[1].x - points[0].x) * drawTickDistance / leftTickDistance;
                mPointY = points[0].y + (points[1].y - points[0].y) * drawTickDistance / leftTickDistance;

                mTickPath.moveTo(points[0].x, points[0].y);
                mTickPath.moveTo(mPointX, mPointY);

            } else if (drawTickDistance < leftTickDistance + rightTickDistance) {

                //画右边勾勾

                drawTickDistance += offSetTickDistance;

                mPointX = points[1].x + (points[2].x - points[1].x) * (drawTickDistance - leftTickDistance) / rightTickDistance;
                mPointY = points[1].y + (points[2].y - points[1].y) * (drawTickDistance - leftTickDistance) / rightTickDistance;

                mTickPath.moveTo(points[0].x, points[0].y);
                mTickPath.lineTo(points[1].x, points[1].y);
                mTickPath.lineTo(mPointX, mPointY);

            } else {

                mTickPath.reset();
                mTickPath.moveTo(points[0].x, points[0].y);
                mTickPath.lineTo(points[1].x, points[1].y);
                mTickPath.lineTo(points[2].x, points[2].y);

            }

            canvas.drawPath(mTickPath, mTickPaint);

            canvas.restore();

            if (drawTickDistance < leftTickDistance + rightTickDistance) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        postInvalidate();
                    }
                }, 10);
            }

        }
    }


    @Override
    public void setChecked(boolean checked) {
        mChecked = checked;
        reset();
        if (mChecked) {
            startCheckAnim();
        }else {
            startUnCheckAnim();
        }
        if (listener != null) {
            listener.onCheckedChanged(this, mChecked);
        }
    }

    private void reset() {
        isTickDrawer = false;
        drawTickDistance = 0;
        mOutCircleScale = 1.0f;
        mInnerCircleScale = isChecked() ? 0f : 1.0f;
        mOutCircleColor = isChecked() ? mBorderColor : mCheckColor;
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        this.setChecked(!mChecked);
    }

    private void startCheckAnim() {

        ValueAnimator radiusAnim = ValueAnimator.ofFloat(1.0f, 0.8f, 1.0f);
        radiusAnim.setInterpolator(new LinearInterpolator());
        radiusAnim.setDuration(mAnimDuration * 3 / 2);
        radiusAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mOutCircleScale = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        radiusAnim.start();

        ValueAnimator innerAnim = ValueAnimator.ofFloat(1.0f, 0f);
        innerAnim.setInterpolator(new LinearInterpolator());
        innerAnim.setDuration(mAnimDuration);
        innerAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mInnerCircleScale = (float) animation.getAnimatedValue();
                mOutCircleColor = getGradientColor(
                        mBorderColor, mCheckColor,
                        (Float) animation.getAnimatedValue()
                );
                postInvalidate();
            }
        });
        innerAnim.start();

        drawTickDelay();
    }

    private void drawTickDelay() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                isTickDrawer = true;
                postInvalidate();
            }
        }, mAnimDuration);
    }

    private void startUnCheckAnim() {

        ValueAnimator radiusAnim = ValueAnimator.ofFloat(1.0f, 0.8f, 1.0f);
        radiusAnim.setInterpolator(new LinearInterpolator());
        radiusAnim.setDuration(mAnimDuration);
        radiusAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mOutCircleScale = (float) animation.getAnimatedValue();
                postInvalidate();
            }
        });
        radiusAnim.start();

        ValueAnimator innerAnim = ValueAnimator.ofFloat(0f, 1.0f);
        innerAnim.setInterpolator(new LinearInterpolator());
        innerAnim.setDuration(mAnimDuration);
        innerAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mInnerCircleScale = (float) animation.getAnimatedValue();
                mOutCircleColor = getGradientColor(
                        mBorderColor, mCheckColor,
                        (Float) animation.getAnimatedValue()
                );
                postInvalidate();
            }
        });
        innerAnim.start();


    }

    private int getGradientColor(int startColor, int endColor, float percent) {

        int startA = Color.alpha(startColor);
        int startR = Color.red(startColor);
        int startG = Color.green(startColor);
        int startB = Color.blue(startColor);

        int endA = Color.alpha(endColor);
        int endR = Color.red(endColor);
        int endG = Color.green(endColor);
        int endB = Color.blue(endColor);

        int currentA = (int) (startA * percent + endA * (1 - percent));
        int currentR = (int) (startR * percent + endR * (1 - percent));
        int currentG = (int) (startG * percent + endG * (1 - percent));
        int currentB = (int) (startB * percent + endB * (1 - percent));

        return Color.argb(currentA, currentR, currentG, currentB);
    }
}
