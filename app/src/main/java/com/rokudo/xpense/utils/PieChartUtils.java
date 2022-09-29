package com.rokudo.xpense.utils;

import android.graphics.Color;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.rokudo.xpense.models.Transaction;
import com.rokudo.xpense.models.Wallet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PieChartUtils {
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


    public static void updatePieChartData(PieChart pieChart, Wallet wallet, List<Transaction> transactionList) {
        if (wallet == null || transactionList == null) {
            return;
        }
        Map<String, Double> categories = new HashMap<>();
        Double sum = 0.0;
        for (Transaction transaction : transactionList) {
            if (transaction.getType().equals(Transaction.INCOME_TYPE))
                continue;
            if (categories.containsKey(transaction.getCategory())) {
                Double amount = categories.getOrDefault(transaction.getCategory(), 0.0);
                categories.put(transaction.getCategory(), amount == null ? 0.0f : amount + transaction.getAmount());
            } else {
                categories.put(transaction.getCategory(), transaction.getAmount());
            }
            sum += transaction.getAmount();
        }

        pieChart.setCenterText(wallet.getCurrency());

        ArrayList<PieEntry> entries = new ArrayList<>();
        Double finalSum = sum;
        categories.forEach((key, value) -> entries.add(new PieEntry(getPercentageOfCategory(value, finalSum), key)));
        ArrayList<Integer> colors = new ArrayList<>();
        for (int color : ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color);
        }
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setDrawValues(true);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setCenterText(sum + " " + wallet.getCurrency());
        pieChart.setData(data);
        pieChart.invalidate();

        pieChart.animateY(1400);
    }

    private static float getPercentageOfCategory(Double value, Double finalSum) {
        return (float) ((value * 100) / finalSum);
    }
}
