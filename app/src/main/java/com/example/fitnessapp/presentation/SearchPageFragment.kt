package com.example.fitnessapp.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.fitnessapp.databinding.FragmentSearchPageBinding
import com.example.fitnessapp.domain.ExerciseWithReps
import com.example.fitnessapp.domain.ExercisesItem
import com.example.fitnessapp.presentation.adapters.SearchAdapter
import com.example.fitnessapp.presentation.viewModels.SearchViewModel

class SearchPageFragment : Fragment() {

    private var _binding: FragmentSearchPageBinding? = null
    private val binding: FragmentSearchPageBinding
        get() = _binding ?: throw RuntimeException("FragmentSearchPageBinding = null")

    private lateinit var viewModel: SearchViewModel
    private val listOfExercises: MutableList<ExerciseWithReps> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[SearchViewModel::class.java]

        val exerciseSearchAdapter = SearchAdapter()
        setupRecyclers(exerciseSearchAdapter)

        binding.btnSearch.setOnClickListener {
            val bodyPart = binding.spinnerBodyPart
            val equipment = binding.spinnerTarget
            val target = binding.spinnerEquipment
//            viewModel.searchExercises(bodyPart, equipment, target)
        }

        viewModel.exercises.observe(viewLifecycleOwner, Observer { exercises ->
            exerciseSearchAdapter.submitList(exercises)
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.pbLoadingSearch.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        viewModel.selectedExercise.observe(viewLifecycleOwner, Observer { exercise ->
            exercise?.let {
                // Открытие диалогового окна с описанием упражнения
//                ExerciseDialogFragment.newInstance(exercise).show(childFragmentManager, "exerciseDialog")
//                searchViewModel.clearSelectedExercise()
            }
        })
    }

    private fun setupRecyclers(exerciseSearchAdapter: SearchAdapter) {
        val layoutManager = GridLayoutManager(requireContext(), 1)
        binding.rvExercisesSearch.layoutManager = layoutManager
        binding.rvExercisesSearch.adapter = exerciseSearchAdapter

        exerciseSearchAdapter.onItemClickListener = object : SearchAdapter.OnItemClickListener {
            override fun onItemClick(exercisesItem: ExercisesItem) {
                viewModel.selectExercise(exercisesItem)
            }
        }
        exerciseSearchAdapter.onButtonClickListener = object : SearchAdapter.OnButtonClickListener {
            override fun onButtonClick(exercisesItem: ExercisesItem) {
                // Открытие диалогового окна для выбора количества повторений и добавление в список
//                RepetitionsDialogFragment.newInstance(exercisesItem) { repetitions ->
//                    listOfExercises.add(ExerciseWithReps(exercisesItem, repetitions))
//                }.show(childFragmentManager, "repetitionsDialog")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): SearchPageFragment {
            return SearchPageFragment()
        }
    }
}
