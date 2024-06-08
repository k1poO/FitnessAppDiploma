package com.example.fitnessapp.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.fitnessapp.databinding.FragmentWorkoutPlanBinding
import com.example.fitnessapp.domain.ExerciseWithReps
import com.example.fitnessapp.domain.User
import com.example.fitnessapp.domain.WorkoutDay
import com.example.fitnessapp.domain.WorkoutPlan
import com.example.fitnessapp.presentation.adapters.ExercisesAdapter
import com.example.fitnessapp.presentation.viewModels.WorkoutViewModel
import android.util.Log
import androidx.activity.OnBackPressedCallback
import android.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class WorkoutPlanFragment : Fragment() {

    private var _binding: FragmentWorkoutPlanBinding? = null
    private val binding: FragmentWorkoutPlanBinding
        get() = _binding ?: throw RuntimeException("FragmentWorkoutPlanBinding = null")

    private lateinit var viewModel: WorkoutViewModel
    private lateinit var currentUser: User

    private var isAddedFromRecRecycler = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[WorkoutViewModel::class.java]
        observeViewModel()
        val workoutPlan = parseWorkoutPlan()
        binding.btnRestartPlan.visibility = View.GONE
        isAddedFromRecRecycler = parseRecycler()
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                launchHomePageFragment()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        if (workoutPlan != null) {
            val dayExercises = workoutPlan.workoutDay.filter { it.day == workoutPlan.dayNum }
            if (dayExercises.isNotEmpty()) {
                val exercises = dayExercises[0].exercises
                val workoutDay = dayExercises[0]

                val listOfWorkoutDays = workoutPlan.workoutDay.toList()
                val lastDays = listOfWorkoutDays.size - workoutPlan.dayNum + 1
                val numOfEx = workoutPlan.workoutDay.filter { workoutPlan.dayNum == it.day }
                    .map { it.exercises.size }
                binding.tvNumberExercises.text =
                    String.format("${numOfEx[0]} exercises | $lastDays days")
                binding.tvWorkoutPlanName.text = String.format("${workoutPlan.bodyPart} workout")
                binding.btnStartTrain.setOnClickListener {
                    viewModel.addWorkoutPlanToUser(workoutPlan, isAddedFromRecRecycler)
                    launchExerciseFragment(workoutDay)
                }
                if (!isAddedFromRecRecycler) {
                    binding.btnRestartPlan.visibility = View.VISIBLE
                    binding.btnRestartPlan.setOnClickListener {
                        showRestartOrDeleteDialog(workoutPlan)
                    }
                }
                setupRecWorkout(exercises)
            } else {
                Log.e("WorkoutPlanFragment", "No exercises found for the specified day")
            }
        } else {
            Log.e("WorkoutPlanFragment", "WorkoutPlan is null")
        }
    }

    private fun showRestartOrDeleteDialog(workoutPlan: WorkoutPlan) {
        AlertDialog.Builder(requireContext())
            .setTitle("Выберите действие")
            .setMessage("Начать заново или удалить план?")
            .setPositiveButton("Начать заново") { _, _ ->
                restartPlan(workoutPlan)
            }
            .setNegativeButton("Удалить") { _, _ ->
                deletePlan(workoutPlan)
            }
            .create()
            .show()
    }

    private fun restartPlan(workoutPlan: WorkoutPlan) {
        val user = FirebaseAuth.getInstance().currentUser
        val usersReference = FirebaseDatabase.getInstance().getReference("Users")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val userId = user?.uid ?: throw IllegalStateException("User is not authenticated.")
                val currentUserReference = usersReference.child(userId)

                val dataSnapshot = currentUserReference.get().await()
                val currentUser = dataSnapshot.getValue(User::class.java)

                if (currentUser != null) {
                    val updatedPlans = currentUser.listOfPlans?.map { plan ->
                        if (plan.id == workoutPlan.id) {
                            plan.copy(dayNum = 1)
                        } else {
                            plan
                        }
                    }

                    if (updatedPlans != null) {
                        val updatedUser = currentUser.copy(listOfPlans = updatedPlans)
                        currentUserReference.setValue(updatedUser).await()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "План перезапущен", Toast.LENGTH_SHORT)
                                .show()
                            launchHomePageFragment()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Не удалось перезапустить план",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun deletePlan(workoutPlan: WorkoutPlan) {
        val user = FirebaseAuth.getInstance().currentUser
        val usersReference = FirebaseDatabase.getInstance().getReference("Users")

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val userId = user?.uid ?: throw IllegalStateException("User is not authenticated.")
                val currentUserReference = usersReference.child(userId)

                val dataSnapshot = currentUserReference.get().await()
                val currentUser = dataSnapshot.getValue(User::class.java)

                if (currentUser != null) {
                    val updatedPlans =
                        currentUser.listOfPlans?.filterNot { it.id == workoutPlan.id }

                    if (updatedPlans != null) {
                        val updatedUser = currentUser.copy(listOfPlans = updatedPlans)
                        currentUserReference.setValue(updatedUser).await()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "План удален", Toast.LENGTH_SHORT)
                                .show()
                            launchHomePageFragment()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Не удалось удалить план", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                currentUser = user
                Log.d("WorkoutPlanFragment", "User observed: ${user.name}")
            } ?: run {
                Log.e("WorkoutPlanFragment", "User is null")
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                Log.e("WorkoutPlanFragment", "Error observed: $errorMessage")
            }
        }
    }

    private fun parseWorkoutPlan(): WorkoutPlan? {
        return requireArguments().getParcelable(EXTRA_WORKOUT_PLAN)
    }

    private fun parseRecycler(): Boolean {
        return requireArguments().getBoolean(EXTRA_IS_ADDED_FROM_RECYCLER)
    }

    private fun setupRecWorkout(list: List<ExerciseWithReps>) {
        val adapter = ExercisesAdapter()
        val layoutManager = GridLayoutManager(requireContext(), 1)
        binding.rvExercises.layoutManager = layoutManager
        binding.rvExercises.adapter = adapter
        binding.apply {
            adapter.submitList(list)
        }
    }

    private fun launchExerciseFragment(workoutDay: WorkoutDay) {
        val newFragment = ExerciseFragment.newInstance(workoutDay)
        (activity as? MainActivity)?.loadFragmentWithBackStack(newFragment)
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

        private const val EXTRA_WORKOUT_PLAN = "workoutPlan"
        private const val EXTRA_IS_ADDED_FROM_RECYCLER = "isUserRecycler"

        fun newInstance(workoutPlan: WorkoutPlan, whatRecycler: Boolean): Fragment {
            return WorkoutPlanFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(EXTRA_WORKOUT_PLAN, workoutPlan)
                    putBoolean(EXTRA_IS_ADDED_FROM_RECYCLER, whatRecycler)
                }
            }
        }
    }
}
