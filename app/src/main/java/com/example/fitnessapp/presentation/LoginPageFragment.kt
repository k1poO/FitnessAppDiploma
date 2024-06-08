package com.example.fitnessapp.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fitnessapp.databinding.FragmentLoginPageBinding
import com.example.fitnessapp.presentation.viewModels.LoginViewModel

class LoginPageFragment : Fragment() {

    private var _binding: FragmentLoginPageBinding? = null
    private val binding: FragmentLoginPageBinding
        get() = _binding ?: throw RuntimeException("FragmentLoginPageBinding = null")

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        observeViewModel()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.login(email, password)
        }
        binding.tvRegisterText.setOnClickListener {
            launchRegisterPageFragment()
        }
        binding.tvResetPassword.setOnClickListener {
            val emailReset = binding.etEmail.text.toString().trim()
            launchResetPasswordFragment(emailReset)
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
                launchHomePageFragment()
            }
        }
    }

    private fun launchRegisterPageFragment() {
        val newFragment = RegisterPageFragment.newInstance()
        (activity as? MainActivity)?.loadFragment(newFragment)
    }

    private fun launchResetPasswordFragment(email: String) {
        val newFragment = ResetPasswordFragment.newInstance(email)
        (activity as? MainActivity)?.loadFragmentWithBackStack(newFragment)
    }


    private fun launchHomePageFragment() {
        val newFragment = HomePageFragment.newInstance()
        (activity as? MainActivity)?.loadFragment(newFragment)
    }

    companion object {

        fun newInstance(): LoginPageFragment {
            return LoginPageFragment()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}