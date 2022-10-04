package com.rokudo.xpense.utils;

import android.annotation.SuppressLint;
import android.util.Log;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.rokudo.xpense.models.TransEntry;
import com.rokudo.xpense.models.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class BarChartUtils {
    private static final String TAG = "BarChartUtils";

    @SuppressLint("SimpleDateFormat")
    static SimpleDateFormat dayOfMonthFormat = new SimpleDateFormat("dd");

    public static void setupBarChart(BarChart barChart, int textColor,boolean isCalledFromHome) {
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

        if (isCalledFromHome){
            barChart.animateY(2000);
            barChart.invalidate();
        }

        barChart.getDescription().setEnabled(false);
    }

    public static void updateBarchartData(BarChart barChart, List<Transaction> transactionList, int textColor, boolean isCalledFromHome) {
        barChart.invalidate();

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        ArrayList<BarEntry> valueSet = new ArrayList<>();

        List<TransEntry> transEntryArrayList = new ArrayList<>();

        int todayDayOfMonth = Integer.parseInt(dayOfMonthFormat.format(new Date()));
        for (Transaction transaction : transactionList) {
            if (todayDayOfMonth - Integer.parseInt(dayOfMonthFormat.format(transaction.getDate())) > 5) {
                continue;
            }
            TransEntry transEntry = new TransEntry(dayOfMonthFormat.format(transaction.getDate()), transaction.getDate(), Float.parseFloat(transaction.getAmount().toString()));
            if (transEntryArrayList.contains(transEntry)) {
                int index = transEntryArrayList.indexOf(transEntry);
                transEntryArrayList.get(index).setAmount((float) (transEntryArrayList.get(index).getAmount() + transaction.getAmount()));
            } else {
                transEntryArrayList.add(transEntry);
            }
        }

        transEntryArrayList.sort(Comparator.comparingLong(transEntry -> transEntry.getDate().getTime()));
        for (int i = 0; i < transEntryArrayList.size(); i++) {
            BarEntry barEntry = new BarEntry(i, transEntryArrayList.get(i).getAmount());
            valueSet.add(barEntry);
        }

        barChart.getXAxis().setGranularity(1f);

        //Set X Axis Labels from transaction list
        try {
            barChart.getXAxis().setValueFormatter((value, axis) -> {
                if ((int) value >= transEntryArrayList.size()) {
                    return "";
                } else {
                    return transEntryArrayList.get((int) value).getDay();
                }
            });
        } catch (IndexOutOfBoundsException exception) {
            Log.e(TAG, "updateBarchartData: ", exception);
        }


        BarDataSet barDataSet = new BarDataSet(valueSet, "");
        barDataSet.setDrawValues(true);
        barDataSet.setValueTextSize(10);
        barDataSet.setValueTextColor(textColor);
        barDataSet.setValueTextColor(textColor);

        dataSets.add(barDataSet);
        barChart.setData(new BarData(dataSets));
        if (isCalledFromHome)
            barChart.animate();
    }

}
