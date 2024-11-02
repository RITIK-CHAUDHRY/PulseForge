
package com.pulseforge.ui

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProgressChart(
    data: List<Float>,
    dates: List<Date>,
    label: String
) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
                
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    private val dateFormatter = SimpleDateFormat("MM/dd", Locale.getDefault())
                    override fun getFormattedValue(value: Float): String {
                        return if (value.toInt() in dates.indices) {
                            dateFormatter.format(dates[value.toInt()])
                        } else {
                            ""
                        }
                    }
                }
                
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            val entries = data.mapIndexed { index, value ->
                Entry(index.toFloat(), value)
            }
            
            val dataSet = LineDataSet(entries, label).apply {
                color = Color.BLUE
                setCircleColor(Color.BLUE)
                lineWidth = 2f
                circleRadius = 4f
                setDrawCircleHole(false)
                valueTextSize = 10f
                setDrawFilled(true)
                fillColor = Color.BLUE
                fillAlpha = 30
            }
            
            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}
