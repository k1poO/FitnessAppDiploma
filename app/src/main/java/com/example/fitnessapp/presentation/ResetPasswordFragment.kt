package com.example.fitnessapp.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fitnessapp.databinding.FragmentResetPasswordBinding
import com.example.fitnessapp.presentation.viewModels.ResetPasswordViewModel

class ResetPasswordFragment : Fragment() {

    private var _binding: FragmentResetPasswordBinding? = null
    private val binding: FragmentResetPasswordBinding
        get() = _binding ?: throw RuntimeException("FragmentResetPasswordBinding = null")

    private lateinit var viewModel: ResetPasswordViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[ResetPasswordViewModel::class.java]
        observeViewModel()

        val emailArgs = requireArguments().getString(EMAIL)

        emailArgs?.let {
            binding.etEmailForReset.setText(it)
        }

        binding.btnReset.setOnClickListener {
            val email = binding.etEmailForReset.text.toString().trim()
            viewModel.resetPassword(email)
        }
    }

    private fun observeViewModel() {
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireActivity(), it, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.success.observe(viewLifecycleOwner) { success ->
            success?.let {
                Toast.makeText(
                    requireActivity(),
                    "The reset link has been successfully sent",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    companion object {
        private const val EMAIL = "email"

        fun newInstance(email: String): ResetPasswordFragment {
            return ResetPasswordFragment().apply {
                arguments = Bundle().apply {
                    putString(EMAIL, email)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}