package com.example.fitnessapp.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.ItemAddExerciseBinding
import com.example.fitnessapp.domain.ExercisesItem

class SearchAdapter :
    ListAdapter<ExercisesItem, SearchAdapter.Holder>(Comparator()) {

    var onButtonClickListener: OnButtonClickListener? = null
    var onItemClickListener: OnItemClickListener? = null

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ItemAddExerciseBinding.bind(view)

        fun bind(exercisesItem: ExercisesItem, onButtonClick: (ExercisesItem) -> Unit, onItemClick: (ExercisesItem) -> Unit) =
            with(binding) {
                tvExerciseName.text = exercisesItem.name
                btnAddExercise.setOnClickListener {
                    onButtonClick(exercisesItem)
                }
                root.setOnClickListener {
                    onItemClick(exercisesItem)
                }
            }
    }

    class Comparator : DiffUtil.ItemCallback<ExercisesItem>() {
        override fun areItemsTheSame(oldItem: ExercisesItem, newItem: ExercisesItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ExercisesItem, newItem: ExercisesItem): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_add_exercise, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position), { exerciseItem ->
            onButtonClickListener?.onButtonClick(exerciseItem)
        }, { exerciseItem ->
            onItemClickListener?.onItemClick(exerciseItem)
        })
    }

    interface OnButtonClickListener {
        fun onButtonClick(exercisesItem: ExercisesItem)
    }

    interface OnItemClickListener {
        fun onItemClick(exercisesItem: ExercisesItem)
    }
}
