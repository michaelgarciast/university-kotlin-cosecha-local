package com.garcia.cosechalocal.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.garcia.cosechalocal.R
import com.garcia.cosechalocal.data.model.SalesPeriod
import com.garcia.cosechalocal.databinding.FragmentSalesReportBinding
import com.garcia.cosechalocal.utils.FormatUtils
import com.google.android.material.snackbar.Snackbar

class SalesReportFragment : Fragment() {

    private var _binding: FragmentSalesReportBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSalesReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = TopSellerAdapter()
        binding.rvTopSellers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTopSellers.adapter = adapter

        binding.chipGroupPeriod.check(
            when (viewModel.period) {
                SalesPeriod.HOY -> R.id.chipPeriodToday
                SalesPeriod.SIETE_DIAS -> R.id.chipPeriodWeek
                SalesPeriod.MES -> R.id.chipPeriodMonth
            }
        )
        binding.chipGroupPeriod.setOnCheckedStateChangeListener { _, ids ->
            viewModel.setPeriod(
                when (ids.firstOrNull()) {
                    R.id.chipPeriodToday -> SalesPeriod.HOY
                    R.id.chipPeriodWeek -> SalesPeriod.SIETE_DIAS
                    else -> SalesPeriod.MES
                }
            )
        }

        viewModel.report.observe(viewLifecycleOwner) { report ->
            val (salesLabel, chartTitle) = when (viewModel.period) {
                SalesPeriod.HOY -> R.string.report_sales_today to R.string.report_chart_today
                SalesPeriod.SIETE_DIAS -> R.string.report_sales_week to R.string.report_chart_week
                SalesPeriod.MES -> R.string.report_sales_month to R.string.report_chart_month
            }
            binding.tvSalesLabel.setText(salesLabel)
            binding.tvChartTitle.setText(chartTitle)
            binding.tvSalesValue.text = FormatUtils.formatMillions(report.totalSales)
            binding.tvCommissionValue.text = FormatUtils.formatMillions(report.commission)
            binding.chartSales.setData(report.bars, report.bars.map { FormatUtils.formatMillions(it.value).removePrefix("$").removeSuffix(" M") })
            adapter.submitList(report.topSellers)
        }

        binding.btnExportReport.setOnClickListener {
            Snackbar.make(binding.root, R.string.report_exported_simulated, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
