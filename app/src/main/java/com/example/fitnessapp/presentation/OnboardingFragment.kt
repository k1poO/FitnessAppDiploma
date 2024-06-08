package com.example.fitnessapp.presentation

import com.example.fitnessapp.presentation.adapters.OnboardingAdapter
import com.example.fitnessapp.presentation.adapters.OnboardingItem
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.fitnessapp.R
import com.example.fitnessapp.databinding.FragmentOnboardingBinding
import me.relex.circleindicator.CircleIndicator3

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding: FragmentOnboardingBinding
        get() = _binding ?: throw RuntimeException("FragmentOnboardingBinding = null")

    private lateinit var onboardingAdapter: OnboardingAdapter
    private lateinit var indicator: CircleIndicator3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
    }

    private fun setupViewPager() {
        indicator = binding.indicator
        val onboardingItems = listOf(
            OnboardingItem(
                R.drawable.first_onboarding_img,
                "Отслеживайте свои цели",
                "Не волнуйтесь, если у вас возникнут проблемы с определением целей. Мы можем помочь вам определить цели и отслеживать их"
            ),
            OnboardingItem(
                R.drawable.second_onboarding_img,
                "Двигайтесь вперед",
                "Давайте продолжать гореть, чтобы достичь своих целей, это больно только временно, если вы сдадитесь сейчас, вам будет больно навсегда»"
            ),
            OnboardingItem(
                R.drawable.third_onboarding_img,
                "Правильно питайтесь",
                "Начните правильно питаться! Это поможет достичь результата быстрее!"
            ),
            OnboardingItem(
                R.drawable.fourth_onboarding_img,
                "Следите за сном",
                "Улучшайте качество своего сна вместе с нами, качественный сон может принести хорошее настроение с утра."
            )
        )

        onboardingAdapter = OnboardingAdapter(onboardingItems)
        binding.vpOnboarding.adapter = onboardingAdapter
        indicator.setViewPager(binding.vpOnboarding)
        binding.btnNextPage.setOnClickListener {
            val nextItem = binding.vpOnboarding.currentItem + 1
            if (nextItem < onboardingItems.size) {
                binding.vpOnboarding.currentItem = nextItem
            } else {
                launchRegisterPageFragment()
            }
        }
    }

    private fun launchRegisterPageFragment() {
        val newFragment = RegisterPageFragment.newInstance()
        (activity as? MainActivity)?.loadFragment(newFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        fun newInstance(): OnboardingFragment {
            return OnboardingFragment()
        }
    }
}