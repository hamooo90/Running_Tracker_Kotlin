package com.gmail.hamedvakhide.runningtracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gmail.hamedvakhide.runningtracker.R
import com.gmail.hamedvakhide.runningtracker.data.Run
import com.gmail.hamedvakhide.runningtracker.util.TrackingUtil
import com.google.android.material.textview.MaterialTextView
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter : ListAdapter<Run, RunAdapter.RunViewHolder>(COMPARATOR) {
    class RunViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val img: ImageView = itemView.findViewById(R.id.ivRunImage)
        private val date: MaterialTextView = itemView.findViewById(R.id.tvDate)
        private val time: MaterialTextView = itemView.findViewById(R.id.tvTime)
        private val distance: MaterialTextView = itemView.findViewById(R.id.tvDistance)
        private val speed: MaterialTextView = itemView.findViewById(R.id.tvAvgSpeed)
        private val calorie: MaterialTextView = itemView.findViewById(R.id.tvCalories)

        fun bind(run: Run) {
            Glide.with(img).load(run.img).into(img)
            val calender = Calendar.getInstance().apply {
                timeInMillis = run.timeStartMillis
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            date.text = dateFormat.format(calender.time)

            time.text = TrackingUtil.getFormattedTime(run.timeRunMillis)
            distance.text = "${run.distanceMeter / 1000f}km"
            speed.text = "${run.avgSpeedKMH}km/h"
            calorie.text = "${run.calories}kcal"
        }

        companion object {
            fun from(parent: ViewGroup): RunViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val itemView = inflater.inflate(R.layout.item_run, parent, false)
                return RunViewHolder(itemView)
            }
        }
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<Run>() {
            override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = getItem(position)
        run.apply {
            holder.bind(this)
        }
    }
}