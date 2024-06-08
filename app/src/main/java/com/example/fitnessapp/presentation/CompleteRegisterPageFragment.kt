package com.example.fitnessapp.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fitnessapp.databinding.FragmentCompleteRegisterPageBinding
import com.example.fitnessapp.presentation.viewModels.CompleteRegisterViewModel

class CompleteRegisterPageFragment : Fragment() {

    private var _binding: FragmentCompleteRegisterPageBinding? = null
    private val binding: FragmentCompleteRegisterPageBinding
        get() = _binding ?: throw RuntimeException("FragmentCompleteRegisterPageBinding = null")

    private lateinit var viewModel: CompleteRegisterViewModel

    private var firstName: String = ""
    private var lastName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompleteRegisterPageBinding.inflate(inflater, container, false)
        arguments?.let {
            firstName = it.getString("firstName", "")
            lastName = it.getString("lastName", "")
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[CompleteRegisterViewModel::class.java]

        binding.btnConfirmRegister.setOnClickListener {
            val gender = binding.spinnerGender.selectedItem.toString()
            val level = binding.spinnerLevel.selectedItem.toString()
            val weight = binding.etWeightCompleteReg.text.toString().trim().toDouble()
            val height = binding.etHeightCompleteReg.text.toString().trim().toInt()

            val user = viewModel.createUser(firstName, lastName, gender, level, weight, height)
            viewModel.addUserToDatabase(user)

            launchSuccessLoginFragment()
        }
    }

    private fun launchSuccessLoginFragment() {
        val newFragment = SuccessLoginFragment.newInstance()
        (activity as? MainActivity)?.loadFragment(newFragment)
    }

    companion object {

        fun newInstance(firstName: String, lastName: String): CompleteRegisterPageFragment {
            val fragment = CompleteRegisterPageFragment()
            val args = Bundle()
            args.putString("firstName", firstName)
            args.putString("lastName", lastName)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}