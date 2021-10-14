package com.gmail.hamedvakhide.runningtracker.ui.fragments


import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.fondesa.kpermissions.PermissionStatus
import com.fondesa.kpermissions.anyPermanentlyDenied
import com.fondesa.kpermissions.anyShouldShowRationale
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.fondesa.kpermissions.request.PermissionRequest
import com.gmail.hamedvakhide.runningtracker.R
import com.gmail.hamedvakhide.runningtracker.adapters.RunAdapter
import com.gmail.hamedvakhide.runningtracker.databinding.FragmentRunBinding
import com.gmail.hamedvakhide.runningtracker.util.*
import com.gmail.hamedvakhide.runningtracker.util.showPermanentlyDeniedDialog
import com.gmail.hamedvakhide.runningtracker.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run),
    PermissionRequest.Listener {

    private var _binding: FragmentRunBinding? = null
    private val viewModel: MainViewModel by viewModels()

    private lateinit var runAdapter: RunAdapter

    private val request by lazy {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissionsBuilder(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ).build()
        } else {
            permissionsBuilder(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ).build()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermission()

        val binding = FragmentRunBinding.bind(view)
        _binding = binding

        binding.fab.setOnClickListener {
            findNavController().navigate(RunFragmentDirections.actionRunFragmentToTrackingFragment())
        }

        runAdapter = RunAdapter()
        binding.rvRuns.apply {
            this.adapter = runAdapter
            this.layoutManager = LinearLayoutManager(requireContext())
        }

        when (viewModel.sortType) {
            SortType.DATE -> binding.spFilter.setSelection(0)
            SortType.TIME -> binding.spFilter.setSelection(1)
            SortType.DISTANCE -> binding.spFilter.setSelection(2)
            SortType.SPEED -> binding.spFilter.setSelection(3)
            SortType.CALORIE -> binding.spFilter.setSelection(4)
        }

        viewModel.runList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                runAdapter.submitList(it)
            }
        })

        binding.spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> viewModel.sortRuns(SortType.DATE)
                    1 -> viewModel.sortRuns(SortType.TIME)
                    2 -> viewModel.sortRuns(SortType.DISTANCE)
                    3 -> viewModel.sortRuns(SortType.SPEED)
                    4 -> viewModel.sortRuns(SortType.CALORIE)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

    }

    private fun requestPermission() {
        if (TrackingUtil.hasLocationPermission(requireContext())) {
            return
        } else {
            request.addListener(this)
            request.addListener {
                if (it.anyPermanentlyDenied()) {
                    Snackbar.make(
                        requireView(),
                        R.string.additional_listener_msg,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

            request.send()
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    override fun onPermissionsResult(result: List<PermissionStatus>) {
        val context = requireContext()
        when {
            result.anyPermanentlyDenied() -> context.showPermanentlyDeniedDialog(result)
            result.anyShouldShowRationale() -> context.showRationaleDialog(result, request)
        }

    }
}