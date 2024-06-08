package com.example.fitnessapp.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fitnessapp.databinding.FragmentRegisterPageBinding
import com.example.fitnessapp.presentation.viewModels.RegisterViewModel

class RegisterPageFragment : Fragment() {

    private var _binding: FragmentRegisterPageBinding? = null
    private val binding: FragmentRegisterPageBinding
        get() = _binding ?: throw RuntimeException("FragmentRegisterPageBinding = null")

    private lateinit var viewModel: RegisterViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]
        observeViewModel()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val firstName = binding.etName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.signUp(email, password, firstName, lastName)
        }

        binding.tvLoginText.setOnClickListener {
            launchLoginPageFragment()
        }

        binding.tvLoginText.setOnClickListener {
            launchLoginPageFragment()
        }
    }

    private fun observeViewModel() {
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireActivity(), it, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                val firstName = binding.etName.text.toString().trim()
                val lastName = binding.etLastName.text.toString().trim()
                launchCompleteRegisterPageFragment(firstName, lastName)
            }
        }
    }

    private fun launchCompleteRegisterPageFragment(name: String, lastName: String) {
        val newFragment = CompleteRegisterPageFragment.newInstance(name, lastName)
        (activity as? MainActivity)?.loadFragment(newFragment)
    }

    private fun launchLoginPageFragment() {
        val newFragment = LoginPageFragment.newInstance()
        (activity as? MainActivity)?.loadFragment(newFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        fun newInstance(): RegisterPageFragment {
            return RegisterPageFragment()
        }
    }
}