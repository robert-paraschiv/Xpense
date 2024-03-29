package com.rokudo.xpense.utils;

import android.graphics.Color;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.rokudo.xpense.models.ExpenseCategory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

public class PieChartUtils {

    public static void setupPieChart(PieChart pieChart, int textColor, boolean isCalledFromHome) {
        pieChart.setTouchEnabled(!isCalledFromHome);
        pieChart.setUsePercentValues(true);
        pieChart.setHighlightPerTapEnabled(!isCalledFromHome);

        pieChart.setEntryLabelTextSize(10f);
        pieChart.setEntryLabelColor(Color.BLACK);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(isCalledFromHome ? 48f : 34f);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setTransparentCircleRadius(isCalledFromHome ? 52f : 38f);
        pieChart.setDrawCenterText(isCalledFromHome);
        pieChart.setCenterTextSize(11f);
        pieChart.setCenterTextColor(textColor);

        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setMinAngleForSlices(20f);

        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setXEntrySpace(4f);
        l.setYEntrySpace(0f);
        l.setWordWrapEnabled(true);
        l.setTextColor(textColor);
    }


    public static void updatePieChartData(PieChart pieChart, String currency,
                                          Map<String, Double> categories, Double sum, boolean isCalledFromHome) {


        ArrayList<PieEntry> entries = getPieEntries(categories, sum);
        PieDataSet dataSet = new PieDataSet(entries, "");

        //Get colors ordered to map label colors from Category Utils
        ArrayList<Integer> pieColors = new ArrayList<>();
        entries.forEach(value ->
                pieColors.add(CategoriesUtil.expenseCategoryList.get(
                                CategoriesUtil.expenseCategoryList.indexOf(new ExpenseCategory(value.getLabel())))
                        .getColor()));

        dataSet.setColors(pieColors);

        if (!isCalledFromHome) {
            dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            dataSet.setValueLineColor(new TextView(pieChart.getContext()).getCurrentTextColor());
            dataSet.setValueLinePart1OffsetPercentage(80f);
            dataSet.setValueLinePart1Length(0.5f);
        }

        PieData data = new PieData(dataSet);
        data.setDrawValues(!isCalledFromHome);
        data.setValueFormatter(new PercentFormatter(pieChart));
        data.setValueTextSize(12f);
        data.setValueTextColor(new TextView(pieChart.getContext()).getCurrentTextColor());


        pieChart.setCenterText(getRoundedValue(sum) + " " + currency);
        pieChart.setData(data);
        pieChart.invalidate();

        if (isCalledFromHome)
            pieChart.animateY(1400);
    }

    @NonNull
    private static ArrayList<PieEntry> getPieEntries(Map<String, Double> categories, Double sum) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        categories.forEach((key, value) -> {
            if (key.equals("Income") || value == 0) {
                return;
            }
            entries.add(new PieEntry(getPercentageOfCategory(value, sum), key));
        });
        entries.sort(Comparator.comparingDouble(PieEntry::getValue).reversed());
        return entries;
    }


    private static float getPercentageOfCategory(Double value, Double finalSum) {
        return (float) ((value * 100) / finalSum);
    }

    private static Double getRoundedValue(Double amount) {
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return Double.parseDouble(decimalFormat.format(amount));
    }
}
