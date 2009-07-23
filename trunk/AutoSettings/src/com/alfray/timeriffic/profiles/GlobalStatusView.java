/*
 * (c) ralfoide gmail com, 2008
 * Project: Timeriffic
 * License TBD
 */

package com.alfray.timeriffic.profiles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.FontMetrics;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.alfray.timeriffic.R;

//-----------------------------------------------

/**
 */
public class GlobalStatusView extends View {

    private Bitmap mAccentLeft;
    private Matrix mAccentRightMatrix;

    private Bitmap mLogoGray;
    private Bitmap mLogoAnim[] = new Bitmap[3];
    private Paint mPaintLast;
    private Paint mPaintNext;
    private Paint mPaintTimestamp;
    private Paint mPaintSummary;
    private float mYLast;
    private float mYNext;
    private float mYSummary;
    private int mXLastNext;
    private int mXTsSummary;
    private String mTextLast;
    private String mTextNext;
    private String mTextLastTs;
    private String mTextNextTs;
    private String mTextSummary;

    public GlobalStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mAccentLeft = getResBitmap(R.drawable.globalstatus_accent_lines_left);

        mLogoGray = getResBitmap(R.drawable.globalstatus_disabled);

        mLogoAnim[0] = getResBitmap(R.drawable.globalstatus_frame1);
        mLogoAnim[1] = getResBitmap(R.drawable.globalstatus_frame2);
        mLogoAnim[2] = getResBitmap(R.drawable.globalstatus_frame3);

        int textFlags = Paint.ANTI_ALIAS_FLAG + Paint.SUBPIXEL_TEXT_FLAG;
        mPaintLast = new Paint(textFlags);
        mPaintLast.setColor(0xFF70D000);
        mPaintLast.setTextSize(16);

        mPaintNext = new Paint(textFlags);
        mPaintNext.setColor(0xFF392394);
        mPaintNext.setTextSize(16);

        mPaintTimestamp = new Paint(textFlags);
        mPaintTimestamp.setColor(0xFFCCCCCC);
        mPaintTimestamp.setTextSize(12);

        mPaintSummary = new Paint(textFlags);
        mPaintSummary.setColor(0xFF181818);
        mPaintSummary.setTextSize(12);

        FontMetrics fmLast = mPaintLast.getFontMetrics();
        FontMetrics fmTs = mPaintTimestamp.getFontMetrics();
        mYLast = -1 * fmLast.top;
        mYNext = mYLast - fmLast.ascent + fmTs.descent;
        mYSummary = mYNext + fmTs.bottom - fmTs.top;

        mTextLast = "Last:";
        mTextNext = "Next:";

        mXLastNext = mLogoAnim[2].getWidth();
        mXTsSummary = mXLastNext + 5 +
                        (int) (Math.max(mPaintLast.measureText(mTextLast),
                                        mPaintNext.measureText(mTextNext)));

        // DEBUG
        mTextLastTs = "Timestamp Last";
        mTextNextTs = "Timestamp Next";
        mTextSummary = "Summary blah blah blah blah blah blah";
    }

    private Bitmap getResBitmap(int bmpResId) {
        Drawable d = getResources().getDrawable(bmpResId);
        int w = d.getIntrinsicWidth();
        int h = d.getIntrinsicHeight();
        Bitmap bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        d.setBounds(0, 0, w - 1, h - 1);
        d.draw(c);
        return bmp;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return mAccentLeft.getHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(getSuggestedMinimumHeight(),
                                                        MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mAccentRightMatrix = new Matrix();
        mAccentRightMatrix.preTranslate(w, 0);
        mAccentRightMatrix.preScale(-1, 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(mAccentLeft, 0, 0, null);
        canvas.drawBitmap(mAccentLeft, mAccentRightMatrix, null);

        canvas.drawBitmap(mLogoAnim[2], 0, 0, null);

        canvas.drawText(mTextLast, mXLastNext, mYLast, mPaintLast);
        canvas.drawText(mTextNext, mXLastNext, mYNext, mPaintNext);
        canvas.drawText(mTextLastTs, mXTsSummary, mYLast, mPaintTimestamp);
        canvas.drawText(mTextNextTs, mXTsSummary, mYNext, mPaintTimestamp);
        canvas.drawText(mTextSummary, mXTsSummary, mYSummary, mPaintSummary);
    }


}


