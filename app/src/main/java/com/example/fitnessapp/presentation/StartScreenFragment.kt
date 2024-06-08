package com.example.fitnessapp.presentation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.FragmentStartScreenBinding

class StartScreenFragment : Fragment() {
    private var _binding: FragmentStartScreenBinding? = null
    private val binding: FragmentStartScreenBinding
        get() = _binding ?: throw RuntimeException("FragmentStartScreen = null")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnGetStarted.setOnClickListener{
            launchOnboardingFragment()
        }
    }
    private fun launchOnboardingFragment() {
        val newFragment = OnboardingFragment.newInstance()
        (activity as? MainActivity)?.loadFragment(newFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}