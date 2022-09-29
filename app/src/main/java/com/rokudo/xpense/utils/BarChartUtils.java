package com.rokudo.xpense.utils;

import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;

public class BarChartUtils {
    public static void setupBarChart(BarChart barChart, int textColor) {
        BarData data = new BarData(getDataSet(textColor));
        barChart.setData(data);
        barChart.setMaxVisibleValueCount(60);
        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setScaleEnabled(false);
//        barChart.setTouchEnabled(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawGridBackground(false);
        barChart.getLegend().setEnabled(false);
        barChart.setFitBars(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(textColor);

        barChart.getAxisLeft().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);

        barChart.animateY(2000);
        barChart.invalidate();

        barChart.getDescription().setEnabled(false);
    }

    private static ArrayList<IBarDataSet> getDataSet(int textColor) {
        ArrayList<IBarDataSet> dataSets;

        ArrayList<BarEntry> valueSet = new ArrayList<>();
        BarEntry v1e1 = new BarEntry(18, 821); // Jan
        valueSet.add(v1e1);
        BarEntry v1e2 = new BarEntry(19, 334); // Feb
        valueSet.add(v1e2);
        BarEntry v2e1 = new BarEntry(20, 1179); // Jan
        valueSet.add(v2e1);
        BarEntry v2e2 = new BarEntry(21, 714); // Jan
        valueSet.add(v2e2);
        BarEntry v2e3 = new BarEntry(22, 245); // Jan
        valueSet.add(v2e3);
        BarEntry v2e4 = new BarEntry(23, 332); // Jan
        valueSet.add(v2e4);
        BarEntry v2e5 = new BarEntry(24, 41); // Jan
        valueSet.add(v2e5);
        BarDataSet barDataSet = new BarDataSet(valueSet, "Transport");
//        barDataSet1.setColor(Color.rgb(0, 155, 0));
        barDataSet.setDrawValues(true);
        barDataSet.setValueTextSize(10);
        barDataSet.setValueTextColor(textColor);

        dataSets = new ArrayList<>();
        dataSets.add(barDataSet);
        return dataSets;
    }
}
