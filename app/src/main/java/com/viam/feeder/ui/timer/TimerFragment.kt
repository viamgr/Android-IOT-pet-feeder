package com.viam.feeder.ui.timer

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import com.viam.feeder.R
import com.viam.feeder.core.databinding.viewBinding
import com.viam.feeder.core.livedata.EventObserver
import com.viam.feeder.databinding.FragmentTimerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ObsoleteCoroutinesApi

@AndroidEntryPoint
@ObsoleteCoroutinesApi
class TimerFragment : Fragment(R.layout.fragment_timer) {

    private val binding by viewBinding(FragmentTimerBinding::bind)

    private val viewModel: TimerViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = viewModel
            clockList.setController(viewModel.controller)
        }
        binding.tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewModel.onTabChanged(tab?.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        viewModel.openTimerDialog.observe(viewLifecycleOwner, EventObserver {
            showFrameworkTimePicker()
        })
    }

    private fun showFrameworkTimePicker() {
        val timePickerDialog = TimePickerDialog(
            context,
            { _: TimePicker?, hourOfDay: Int, minute: Int ->
                onTimeSet(hourOfDay, minute)
            }, 0, 0, false
        )
        timePickerDialog.show()
    }

    private fun onTimeSet(newHour: Int, newMinute: Int) {
        viewModel.onTimeSet(newHour, newMinute)
    }

}