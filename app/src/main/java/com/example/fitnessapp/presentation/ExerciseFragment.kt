package com.example.fitnessapp.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.FragmentExerciseBinding
import com.example.fitnessapp.domain.User
import com.example.fitnessapp.domain.WorkoutDay
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ExerciseFragment : Fragment() {
    private var _binding: FragmentExerciseBinding? = null
    private val binding: FragmentExerciseBinding
        get() = _binding ?: throw RuntimeException("FragmentExerciseBinding = null")

    private var currentExerciseIndex = 0
    private lateinit var workoutDay: WorkoutDay

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        workoutDay = parseArgs() ?: return
        if (workoutDay.restDay == "false") {
            updateExerciseUI()
            setupClickListener()
        } else if (workoutDay.restDay == "true") {
            restDay()
        }
    }

    private fun setupClickListener() {
        binding.btnNextExercise.setOnClickListener {
            if (currentExerciseIndex < workoutDay.exercises.size - 1) {
                currentExerciseIndex++
                updateExerciseUI()
            } else {
                updateDayNumInFirebase()
            }
        }
    }

    private fun restDay() {
        Glide.with(binding.ivExerciseGif.context)
            .load(R.drawable.img_finish_train)
            .into(binding.ivExerciseGif)
        binding.tvExerciseName.text = String.format("Сегодня Вы можете отдохнуть!")
        binding.tvDescriptionTitle.text = ""
        binding.tvExerciseDescription.text = ""
        binding.btnNextExercise.text = String.format("Завершить")
        binding.btnNextExercise.setOnClickListener {
            updateDayNumInFirebase()
        }
    }

    private fun updateExerciseUI() {
        val exercise = workoutDay.exercises[currentExerciseIndex].exercisesItem
        binding.tvExerciseName.text = exercise.name
        binding.tvExerciseDescription.text = exercise.instructions.joinToString(" ")
        Glide.with(binding.ivExerciseGif.context)
            .load(exercise.gifUrl)
            .into(binding.ivExerciseGif)
        if (currentExerciseIndex >= workoutDay.exercises.size - 1) {
            binding.btnNextExercise.text = String.format("Завершить")
        }
    }

    private fun updateDayNumInFirebase() {
        binding.pbUpdatingData.visibility = View.VISIBLE
        val user = FirebaseAuth.getInstance().currentUser
        val usersReference = FirebaseDatabase.getInstance().getReference("Users")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val userId = user?.uid ?: throw IllegalStateException("User is not authenticated.")
                val currentUserReference = usersReference.child(userId)

                val dataSnapshot = currentUserReference.get().await()
                val currentUser = dataSnapshot.getValue(User::class.java)
                val planId = workoutDay.planId

                Log.d("TestUpdateDay", currentUser.toString())

                if (currentUser != null) {
                    val updatedPlans = currentUser.listOfPlans?.map { plan ->
                        if (plan.id == planId) {
                            val newDayNum = if (plan.dayNum >= 30) 0 else plan.dayNum + 1
                            plan.copy(dayNum = newDayNum)
                        } else {
                            plan
                        }
                    }

                    if (updatedPlans != null) {
                        val updatedUser = currentUser.copy(listOfPlans = updatedPlans)
                        currentUserReference.setValue(updatedUser).await()
                        withContext(Dispatchers.Main) {
                            binding.pbUpdatingData.visibility = View.GONE
                            Log.d(
                                "com.example.fitnessapp.presentation.ExerciseFragment",
                                "DayNum updated successfully."
                            )
                            launchHomePageFragment()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.pbUpdatingData.visibility = View.GONE
                    Log.e(
                        "com.example.fitnessapp.presentation.ExerciseFragment",
                        "Failed to update DayNum.",
                        e
                    )
                }
            }
        }
    }

    private fun parseArgs(): WorkoutDay? {
        return requireArguments().getParcelable(EXTRA_WORKOUT_DAY)
    }

    private fun launchHomePageFragment() {
        val newFragment = HomePageFragment.newInstance()
        (activity as? MainActivity)?.loadFragment(newFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val EXTRA_WORKOUT_DAY = "workoutDay"

        fun newInstance(workoutDay: WorkoutDay): Fragment {
            return ExerciseFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(EXTRA_WORKOUT_DAY, workoutDay)
                }
            }
        }
    }
}
