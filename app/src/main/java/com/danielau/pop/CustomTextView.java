/**
 *
 * Author: Daniel Tak Yin Au
 *
 * Version 1.0
 *
 * Typography font, 'Fins', created by Jake Kho.
 *
 */

package com.danielau.pop;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class CustomTextView extends TextView {

    public CustomTextView(Context context) {
        super(context);
        setFont();
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont();
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFont();
    }

    private void setFont() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "Fins-Regular.ttf");
        setTypeface(font, Typeface.NORMAL);
        setTextColor(Color.parseColor(ColourManager.getColourHex()));
    }

}
