package com.example.fitnessapp.presentation

import android.app.AlertDialog
import com.example.fitnessapp.presentation.viewModels.ProfileViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fitnessapp.databinding.FragmentProfilePageBinding
import com.example.fitnessapp.domain.User

class ProfilePageFragment : Fragment() {

    private var _binding: FragmentProfilePageBinding? = null
    private val binding: FragmentProfilePageBinding
        get() = _binding ?: throw RuntimeException("FragmentProfilePageBinding = null")

    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        observeViewModel()

        binding.btnLogOut.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Вы уверены?")
                .setCancelable(false)
                .setPositiveButton("Да") { dialog, id ->
                    viewModel.logout()
                }
                .setNegativeButton("Нет") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }

        binding.cvProfile.setOnClickListener {
            val editProfileFragment = EditProfileFragment.newInstance()
            activity?.supportFragmentManager?.beginTransaction()
                ?.setCustomAnimations(
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right,
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right
                )
                ?.add(android.R.id.content, editProfileFragment)
                ?.addToBackStack(null)
                ?.commit()
        }
    }

    private fun observeViewModel() {
        viewModel.user.observe(viewLifecycleOwner) {
            if (it == null) {
                launchLoginPageFragment()
            } else {
                updateUIWithUserData(it)
            }
        }
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUIWithUserData(user: User) {
        binding.tvUserName.text = String.format("${user.name} ${user.lastName}")
        binding.tvUserCourse.text = user.level
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

        fun newInstance(): ProfilePageFragment {
            return ProfilePageFragment()
        }
    }
}