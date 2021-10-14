package com.gmail.hamedvakhide.runningtracker.repository

import com.gmail.hamedvakhide.runningtracker.data.Run
import com.gmail.hamedvakhide.runningtracker.data.RunDao
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val dao: RunDao
) {

    suspend fun insertRun(run: Run) = dao.insertRun(run)

    suspend fun deleteRun(run: Run) = dao.deleteRun(run)

    fun getAllRunByDistance() = dao.getAllRunByDistance()

    fun getAllRunByDate() = dao.getAllRunByDate()

    fun getAllRunBySpeed() = dao.getAllRunBySpeed()

    fun getAllRunByDuration() = dao.getAllRunByDuration()

    fun getAllRunByCalories() = dao.getAllRunByCalories()

    fun getTotalRunTimeMillis() = dao.getTotalRunTimeMillis()

    fun getTotalCalories() = dao.getTotalCalories()

    fun getTotalRunDistance() = dao.getTotalRunDistance()

    fun getTotalAverageSpeed() = dao.getTotalAverageSpeed()
}