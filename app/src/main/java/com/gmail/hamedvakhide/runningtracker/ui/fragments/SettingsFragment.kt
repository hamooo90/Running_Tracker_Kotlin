package com.gmail.hamedvakhide.runningtracker.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gmail.hamedvakhide.runningtracker.R
import com.gmail.hamedvakhide.runningtracker.databinding.FragmentSettingsBinding
import com.gmail.hamedvakhide.runningtracker.util.Constants
import com.gmail.hamedvakhide.runningtracker.viewmodel.StatisticViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding : FragmentSettingsBinding? = null
    private val viewModel : StatisticViewModel by viewModels()

    @Inject
    lateinit var sharedPrefs : SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentSettingsBinding.bind(view)
        _binding = binding

        loadFromSharedPref()
        binding.btnApplyChanges.setOnClickListener {
            val name = binding.etName.text.toString()
            val weight = binding.etWeight.text.toString()
            if (applyChangesToSharedPref(name, weight)) {
                Snackbar.make(requireView(), "Changes saved", Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(requireView(), "Enter all the fields", Snackbar.LENGTH_SHORT).show()
            }

        }

    }

    private fun loadFromSharedPref(){
        val name = sharedPrefs.getString(Constants.KEY_NAME,"")
        val weight = sharedPrefs.getFloat(Constants.KEY_WEIGHT,75F)
        _binding?.etName?.setText(name)
        _binding?.etWeight?.setText(weight.toString())
    }
    private fun applyChangesToSharedPref(name: String, weight: String): Boolean {
        if (name.isEmpty() || weight.isEmpty()) {
            return false
        }

        sharedPrefs.edit()
            .putString(Constants.KEY_NAME, name)
            .putFloat(Constants.KEY_WEIGHT, weight.toFloat())
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