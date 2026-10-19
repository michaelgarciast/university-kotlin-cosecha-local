package com.garcia.cosechalocal.ui.admin

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.model.BarEntry

class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var entries: List<BarEntry> = emptyList()
    private var valueLabels: List<String> = emptyList()

    private val textSize = resources.getDimension(R.dimen.text_size_caption)
    private val barRadius = resources.getDimension(R.dimen.chart_bar_corner)
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.primary_green)
    }
    private val peakPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.amber_accent)
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.text_secondary)
        textSize = this@BarChartView.textSize
        textAlign = Paint.Align.CENTER
    }
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.text_primary)
        textSize = this@BarChartView.textSize
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    private val rect = RectF()

    fun setData(entries: List<BarEntry>, valueLabels: List<String>) {
        this.entries = entries
        this.valueLabels = valueLabels
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (entries.isEmpty()) return
        val max = entries.maxOf { it.value }.coerceAtLeast(1)
        val peak = entries.indexOfFirst { it.value == max }
        val lineHeight = textSize * 1.6f
        val left = paddingLeft.toFloat()
        val slot = (width - paddingLeft - paddingRight) / entries.size.toFloat()
        val barWidth = slot * BAR_RATIO
        val top = paddingTop + lineHeight
        val bottom = height - paddingBottom - lineHeight
        val maxBarHeight = (bottom - top).coerceAtLeast(0f)

        entries.forEachIndexed { i, entry ->
            val cx = left + slot * i + slot / 2
            val barHeight = (maxBarHeight * entry.value / max).coerceAtLeast(barRadius)
            rect.set(cx - barWidth / 2, bottom - barHeight, cx + barWidth / 2, bottom)
            canvas.drawRoundRect(rect, barRadius, barRadius, if (i == peak) peakPaint else barPaint)
            canvas.drawText(valueLabels.getOrElse(i) { "" }, cx, bottom - barHeight - textSize * 0.4f, valuePaint)
            canvas.drawText(entry.label, cx, height - paddingBottom - textSize * 0.3f, labelPaint)
        }
    }

    private companion object {
        const val BAR_RATIO = 0.55f
    }
}
