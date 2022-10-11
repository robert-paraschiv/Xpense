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
import com.github.mikephil.charting.utils.ColorTemplate;
import com.rokudo.xpense.models.Transaction;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PieChartUtils {
    public static ArrayList<Integer> PIE_COLORS;

    static {
        PIE_COLORS = new ArrayList<>();
        for (int color : ColorTemplate.VORDIPLOM_COLORS) {
            PIE_COLORS.add(color);
        }
        for (int color : ColorTemplate.MATERIAL_COLORS) {
            PIE_COLORS.add(color);
        }
        for (int color : ColorTemplate.COLORFUL_COLORS) {
            PIE_COLORS.add(color);
        }
    }

    public static void setupPieChart(PieChart pieChart, int textColor) {
        pieChart.setDrawHoleEnabled(true);
        pieChart.setTouchEnabled(true);
        pieChart.setUsePercentValues(true);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setEntryLabelTextSize(10f);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterTextSize(11f);
        pieChart.setHoleRadius(48f);
        pieChart.setCenterTextColor(textColor);
        pieChart.setHoleColor(Color.TRANSPARENT);
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
        pieChart.setTransparentCircleRadius(52f);
    }


    public static void updatePieChartData(PieChart pieChart, String currency,
                                          List<Transaction> transactionList, boolean isCalledFromHome) {
        if (transactionList == null) {
            return;
        }
        Map<String, Double> categories = new HashMap<>();
        Double sum = 0.0;
        for (Transaction transaction : transactionList) {
            if (transaction.getType().equals(Transaction.INCOME_TYPE)) {
                continue;
            }
            if (categories.containsKey(transaction.getCategory())) {
                Double amount = categories.getOrDefault(transaction.getCategory(), 0.0);
                categories.put(transaction.getCategory(), amount == null ? 0.0f : amount + transaction.getAmount());
            } else {
                categories.put(transaction.getCategory(), transaction.getAmount());
            }
            sum += transaction.getAmount();
        }

        ArrayList<PieEntry> entries = getPieEntries(categories, sum);

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(PIE_COLORS);
        if (!isCalledFromHome) {
            dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            dataSet.setValueLineColor(new TextView(pieChart.getContext()).getCurrentTextColor());
            dataSet.setValueLinePart1OffsetPercentage(90f);
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
        categories.forEach((key, value) -> entries.add(new PieEntry(getPercentageOfCategory(value, sum), key)));
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
