package com.gmail.hamedvakhide.runningtracker.ui.fragments

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gmail.hamedvakhide.runningtracker.R
import com.gmail.hamedvakhide.runningtracker.data.Run
import com.gmail.hamedvakhide.runningtracker.databinding.FragmentTrackingBinding
import com.gmail.hamedvakhide.runningtracker.services.PolyLine
import com.gmail.hamedvakhide.runningtracker.services.TrackingService
import com.gmail.hamedvakhide.runningtracker.util.Constants
import com.gmail.hamedvakhide.runningtracker.util.Constants.ACTION_STOP_SERVICE
import com.gmail.hamedvakhide.runningtracker.util.TrackingUtil
import com.gmail.hamedvakhide.runningtracker.viewmodel.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {

    private var _binding: FragmentTrackingBinding? = null
    private val viewModel: MainViewModel by viewModels()

    private var isTracking = false
    private var path = mutableListOf<PolyLine>()

    private var currentTimeMillis = 0L

    private var map: GoogleMap? = null

    private var menu: Menu? = null

    @set:Inject
    var weight = 75f

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        val binding = FragmentTrackingBinding.bind(view)
        _binding = binding

        binding.btnToggleRun.setOnClickListener {
            toggleTracking()
        }
        binding.btnFinishRun.setOnClickListener {
            zoomFullTrack()
            endRunAndSaveToDb()
        }

        binding.mapView.apply {

            onCreate(savedInstanceState)
            getMapAsync {
                map = it
                addAllPolyLines()
            }

            subscribeToObservers()
        }
//        viewModel.getStartingLocation(requireContext())
//        viewModel.latLng.observe(viewLifecycleOwner,{
//            it?.let {
//                map?.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                    it,
//                    15f
//                ))
//            }
//        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (currentTimeMillis > 0L) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    private fun showCancelDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancel Run?")
            .setMessage("Are you sure you want to cancel your current run?")
            .setPositiveButton("Yes") { _, _ ->
                stopRun()
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
        dialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.miCancelTracking) {
            showCancelDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun stopRun() {
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(TrackingFragmentDirections.actionTrackingFragmentToRunFragment())
    }

    private fun addNewPolyLine() {
        if (path.isNotEmpty() && path.last().size > 1) {
            val size = path.last().size
            val secondLastLatLng = path.last()[size - 2]
            val lastLatLng = path.last()[size - 1]

            val polyLineOptions = PolylineOptions()
                .color(Color.RED)
                .width(6f)
                .add(secondLastLatLng)
                .add(lastLatLng)

            map?.addPolyline(polyLineOptions)
        }
    }

    private fun addAllPolyLines() {
        for (polyLine in path) {
            val polyLineOptions = PolylineOptions()
                .color(Color.RED)
                .width(6f)
                .addAll(polyLine)
            map?.addPolyline(polyLineOptions)
        }
    }

    private fun moveCameraToCurrentLatLng() {
        if (path.isNotEmpty() && path.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    path.last().last(),
                    15f
                )
            )
        }
    }

    private fun endRunAndSaveToDb() {
        map?.snapshot { bmp ->
            var distanceInMeter = 0
            for (polyline in path) {
                distanceInMeter += TrackingUtil.calculatePolyLineDistance(polyline).toInt()
            }
            val avgSpeedInKMH =
                round((distanceInMeter / 1000f) / (currentTimeMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeter / 1000f) * weight).toInt()

            val run = Run(bmp,dateTimeStamp,avgSpeedInKMH,distanceInMeter,currentTimeMillis,caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }

    }

    private fun zoomFullTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in path) {
            for (latLng in polyline) {
                bounds.include(latLng)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                _binding!!.mapView.width,
                _binding!!.mapView.height,
                (_binding!!.mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking) {
            _binding?.btnToggleRun?.text = "Start"
            _binding?.btnFinishRun?.visibility = View.VISIBLE
        } else {
            _binding?.btnToggleRun?.text = "Stop"
            _binding?.btnFinishRun?.visibility = View.GONE
            menu?.getItem(0)?.isVisible = true
        }
    }

    private fun toggleTracking() {
        if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(Constants.ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(Constants.ACTION_START_RESUME_SERVICE)
        }
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, {
            updateTracking(it)
        })

        TrackingService.path.observe(viewLifecycleOwner, {
            path = it
            addNewPolyLine()
            moveCameraToCurrentLatLng()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, {

            // fix finnish button show at on create
            if(it == 0L){
                _binding?.btnFinishRun?.visibility = View.GONE
            }

            currentTimeMillis = it
            val formattedTime = TrackingUtil.getFormattedTime(currentTimeMillis, true)
            _binding?.tvTimer?.text = formattedTime
        })
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }


    override fun onDestroy() {
        _binding?.mapView?.onDestroy()
        _binding = null
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        _binding?.mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        _binding?.mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        _binding?.mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        _binding?.mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        _binding?.mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _binding?.mapView?.onSaveInstanceState(outState)
    }

}