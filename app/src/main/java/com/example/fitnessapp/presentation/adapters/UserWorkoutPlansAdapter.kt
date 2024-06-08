package com.example.fitnessapp.presentation.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fitnessapp.databinding.ItemUpcomingWorkoutBinding
import com.example.fitnessapp.domain.User
import com.example.fitnessapp.domain.WorkoutPlan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserWorkoutPlansAdapter :
    ListAdapter<WorkoutPlan, UserWorkoutPlansAdapter.Holder>(Comparator()) {

    var callback: AdapterCallback? = null
    var onWorkoutClickListener: OnWorkoutClickListener? = null
    private var databaseReference: DatabaseReference? = null
    private var listener: ValueEventListener? = null

    init {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("Users/$userId")
        }
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        listener?.let { databaseReference?.removeEventListener(it) }
    }

    class Holder(private val binding: ItemUpcomingWorkoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(workoutPlan: WorkoutPlan) {
            binding.tvWorkoutName.text = String.format("${workoutPlan.bodyPart} workout")
            binding.tvWorkoutDay.text = String.format("${workoutPlan.dayNum} / 30")
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
        val binding = ItemUpcomingWorkoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val workoutPlan = getItem(position)
        holder.bind(workoutPlan)
        holder.itemView.setOnClickListener {
            onWorkoutClickListener?.onWorkoutClick(workoutPlan)
        }
    }

    interface OnWorkoutClickListener {
        fun onWorkoutClick(workoutPlan: WorkoutPlan)
    }

    interface AdapterCallback {
        fun onUpdateEmptyView(visible: Boolean)
    }

    fun getDataFromFirebase() {
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                user?.listOfPlans?.let {
                    submitList(it)
                    callback?.onUpdateEmptyView(user.listOfPlans.isEmpty())
                } ?: callback?.onUpdateEmptyView(true)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("UserWorkoutPlansAdapter", "Error loading data: ${error.message}")
            }
        }
        databaseReference?.addValueEventListener(listener!!)
    }
}