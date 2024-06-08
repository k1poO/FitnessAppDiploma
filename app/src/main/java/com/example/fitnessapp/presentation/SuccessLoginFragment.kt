package com.example.fitnessapp.presentation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.FragmentSuccessLoginBinding
import com.example.fitnessapp.domain.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SuccessLoginFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersReference: DatabaseReference = database.getReference("Users")
    private lateinit var user: User

    private var _binding: FragmentSuccessLoginBinding? = null
    private val binding: FragmentSuccessLoginBinding
        get() = _binding ?: throw RuntimeException("FragmentSuccessLoginBinding = null")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuccessLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUserId = auth.currentUser?.uid
        currentUserId?.let { getUserData(it) }

        binding.btnGoToHome.setOnClickListener {
            launchHomePageFragment()
        }
    }

    private fun getUserData(userId: String) {
        val userRef: DatabaseReference = usersReference.child(userId)

        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                user = dataSnapshot.getValue(User::class.java)!!
                updateUIWithUserData()
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("SuccessLoginFragment", "User not found")
            }
        })
    }

    private fun updateUIWithUserData() {
        binding.tvSuccessTitle.text = getString(R.string.welcome_name, user.name)
    }

    private fun launchHomePageFragment() {
        val newFragment = HomePageFragment.newInstance()
        (activity as? MainActivity)?.loadFragment(newFragment)
    }

    companion object {

        fun newInstance(): SuccessLoginFragment {
            return SuccessLoginFragment()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}