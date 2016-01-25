package liumeng.com.numberpicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lm on 2015/12/18.
 */
public class NumberPicker extends View {

    private Bitmap mSliderBitmap;
    private int mTextColor;
    private float mTextSize;
    private int mBackgroundColor;
    private int mProgressColor;
    private Paint mTextPaint;
    private Paint mBackgroundPaint;
    private Paint mProgressPaint;
    private Paint mLinePaint;
    private Paint mSliderPaint;
    private Rect mLeftSliderR;
    private Rect mRightSliderR;
    private Rect backR = new Rect();
    private int padding = 20;//
    private int backgroundHeight = 10;
    private List<String> nums;
    private int mSliderWidth;
    private int mSegmentWidth;//每一段的长度
    private static int LINE_HEIGHT = 30;
    private int mLeftSliderIndex, mRightSliderIndex;
    private int lastX, lastY;
    private ArrayList<P> mPoints = new ArrayList<P>();//记录每个段的起始和终点
    private boolean isMoveLeft, isMoveRight;

    public NumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumberPicker);
        BitmapDrawable slider = (BitmapDrawable) a.getDrawable(R.styleable.NumberPicker_slider);
        mSliderBitmap = slider.getBitmap();
        mTextColor = a.getColor(R.styleable.NumberPicker_textColor, getResources().getColor(R.color.uthing_color_333333));
        mTextSize = a.getDimension(R.styleable.NumberPicker_textSize, getResources().getDimension(R.dimen.font_size_remind));
        mBackgroundColor = a.getColor(R.styleable.NumberPicker_backgroundCcolor, getResources().getColor(R.color.uthing_color_e0e0e0));
        mProgressColor = a.getColor(R.styleable.NumberPicker_progressColor, getResources().getColor(R.color.uthing_color_1fb6c4));
        init();
        initNums();
    }

    private ArrayList<String> initNums() {
        ArrayList<String> defaultNum = new ArrayList<String>();
        defaultNum.add("0");
        defaultNum.add("1");
        defaultNum.add("2");
        defaultNum.add("3");
        defaultNum.add("4");
        defaultNum.add("5");
        defaultNum.add("6");
        defaultNum.add("7");
        defaultNum.add("8");
        defaultNum.add("8+");
        return defaultNum;
    }

    private void init() {
        mTextPaint = new Paint();
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setAntiAlias(true);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setColor(mBackgroundColor);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setStrokeJoin(Paint.Join.ROUND);
        mBackgroundPaint.setStrokeWidth(backgroundHeight);

        mProgressPaint = new Paint();
        mProgressPaint.setColor(mProgressColor);
        mProgressPaint.setAntiAlias(true);
        mProgressPaint.setStrokeWidth(backgroundHeight);

        mLinePaint = new Paint();
        mLinePaint.setColor(mBackgroundColor);
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(3);

        mSliderPaint = new Paint();
        mSliderPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mSliderWidth = mSliderBitmap.getWidth();
        backR = new Rect(padding + mSliderWidth / 2, (mSliderWidth - backgroundHeight) / 2, getMeasuredWidth() - (padding + mSliderWidth / 2), backgroundHeight / 2 + mSliderWidth / 2);
        mLeftSliderR = new Rect(padding, 0, padding + mSliderWidth, mSliderWidth);
        mRightSliderR = new Rect(backR.right - mSliderWidth / 2, 0, backR.right + mSliderWidth / 2, mSliderWidth);

        int height = (int) (mSliderWidth + LINE_HEIGHT + mTextPaint.getFontMetrics().descent - mTextPaint.getFontMetrics().top + 10);
        setMeasuredDimension(widthMeasureSpec, height);
        /**onMeasure会调用多次**/
        if (nums == null || nums.size() == 0) {
            setText(initNums());
        }

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        canvas.drawRect(backR, mBackgroundPaint);
        canvas.drawBitmap(mSliderBitmap, mLeftSliderR.left, mLeftSliderR.top, null);
        canvas.drawBitmap(mSliderBitmap, mRightSliderR.left, mRightSliderR.top, null);
        drawLine(canvas);
        drawText(canvas);
        drawProgress(canvas);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = x;


                break;
            case MotionEvent.ACTION_MOVE:
                int disX = x - lastX;
                if (isCanMoveLeftSlider(x, y)) {
                    isMoveLeft = true;
                    isMoveRight = false;
                    updateLeftSliderPosition(disX);

                } else if (isCanMoveRightSlider(x, y)) {
                    isMoveLeft = false;
                    isMoveRight = true;
                    updateRightSliderPosition(disX);
                }
                lastX = x;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                shouldMoveDist();
                if (null != mListener) {
                    if (mRightSliderIndex == nums.size()) {
                        mRightSliderIndex -= 1;
                    }

                    if (mLeftSliderIndex == 0 && mRightSliderIndex == nums.size() - 1) {
                        mListener.numberChange(null, null);
                    } else {
                        mListener.numberChange(nums.get(mLeftSliderIndex), nums.get(mRightSliderIndex));
                    }

                }
                break;
        }
        return true;
    }

    private void shouldMoveDist() {
        if (isMoveLeft) {
            P p = mPoints.get(mLeftSliderIndex);
            int middle = mLeftSliderR.left + mSliderWidth / 2;
            if (Math.abs(p.getStartX() - middle) <= Math.abs(p.getEndX() - middle)) {
                updateLeftSliderPosition(p.getStartX() - middle);
            } else {
                updateLeftSliderPosition(p.getEndX() - middle);
            }

        } else if (isMoveRight) {
            if (mRightSliderIndex == mPoints.size() ) {
                mRightSliderIndex = mRightSliderIndex - 1;
            }
            P p = mPoints.get(mRightSliderIndex);
            int middle = mRightSliderR.left + mSliderWidth / 2;
            if (Math.abs(p.getStartX() - middle) <= Math.abs(p.getEndX() - middle)) {
                updateRightSliderPosition(p.getStartX() - middle);
            } else {
                updateRightSliderPosition(p.getEndX() - middle);
            }
        }

    }

    private boolean isCanMoveLeftSlider(int x, int y) {
        if (mLeftSliderR.contains(x, y) && mLeftSliderR.right < mRightSliderR.left) {
            return true;
        }
        return false;
    }

    private boolean isCanMoveRightSlider(int x, int y) {
        if (mRightSliderR.contains(x, y) && mRightSliderR.left > mLeftSliderR.left) {
            return true;
        }
        return false;
    }

    /**
     * 更新左侧滑块的位置
     *
     * @param disX
     */
    private void updateLeftSliderPosition(int disX) {
        int left = mLeftSliderR.left + disX;
        int right = mLeftSliderR.right + disX;
        if (disX < 0 && left < padding) {
            disX = disX - (left - padding);
        }

        if (disX > 0 && mRightSliderR.left - right < mSegmentWidth - mSliderWidth) {
            disX -= mSegmentWidth - mSliderWidth - (mRightSliderR.left - right);
        }

        mLeftSliderR.left = mLeftSliderR.left + disX;
        mLeftSliderR.right = mLeftSliderR.right + disX;
        mLeftSliderIndex = getCurrentIndex(mLeftSliderR);

        invalidate();
    }

    /**
     * 更新右侧滑块位置
     *
     * @param disX
     */
    private void updateRightSliderPosition(int disX) {
        int left = mRightSliderR.left + disX;
        int right = mRightSliderR.right + disX;
        if (disX > 0 && right > backR.right + mSliderWidth / 2) {
            disX -= right - backR.right - mSliderWidth / 2;
        }

        if (disX < 0 && left - mLeftSliderR.right < mSegmentWidth - mSliderWidth) {
            disX += mSegmentWidth - mSliderWidth - (mRightSliderR.left - mRightSliderR.left);
        }
        mRightSliderR.left = mRightSliderR.left + disX;
        mRightSliderR.right = mRightSliderR.right + disX;
        mRightSliderIndex = getCurrentIndex(mRightSliderR);
        invalidate();
    }

    /**
     * 绘制进度
     *
     * @param canvas
     */
    private void drawProgress(Canvas canvas) {
        canvas.drawRect(mLeftSliderR.right, backR.top, mRightSliderR.left, backR.bottom, mProgressPaint);
    }

    public void setText(List<String> texts) {
        this.nums = texts;
        if (null == texts) {
            throw new RuntimeException("texts can't be null");
        }
        mSegmentWidth = (backR.right - backR.left) / (nums.size() - 1);
        for (int i = 0; i < nums.size() - 1; i++) {
            P p = new P(backR.left + i * mSegmentWidth, backR.left + (i + 1) * mSegmentWidth);
            mPoints.add(p);
        }
        mLeftSliderIndex = 0;
        mRightSliderIndex = mPoints.size();
        invalidate();
    }


    private int getCurrentIndex(Rect rect) {
        int x = rect.left + mSliderWidth / 2;
        for (P p : mPoints) {
            if (p.contains(x)) {
                return mPoints.indexOf(p);
            }
        }
        return mPoints.size();


    }

    /**
     * 绘制文字
     *
     * @param canvas
     */
    public void drawText(Canvas canvas) {
        if (null != nums && nums.size() > 0) {
            for (int i = 0; i < nums.size(); i++) {
                String s = nums.get(i);
                int textWidth = (int) mTextPaint.measureText(s);
                if (i == nums.size() - 1) {
                    canvas.drawText(s, mPoints.get(i - 1).getEndX() - textWidth, mLeftSliderR.bottom + LINE_HEIGHT + 20 + ((Math.abs(mTextPaint.ascent() - Math.abs(mTextPaint.descent()))) / 2), mTextPaint);
                } else {
                    canvas.drawText(s, mPoints.get(i).getStartX() - textWidth / 2, mLeftSliderR.bottom + LINE_HEIGHT + 20 + ((Math.abs(mTextPaint.ascent() - Math.abs(mTextPaint.descent()))) / 2), mTextPaint);
                }
            }
            int textWidth = (int) mTextPaint.measureText("(万)");
            canvas.drawText("(万)", mPoints.get(mPoints.size() - 1).getEndX(), mLeftSliderR.bottom + LINE_HEIGHT + 20 + ((Math.abs(mTextPaint.ascent() - Math.abs(mTextPaint.descent()))) / 2), mTextPaint);
        }
    }

    /**
     * 绘制竖线
     *
     * @param canvas
     */
    private void drawLine(Canvas canvas) {

        if (null != mPoints && mPoints.size() > 0) {
            for (int i = 0; i <= mPoints.size(); i++) {
                if (i >= mLeftSliderIndex && i <= mRightSliderIndex) {
                    mLinePaint.setColor(mProgressColor);
                } else {
                    mLinePaint.setColor(mBackgroundColor);
                }
                if (i == mPoints.size()) {
                    canvas.drawLine(mPoints.get(mPoints.size() - 1).getEndX(), mLeftSliderR.bottom + 10, mPoints.get(mPoints.size() - 1).getEndX(), mLeftSliderR.bottom + LINE_HEIGHT, mLinePaint);
                } else {
                    canvas.drawLine(mPoints.get(i).getStartX(), mLeftSliderR.bottom + 10, mPoints.get(i).getStartX(), mLeftSliderR.bottom + LINE_HEIGHT, mLinePaint);
                }

            }


        }

    }


    class P {
        private int startX, endX;

        public P(int startX, int endX) {
            this.startX = startX;
            this.endX = endX;
        }

        public int getStartX() {
            return startX;
        }

        public void setStartX(int startX) {
            this.startX = startX;
        }

        public int getEndX() {
            return endX;
        }

        public void setEndX(int endX) {
            this.endX = endX;
        }

        public boolean contains(int x) {
            if (x >= startX && x < endX) {
                return true;
            }
            return false;
        }
    }

    public static interface OnNumberChangeListener {
        public void numberChange(String start, String end);
    }

    private OnNumberChangeListener mListener;

    public void setOnNumberChangeListener(OnNumberChangeListener mListener) {
        this.mListener = mListener;
    }
}
