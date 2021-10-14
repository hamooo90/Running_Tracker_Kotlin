package com.gmail.hamedvakhide.runningtracker.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.gmail.hamedvakhide.runningtracker.R
import com.gmail.hamedvakhide.runningtracker.databinding.FragmentSetupBinding
import com.gmail.hamedvakhide.runningtracker.util.Constants
import com.gmail.hamedvakhide.runningtracker.viewmodel.StatisticViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {

    private var _binding: FragmentSetupBinding? = null
    private val viewModel: StatisticViewModel by viewModels()

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    @set:Inject
    var isFirstTime = true

    @set:Inject
    var userName = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isFirstTime) {
            val toolbarText = "Keep going $userName"
            requireActivity().findViewById<MaterialToolbar>(R.id.toolbar).title = toolbarText

            val navOption = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()
            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment, savedInstanceState, navOption
            )
        }

        val binding = FragmentSetupBinding.bind(view)
        _binding = binding



        binding.tvContinue.setOnClickListener {
            val name = binding.etName.text.toString()
            val weight = binding.etWeight.text.toString()
            if (writePersonalInfoToSharedPrefs(name, weight)) {
                findNavController().navigate(SetupFragmentDirections.actionSetupFragmentToRunFragment())
            } else {
                Snackbar.make(requireView(), "Enter all the fields", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun writePersonalInfoToSharedPrefs(name: String, weight: String): Boolean {
        if (name.isEmpty() || weight.isEmpty()) {
            return false
        }

        sharedPrefs.edit()
            .putString(Constants.KEY_NAME, name)
            .putFloat(Constants.KEY_WEIGHT, weight.toFloat())
            .putBoolean(Constants.KEY_FIRST_TIME_TOGGLE, false)
            .apply()

        val toolbarText = "Keep going $name"
        requireActivity().findViewById<MaterialToolbar>(R.id.toolbar).title = toolbarText
        return true
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}