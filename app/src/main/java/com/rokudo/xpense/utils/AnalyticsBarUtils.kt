package com.rokudo.xpense.utils

import android.annotation.SuppressLint
import android.util.Log
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.rokudo.xpense.models.TransEntry
import com.rokudo.xpense.models.Transaction
import java.text.SimpleDateFormat
import java.util.*

object AnalyticsBarUtils {
    private const val TAG = "BarChartUtils"

    @SuppressLint("SimpleDateFormat")
    private val dayOfMonthFormat = SimpleDateFormat("EEE dd")

    fun setupBarChart(barChart: BarChart, textColor: Int) {
        barChart.setPinchZoom(false)
        barChart.isDoubleTapToZoomEnabled = false
        barChart.setScaleEnabled(false)
        barChart.setTouchEnabled(true)
        barChart.setDrawBarShadow(false)
        barChart.setDrawGridBackground(false)
        barChart.legend.isEnabled = false
        barChart.setFitBars(true)
        barChart.setExtraBottomOffset(8f)

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = textColor

        barChart.axisLeft.isEnabled = false
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.xAxis.granularity = 1f
        barChart.xAxis.labelRotationAngle = -45f
        barChart.xAxis.textSize = 8f
    }

    fun setBarLabelRotation(barChart: BarChart, rotated: Boolean) {
        barChart.xAxis.labelRotationAngle = if (rotated) -45f else 0f
    }

    fun updateBarchartData(barChart: BarChart, transEntryArrayList: List<TransEntry>, textColor: Int) {
        val valueSet = transEntryArrayList.mapIndexed { i, entry ->
            BarEntry(i.toFloat(), entry.amount)
        }

        try {
            barChart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index >= transEntryArrayList.size) "" else transEntryArrayList[index].day
                }
            }
        } catch (e: IndexOutOfBoundsException) {
            Log.e(TAG, "updateBarchartData: ", e)
        }

        val barDataSet = BarDataSet(valueSet, "").apply {
            setDrawValues(true)
            valueTextSize = 10f
            valueTextColor = textColor
            color = 0xFF1B5E20.toInt()
            highLightColor = 0xFF2E7D32.toInt()
            highLightAlpha = 180
        }

        barChart.data = BarData(listOf(barDataSet))
        barChart.xAxis.labelCount = valueSet.size
        barChart.xAxis.labelRotationAngle = if (valueSet.size > 5) -45f else 0f
        barChart.invalidate()
    }

    fun getTransEntryArrayList(transactionList: List<Transaction>, isYearMode: Boolean): List<TransEntry> {
        val transEntryArrayList = mutableListOf<TransEntry>()
        for (transaction in transactionList) {
            if (transaction.type == Transaction.INCOME_TYPE) continue

            val dateString = if (isYearMode) {
                SimpleDateFormat("MMMM", Locale.getDefault()).format(transaction.date!!)
            } else {
                dayOfMonthFormat.format(transaction.date!!)
            }

            val transEntry = TransEntry(
                dateString,
                transaction.date,
                (transaction.amount ?: 0.0).toFloat()
            )

            val existingIndex = transEntryArrayList.indexOf(transEntry)
            if (existingIndex >= 0) {
                transEntryArrayList[existingIndex].amount += (transaction.amount ?: 0.0).toFloat()
            } else {
                transEntryArrayList.add(transEntry)
            }
        }
        transEntryArrayList.sortBy { it.date?.time ?: 0 }
        return transEntryArrayList
    }
}

