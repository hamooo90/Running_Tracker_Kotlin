package com.gmail.hamedvakhide.runningtracker.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query("SELECT * FROM run_table ORDER BY distanceMeter DESC")
    fun getAllRunByDistance() : LiveData<List<Run>>

    @Query("SELECT * FROM run_table ORDER BY timeStartMillis DESC")
    fun getAllRunByDate() : LiveData<List<Run>>

    @Query("SELECT * FROM run_table ORDER BY avgSpeedKMH DESC")
    fun getAllRunBySpeed() : LiveData<List<Run>>

    @Query("SELECT * FROM run_table ORDER BY timeRunMillis DESC")
    fun getAllRunByDuration() : LiveData<List<Run>>

    @Query("SELECT * FROM run_table ORDER BY calories DESC")
    fun getAllRunByCalories() : LiveData<List<Run>>

    @Query("SELECT SUM(timeRunMillis) FROM run_table")
    fun getTotalRunTimeMillis() : LiveData<Long>

    @Query("SELECT SUM(calories) FROM run_table")
    fun getTotalCalories() : LiveData<Int>

    @Query("SELECT SUM(distanceMeter) FROM run_table")
    fun getTotalRunDistance() : LiveData<Int>

    @Query("SELECT AVG(avgSpeedKMH) FROM run_table")
    fun getTotalAverageSpeed() : LiveData<Float>




}