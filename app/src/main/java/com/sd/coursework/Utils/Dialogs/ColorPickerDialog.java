package com.sd.coursework.Utils.Dialogs;

/* Credits to: https://github.com/javalc6/simple-colorpicker-dialog */

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.support.v4.graphics.ColorUtils;
import android.view.MotionEvent;
import android.view.View;

import com.sd.coursework.R;

public class ColorPickerDialog extends Dialog {

    private final OnColorChangedListener mListener;
    private final int mInitialColor;
    private final String mTitle;
    private final int[] mColors;
    private final int N;

    public interface OnColorChangedListener {
        void colorChanged(int color);
    }

    public ColorPickerDialog(Context context, int initialColor, String title, int[] colors, OnColorChangedListener listener) {
        super(context);
        mListener = listener;
        mInitialColor = initialColor;
        mTitle = title;
        mColors = colors;
        int n = 1;
        while (n * n < colors.length) n++;
        N = n;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new ColorPickerView(getContext(), mListener, mInitialColor));
        setTitle(mTitle);
    }

    private class ColorPickerView extends View {
        private final Paint mPaint;
        private int mCurrentColor;
        private final OnColorChangedListener mListener;
        private final Bitmap icon;

        ColorPickerView(Context ctx, OnColorChangedListener listener, int color) {
            super(ctx);
            mListener = listener;
            mCurrentColor = color;
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            icon = BitmapFactory.decodeResource(getResources(),R.drawable.icon_check_mark);
        }

        int mMarginSize;
        int offsetY;
        int offsetX;
        int gridL = 64;

        @Override
        protected void onDraw(Canvas canvas) {
            for (int i = 0; i < N; i++)
                for (int j = 0; j < N; j++) {
                    int index = i + j * N;
                    if (index < mColors.length) {//sub-optimal solution
                        int x = i * gridL + offsetX;
                        int y = j * gridL + offsetY;

                        int radius = (int)(gridL /2.1);

                        // draw circle
                        mPaint.setColor(mColors[i + j * N]);
                        mPaint.setStyle(Paint.Style.FILL);
                        canvas.drawCircle(x + radius, y + radius, radius  - mMarginSize, mPaint);

                        //draw border
                        int color_darker = ColorUtils.blendARGB(Color.BLACK, mColors[i + j * N],0.90f);
                        mPaint.setColor(color_darker);
                        mPaint.setStyle(Paint.Style.STROKE);
                        mPaint.setStrokeWidth(7);

                        if (mColors[index] == mCurrentColor) {
                            mPaint.setStrokeWidth(15);
                        } else {
                            mPaint.setStrokeWidth(9);
                        }
                        canvas.drawCircle(x + radius, y + radius, radius  - mMarginSize, mPaint);

                        //draw check mark
                        if (mColors[index] == mCurrentColor) {
                            mPaint.setColor(ColorUtils.blendARGB(Color.BLACK, mColors[i + j * N],0.90f));
                            mPaint.setColorFilter(new PorterDuffColorFilter(color_darker, PorterDuff.Mode.SRC_IN));
                            canvas.drawBitmap(icon, x + (radius-mMarginSize)/2f, y + (radius-mMarginSize)/2f, mPaint);
                            mPaint.setColorFilter(null);
                        }

                    }
                }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            int w = MeasureSpec.getSize(widthMeasureSpec); //getMeasuredWidth();
            int h = MeasureSpec.getSize(heightMeasureSpec); //getMeasuredHeight();
            int mSwatchLength;
            Resources res = getResources();
            mSwatchLength = res.getDimensionPixelSize(R.dimen.color_swatch);
            mMarginSize = res.getDimensionPixelSize(R.dimen.color_swatch_margins);

            int extraheight = 2 * mMarginSize;
            if (w > h - extraheight) {
                gridL = (h - extraheight - 1) / N;
                if (gridL > mSwatchLength) {
                    gridL = mSwatchLength;
                    w = gridL * N + gridL/2;
                    h = w + extraheight;
                }

            } else {
                gridL = (w - 1) / N;
                if (gridL > mSwatchLength) {
                    gridL = mSwatchLength;
                    w = gridL * N + gridL/2;
                    h = w;
                }
            }

            offsetX = (gridL/4) + extraheight;
            offsetY = offsetX;
            setMeasuredDimension(w, h);

        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                float y = event.getY();
                if ((x >= offsetX) && (y >= offsetY)) {
                    int transX = ((int) x - offsetX) / gridL;
                    int transY = ((int) y - offsetY) / gridL;
                    if (transX < N && transY < N) {
                        int index = N * transY + transX;
                        if (index < mColors.length) {
                            mCurrentColor = mColors[index];
                            mListener.colorChanged(mCurrentColor);
                            dismiss();
                        }
                    }
                }
            }
            return true;
        }
    }

}