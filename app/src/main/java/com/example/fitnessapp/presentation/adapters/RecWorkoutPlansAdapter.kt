package com.example.fitnessapp.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.ItemRecommendedWorkoutBinding
import com.example.fitnessapp.domain.WorkoutPlan

class RecWorkoutPlansAdapter :
    ListAdapter<WorkoutPlan, RecWorkoutPlansAdapter.Holder>(Comparator()) {

    var onButtonClickListener: OnButtonClickListener? = null

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ItemRecommendedWorkoutBinding.bind(view)

        fun bind(workoutPlan: WorkoutPlan, onViewMoreClick: (WorkoutPlan) -> Unit) = with(binding) {
            tvWorkoutName.text = String.format("${workoutPlan.bodyPart} workout")
            btnViewMore.setOnClickListener {
                onViewMoreClick(workoutPlan)
            }
        }
    }

    class Comparator : DiffUtil.ItemCallback<WorkoutPlan>() {
        override fun areItemsTheSame(oldItem: WorkoutPlan, newItem: WorkoutPlan): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: WorkoutPlan, newItem: WorkoutPlan): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_recommended_workout,
                parent,
                false
            )
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position)) { workoutPlan ->
            onButtonClickListener?.onButtonClickListener(workoutPlan)
        }
    }

    interface OnButtonClickListener {
        fun onButtonClickListener(workoutPlan: WorkoutPlan)
    }
}
