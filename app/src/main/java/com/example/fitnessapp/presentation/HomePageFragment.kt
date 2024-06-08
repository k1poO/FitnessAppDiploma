package com.example.fitnessapp.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.FragmentHomePageBinding
import com.example.fitnessapp.domain.User
import com.example.fitnessapp.domain.WorkoutPlan
import com.example.fitnessapp.presentation.adapters.RecWorkoutPlansAdapter
import com.example.fitnessapp.presentation.adapters.UserWorkoutPlansAdapter
import com.example.fitnessapp.presentation.viewModels.HomePageViewModel

class HomePageFragment : Fragment() {

    private var _binding: FragmentHomePageBinding? = null
    private val binding: FragmentHomePageBinding
        get() = _binding ?: throw RuntimeException("FragmentHomePageBinding = null")

    private lateinit var viewModel: HomePageViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[HomePageViewModel::class.java]

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                updateUIWithUserData(it)
            }
        }

        viewModel.workoutPlans.observe(viewLifecycleOwner) {
            Log.d("WORKOUT PLANS", it.toString())
            setupRecyclers(it)
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                showError(it)
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.pbLoadingData.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (!isLoading) {
                // Скрыть прогресс-бар и показать список планов тренировок
                binding.rvRecommendedWorkout.visibility = View.VISIBLE
            }
        }
    }

    private fun setupRecyclers(recommendedList: List<WorkoutPlan>) {
        val recAdapter = RecWorkoutPlansAdapter()
        val userAdapter = UserWorkoutPlansAdapter()

        val layoutManager = GridLayoutManager(requireContext(), 1)
        binding.rvRecommendedWorkout.layoutManager = layoutManager

        binding.apply {
            userAdapter.onWorkoutClickListener =
                object : UserWorkoutPlansAdapter.OnWorkoutClickListener {
                    override fun onWorkoutClick(workoutPlan: WorkoutPlan) {
                        launchWorkoutPlanFragment(workoutPlan, false)
                    }
                }
            rvUserWorkout.layoutManager = GridLayoutManager(requireContext(), 1)
            rvUserWorkout.adapter = userAdapter
        }
        userAdapter.getDataFromFirebase()

        recAdapter.submitList(recommendedList)
        recAdapter.onButtonClickListener = object : RecWorkoutPlansAdapter.OnButtonClickListener {
            override fun onButtonClickListener(workoutPlan: WorkoutPlan) {
                launchWorkoutPlanFragment(workoutPlan, true)
            }
        }
        binding.rvRecommendedWorkout.adapter = recAdapter
    }


    private fun launchWorkoutPlanFragment(workoutPlan: WorkoutPlan, isRecRecycler: Boolean) {
        val newFragment = WorkoutPlanFragment.newInstance(workoutPlan, isRecRecycler)
        (activity as? MainActivity)?.loadFragmentWithBackStack(newFragment)
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateUIWithUserData(user: User) {
        binding.tvWelcomeBackUser.text = getString(R.string.welcome_back_user, user.name)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        fun newInstance(): HomePageFragment {
            return HomePageFragment()
        }
    }

}