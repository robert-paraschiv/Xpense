package com.rokudo.xpense.utils

import android.graphics.Color
import android.widget.TextView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.rokudo.xpense.models.ExpenseCategory
import java.text.DecimalFormat

object PieChartUtils {

    fun setupPieChart(pieChart: PieChart, textColor: Int, isCalledFromHome: Boolean) {
        pieChart.setTouchEnabled(!isCalledFromHome)
        pieChart.setUsePercentValues(true)
        pieChart.isHighlightPerTapEnabled = !isCalledFromHome
        pieChart.setEntryLabelTextSize(10f)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = if (isCalledFromHome) 48f else 34f
        pieChart.setHoleColor(Color.TRANSPARENT)
        pieChart.transparentCircleRadius = if (isCalledFromHome) 52f else 38f
        pieChart.setDrawCenterText(isCalledFromHome)
        pieChart.setCenterTextSize(11f)
        pieChart.setCenterTextColor(textColor)
        pieChart.description.isEnabled = false
        pieChart.setDrawEntryLabels(false)
        pieChart.minAngleForSlices = 20f

        val l = pieChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        l.xEntrySpace = 4f
        l.yEntrySpace = 0f
        l.isWordWrapEnabled = true
        l.textColor = textColor
    }

    fun updatePieChartData(
        pieChart: PieChart, currency: String,
        categories: Map<String, Double>?, sum: Double?, isCalledFromHome: Boolean
    ) {
        if (categories.isNullOrEmpty() || sum == null || sum == 0.0) {
            pieChart.clear()
            pieChart.invalidate()
            return
        }

        val entries = getPieEntries(categories, sum)
        val dataSet = PieDataSet(entries, "")

        val pieColors = entries.map { entry ->
            CategoriesUtil.expenseCategoryList
                .find { it.name == entry.label }
                ?.color ?: Color.GRAY
        }
        dataSet.colors = pieColors

        if (!isCalledFromHome) {
            dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
            dataSet.valueLineColor = TextView(pieChart.context).currentTextColor
            dataSet.valueLinePart1OffsetPercentage = 80f
            dataSet.valueLinePart1Length = 0.5f
        }

        val data = PieData(dataSet)
        data.setDrawValues(!isCalledFromHome)
        data.setValueFormatter(PercentFormatter(pieChart))
        data.setValueTextSize(12f)
        data.setValueTextColor(TextView(pieChart.context).currentTextColor)

        pieChart.centerText = "${getRoundedValue(sum)} $currency"
        pieChart.data = data
        pieChart.invalidate()
    }

    private fun getPieEntries(categories: Map<String, Double>, sum: Double): List<PieEntry> {
        return categories
            .filter { it.key != "Income" && it.value != 0.0 }
            .map { PieEntry(getPercentageOfCategory(it.value, sum), it.key) }
            .sortedByDescending { it.value }
    }

    private fun getPercentageOfCategory(value: Double, finalSum: Double): Float {
        return ((value * 100) / finalSum).toFloat()
    }

    private fun getRoundedValue(amount: Double): Double {
        val df = DecimalFormat("0.00")
        return df.format(amount).toDouble()
    }
}

