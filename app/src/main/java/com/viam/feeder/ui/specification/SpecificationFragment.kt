package com.viam.feeder.ui.specification

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.devlomi.record_view.OnRecordListener
import com.viam.feeder.R
import com.viam.feeder.core.databinding.viewBinding
import com.viam.feeder.databinding.FragmentSpecificationBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ObsoleteCoroutinesApi


@AndroidEntryPoint
@ObsoleteCoroutinesApi
class SpecificationFragment : Fragment(R.layout.fragment_specification) {

    private val binding by viewBinding(FragmentSpecificationBinding::bind)

    private val viewModel: SpecificationViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = viewModel
        }

        viewModel.feedSounds.observe(viewLifecycleOwner, Observer { list ->
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                list
            ).also { adapter ->
                binding.spinnerDropdown.setAdapter(adapter)
            }
        })

        viewModel.ledStates.observe(viewLifecycleOwner, Observer { list ->
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                list
            ).also { adapter ->
                binding.soundVolumeDropdown.setAdapter(adapter)
            }
        })

        binding.recordButton.setRecordView(binding.recordView);
        binding.recordView.setOnRecordListener(object : OnRecordListener {
            override fun onStart() {
                //Start Recording..
                Log.d("RecordView", "onStart")
            }

            override fun onCancel() {
                //On Swipe To Cancel
                Log.d("RecordView", "onCancel")
            }

            override fun onFinish(recordTime: Long) {
                //Stop Recording..
//                val time: String = getHumanTimeText(recordTime)
                Log.d("RecordView", "onFinish")
                Log.d("RecordTime", "recordTime:$recordTime")
            }

            override fun onLessThanSecond() {
                //When the record time is less than One Second
                Log.d("RecordView", "onLessThanSecond")
            }
        })

        binding.soundVolume.addOnChangeListener { slider, value, fromUser ->
            // Responds to when slider's value is changed
        }
    }

}