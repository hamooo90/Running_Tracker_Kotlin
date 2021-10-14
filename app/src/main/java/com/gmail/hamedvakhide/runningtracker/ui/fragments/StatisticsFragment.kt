package com.gmail.hamedvakhide.runningtracker.ui.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.gmail.hamedvakhide.runningtracker.R
import com.gmail.hamedvakhide.runningtracker.databinding.FragmentStatisticsBinding
import com.gmail.hamedvakhide.runningtracker.util.TrackingUtil
import com.gmail.hamedvakhide.runningtracker.viewmodel.StatisticViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics){

    private var _binding : FragmentStatisticsBinding? = null
    private val viewModel : StatisticViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentStatisticsBinding.bind(view)
        _binding = binding

        subscribeObserver()
        setupChart(binding.barChart)
    }

    private fun subscribeObserver() {
        viewModel.totalRunDistance.observe(viewLifecycleOwner,{
            if (it!=null) {
                _binding?.tvTotalDistance?.text = "${(it.toFloat())/1000}km"
            }
        })
        viewModel.totalAvgSpeed.observe(viewLifecycleOwner,{
            if (it!=null) {
                _binding?.tvAverageSpeed?.text = "${((it * 10).toInt()).toFloat() / 10}km/h"
            }
        })
        viewModel.totalCalories.observe(viewLifecycleOwner,{
            if (it!=null) {
                _binding?.tvTotalCalories?.text = "${it}kcal"
            }
        })
        viewModel.totalRunTimeMillis.observe(viewLifecycleOwner,{
            if (it!=null) {
                _binding?.tvTotalTime?.text = TrackingUtil.getFormattedTime(it)
            }
        })

        viewModel.runListByDate.observe(viewLifecycleOwner,{
            if(it!=null){
                val allAvgSpeeds = it.indices.map { i -> BarEntry(i.toFloat(),(it[i].distanceMeter/1000f)) }
                val barDataSet = BarDataSet(allAvgSpeeds,"Distance over time").apply {
                    valueTextColor = Color.WHITE
                    color = ContextCompat.getColor(requireContext(),R.color.colorAccent)
                }
                _binding?.barChart?.data = BarData(barDataSet)

            }
        })
    }
    private fun setupChart(chart : BarChart){
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        chart.axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        chart.axisRight.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        chart.apply {
            description.text = "Distance over time"
            legend.isEnabled = false
        }


    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }


}