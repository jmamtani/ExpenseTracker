package com.school.project.expensetracer.activities;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.school.project.expensetracer.R;

/**
 * Created by SUDESH on 2/11/2018.
 */

public class BarcharPopup extends MarkerView implements IMarker
{
    private TextView tvContent;
    private MPPointF mOffset;

    public BarcharPopup(Context context, int layoutResource)
    {
        super(context, layoutResource);
        tvContent = (TextView) findViewById(R.id.chart_value);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight)
    {
        tvContent.setText(e.getY()+"");
        super.refreshContent(e, highlight);
    }
    @Override
    public MPPointF getOffset()
    {
        if(mOffset == null) {mOffset = new MPPointF(-(getWidth() / 2), -getHeight());}
        return mOffset;
    }
}