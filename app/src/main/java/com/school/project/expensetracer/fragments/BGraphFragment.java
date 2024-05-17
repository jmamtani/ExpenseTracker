package com.school.project.expensetracer.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.school.project.expensetracer.R;
import com.school.project.expensetracer.db.ExpenseDbHelper;

import java.util.ArrayList;

public class BGraphFragment extends Fragment implements OnChartValueSelectedListener {


    String rType="income";
    View rootView;


    BarChart mChart;
    float barWidth = 0.3f;
    float barSpace = 0.05f;
    float groupSpace = 0.3f;

    ExpenseDbHelper expenseDbHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.b_fragment_graph, container, false);

        expenseDbHelper = new ExpenseDbHelper(getActivity());




        // For Bar Chart
        mChart = (BarChart) rootView.findViewById(R.id.chart);

        setDataInBarChart();

        return rootView;
    }


    // For Bar Chart
    private void setDataInBarChart()
    {
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDescription(null);
        mChart.setPinchZoom(false);
        mChart.setScaleEnabled(false);
        mChart.setDrawBarShadow(true);
        mChart.setDrawGridBackground(true);
        mChart.setFitBars(true);
        mChart.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);


        int groupCount = 12;
        ArrayList<String> xVals = new ArrayList<String>();
        xVals.add("Jan");
        xVals.add("Feb");
        xVals.add("Mar");
        xVals.add("Apr");
        xVals.add("May");
        xVals.add("Jun");
        xVals.add("Jul");
        xVals.add("Aug");
        xVals.add("Sep");
        xVals.add("Oct");
        xVals.add("Nov");
        xVals.add("Dec");

        ArrayList<String> ylabels = new ArrayList<>();
        ylabels.add("");

        ArrayList<BarEntry> yVals1 = new ArrayList<>();
        ArrayList<BarEntry> yVals2 = new ArrayList<BarEntry>();

        for (int i = 1; i <= 12; i++)
        {
            String mCode = "";
            if(i<10) { mCode = "0"+i; }
            else { mCode = ""+i; }



            yVals1.add(new BarEntry(i,(float) expenseDbHelper._TotalFilter("income",mCode)));
            yVals2.add(new BarEntry(i, (float) expenseDbHelper._TotalFilter("expense",mCode)));


        }




        BarDataSet set1, set2;
        set1 = new BarDataSet(yVals1, "Income Amount");
        set1.setColor(Color.GREEN);
        set2 = new BarDataSet(yVals2, "Expense Amount");
        set2.setColor(Color.RED);
        BarData data = new BarData(set1, set2);
        data.setDrawValues(true);


        mChart.setData(data);
        mChart.getBarData().setBarWidth(barWidth);
        mChart.getXAxis().setAxisMinimum(0);
        mChart.getXAxis().setAxisMaximum(0 + mChart.getBarData().getGroupWidth(groupSpace, barSpace) * groupCount);
        mChart.groupBars(0, groupSpace, barSpace);
        mChart.invalidate();




        Legend legend = mChart.getLegend();
        legend.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        legend.setTextSize(10);
        legend.setFormSize(10);
        legend.setTextColor(Color.BLACK);
        legend.setYOffset(10f);
        legend.setXOffset(0f);
        legend.setYEntrySpace(15f);



        //X-axis
        XAxis xAxis = mChart.getXAxis();
        xAxis.setLabelCount(12,false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setCenterAxisLabels(true);
        xAxis.setDrawGridLines(false);
        xAxis.setAxisMaximum(12);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xVals));
        //Y-axis
        mChart.getAxisRight().setEnabled(false);
        YAxis yAxis = mChart.getAxisLeft();
        yAxis.setLabelCount(12,false);
        yAxis.setDrawGridLines(true);
        yAxis.setGranularityEnabled(true);
        yAxis.setCenterAxisLabels(true);
        yAxis.setGranularity(20f); // interval 1
        yAxis.setAxisMinimum(0f);
        yAxis.setSpaceTop(10f);
        yAxis.setDrawGridLines(false);
        yAxis.setEnabled(true);
        yAxis.setValueFormatter(new IndexAxisValueFormatter(ylabels));


    }





    @Override
    public void onValueSelected(Entry entry, Highlight highlight) {
       /* IMarker marker = new BarcharPopup(getActivity(),R.layout.barchart_menu);
        mChart.setMarker(marker);*/
    }

    @Override
    public void onNothingSelected() {

    }
}
