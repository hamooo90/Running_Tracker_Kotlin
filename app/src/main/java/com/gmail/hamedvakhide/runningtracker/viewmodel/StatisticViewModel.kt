package com.gmail.hamedvakhide.runningtracker.viewmodel

import androidx.lifecycle.ViewModel
import com.gmail.hamedvakhide.runningtracker.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel(){

    val totalRunTimeMillis = repository.getTotalRunTimeMillis()
    val totalCalories = repository.getTotalCalories()
    val totalRunDistance = repository.getTotalRunDistance()
    val totalAvgSpeed = repository.getTotalAverageSpeed()

     val runListByDate = repository.getAllRunByDate()
    
}