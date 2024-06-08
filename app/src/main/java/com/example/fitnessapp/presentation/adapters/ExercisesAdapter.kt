package com.example.fitnessapp.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.ItemExercisesRecyclerBinding
import com.example.fitnessapp.domain.ExerciseWithReps

class ExercisesAdapter :
    ListAdapter<ExerciseWithReps, ExercisesAdapter.Holder>(Comparator()) {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ItemExercisesRecyclerBinding.bind(view)

        fun bind(exerciseWithReps: ExerciseWithReps) = with(binding) {
            val exerciseGif = exerciseWithReps.exercisesItem.gifUrl
            val exerciseReps = exerciseWithReps.repetitions
            val exerciseName = exerciseWithReps.exercisesItem.name

            tvExerciseName.text = exerciseName
            tvExerciseReps.text = String.format("$exerciseReps repetitions")
            Glide.with(ivExerciseGif.context)
                .load(exerciseGif)
                .into(ivExerciseGif)
        }
    }

    class Comparator : DiffUtil.ItemCallback<ExerciseWithReps>() {
        override fun areItemsTheSame(oldItem: ExerciseWithReps, newItem: ExerciseWithReps): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ExerciseWithReps, newItem: ExerciseWithReps): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_exercises_recycler,
                parent,
                false
            )
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }
}