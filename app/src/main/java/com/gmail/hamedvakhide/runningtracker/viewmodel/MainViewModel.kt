package com.gmail.hamedvakhide.runningtracker.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gmail.hamedvakhide.runningtracker.data.Run
import com.gmail.hamedvakhide.runningtracker.repository.MainRepository
import com.gmail.hamedvakhide.runningtracker.util.SortType
import com.gmail.hamedvakhide.runningtracker.util.TrackingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MainRepository
) : ViewModel() {


    fun insertRun(run: Run) = viewModelScope.launch {
        repository.insertRun(run)
    }

    private val runListByDate = repository.getAllRunByDate()
    private val runListByDistance = repository.getAllRunByDistance()
    private val runListBySpeed = repository.getAllRunBySpeed()
    private val runListByDuration = repository.getAllRunByDuration()
    private val runListByCalories = repository.getAllRunByCalories()

    val runList = MediatorLiveData<List<Run>>()

//    val latLng = MutableLiveData<LatLng>()

    var sortType = SortType.DATE

    init {
        runList.addSource(runListByDate) { result ->
            if (sortType == SortType.DATE) {
                result?.let {
                    runList.value = it
                }
            }
        }
        runList.addSource(runListByDistance) { result ->
            if (sortType == SortType.DISTANCE) {
                result?.let {
                    runList.value = it
                }
            }
        }
        runList.addSource(runListBySpeed) { result ->
            if (sortType == SortType.SPEED) {
                result?.let {
                    runList.value = it
                }
            }
        }
        runList.addSource(runListByDuration) { result ->
            if (sortType == SortType.TIME) {
                result?.let {
                    runList.value = it
                }
            }
        }
        runList.addSource(runListByCalories) { result ->
            if (sortType == SortType.CALORIE) {
                result?.let {
                    runList.value = it
                }
            }
        }
    }

    fun sortRuns(sortType: SortType) {
        when (sortType) {
            SortType.DATE -> runListByDate.value?.let { runList.value = it }
            SortType.SPEED -> runListBySpeed.value?.let { runList.value = it }
            SortType.DISTANCE -> runListByDistance.value?.let { runList.value = it }
            SortType.CALORIE -> runListByCalories.value?.let { runList.value = it }
            SortType.TIME -> runListByDuration.value?.let { runList.value = it }
        }
        this.sortType = sortType
    }

    //    val totalRunTimeMillis = repository.getTotalRunTimeMillis()
//    val totalCalories = repository.getTotalCalories()
//    val totalRunDistance = repository.getTotalRunDistance()
//    val totalAvgSpeed = repository.getTotalAverageSpeed()

//    @SuppressLint("MissingPermission")
//    fun getStartingLocation(context: Context) {
//        if(TrackingUtil.hasLocationPermission(context)){
//            fusedLocationProviderClient.lastLocation
//                .addOnSuccessListener { location ->
//                    if (location != null) {
//                        latLng.postValue(LatLng(location.latitude,location.longitude))
//                    }
//                }
//        }
//
//    }


}