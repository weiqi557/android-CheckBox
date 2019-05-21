package com.weiqi.android.checkbox;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Checkable;

public class CheckBoxView extends View implements Checkable {

    private static final String TAG = "CheckBoxView";

    public static final int DEFAULT_INNER_CIRCLE_COLOR = 0xFFFFFFFF;
    public static final int DEFAULT_OUT_CIRCLE_COLOR = 0xFF949495;
    private static final float DEFAULT_OFFSET_LENGTH = 1;

    private Paint mOutCirclePaint, mInnerCirclePaint, mTickPaint;

    private float mRadius;
    private int mWidth, mHeight;
    //外圆与内圆的距离
    private float mBorderWidth;

    private float mOutCircleScale = 1.0f, mInnerCircleScale = 1.0f;

    private int mOutCircleColor, mInnerCircleColor;

    private boolean mChecked;
    //对勾是否绘制
    private boolean isTickDrawed;

    //对勾三个点
    private Point[] points;
    private float leftTickDistance,rightTickDistance, drawTickDistance;
    private Path mTickPath;
    private float mPointX, mPointY;

    private int mAnimDuration = 2000;
    private int mCheckColor = Color.RED;
    private int mTickColor = Color.WHITE;

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
            array.recycle();
        }

        mOutCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOutCirclePaint.setStyle(Paint.Style.FILL);
        mOutCircleColor = DEFAULT_OUT_CIRCLE_COLOR;

        mInnerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerCirclePaint.setStyle(Paint.Style.FILL);
        mInnerCircleColor = DEFAULT_INNER_CIRCLE_COLOR;

        mTickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTickPaint.setStyle(Paint.Style.STROKE);
        mTickPaint.setStrokeWidth(12);
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
                isTickDrawed = false;
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

        mWidth = w;
        mHeight = h;
        mRadius = w / 2f;

        mBorderWidth = 10;

        points[0].set(
                (int) (4.1f * mWidth / 24), (int) (12.7f * mHeight / 24)
        );
        points[1].set(
                9 * mWidth / 24, 19 * mHeight / 24
        );
        points[2].set(
                (int) (20.3f * mWidth / 24), (int) (6.3f * mHeight / 24)
        );

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
        drawOutCircle(canvas);
        drawInnerCircle(canvas);
        drawTick(canvas);
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
        if (!isTickDrawed && isChecked()) {

            mTickPath.reset();

            if (drawTickDistance < leftTickDistance) {
                //画左边勾勾

                drawTickDistance += DEFAULT_OFFSET_LENGTH;

                mPointX = points[0].x + (points[1].x - points[0].x) * drawTickDistance / leftTickDistance;
                mPointY = points[0].y + (points[1].y - points[0].y) * drawTickDistance / leftTickDistance;

                mTickPath.moveTo(points[0].x, points[0].y);
                mTickPath.moveTo(mPointX, mPointY);

            }else if (drawTickDistance < leftTickDistance + rightTickDistance){

                //画右边勾勾

                drawTickDistance += DEFAULT_OFFSET_LENGTH;

                mPointX = points[1].x + (points[2].x - points[1].x) * (drawTickDistance - leftTickDistance) / rightTickDistance;
                mPointY = points[1].y + (points[2].y - points[1].y) * (drawTickDistance - leftTickDistance) / rightTickDistance;

                mTickPath.moveTo(points[0].x, points[0].y);
                mTickPath.lineTo(points[1].x, points[1].y);
                mTickPath.lineTo(mPointX, mPointY);

            }else {

                mTickPath.reset();
                mTickPath.moveTo(points[0].x, points[0].y);
                mTickPath.lineTo(points[1].x, points[1].y);
                mTickPath.lineTo(points[2].x, points[2].y);

            }

            canvas.drawPath(mTickPath,mTickPaint);


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
        radiusAnim.setDuration(mAnimDuration);
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
                        DEFAULT_OUT_CIRCLE_COLOR, mCheckColor,
                        (Float) animation.getAnimatedValue()
                );
                postInvalidate();
            }
        });
        innerAnim.start();
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
                        DEFAULT_OUT_CIRCLE_COLOR, mCheckColor,
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
