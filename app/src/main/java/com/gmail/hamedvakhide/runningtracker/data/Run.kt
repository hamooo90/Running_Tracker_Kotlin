package com.gmail.hamedvakhide.runningtracker.data

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "run_table")
data class Run(
    var img : Bitmap? = null,

    var timeStartMillis : Long = 0L,
    var avgSpeedKMH : Float = 0f,
    var distanceMeter : Int = 0,
    var timeRunMillis : Long = 0L,
    var calories : Int = 0,

    @PrimaryKey(autoGenerate = true)
    var id : Int? = null
)
//{
//    @PrimaryKey(autoGenerate = true)
//    var id : Int? = null
//}