package com.viam.feeder.ui.timer

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.viam.feeder.R
import com.viam.feeder.core.databinding.viewBinding
import com.viam.feeder.core.livedata.EventObserver
import com.viam.feeder.databinding.FragmentTimerBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

@AndroidEntryPoint

class TimerFragment : Fragment(R.layout.fragment_timer) {

    private val binding by viewBinding(FragmentTimerBinding::bind)

    private val viewModel: TimerViewModel by viewModels()

    private val controller = TimerController()
        .also { timerController ->
            timerController.clickListener = { clockTimer ->
                viewModel.removeTimer(clockTimer)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = viewModel
            clockList.setController(controller)
        }

        viewModel.timerList.observe(viewLifecycleOwner, {
            controller.setData(it)
        })

        viewModel.showTimeSettingMenu.observe(viewLifecycleOwner, EventObserver {
            showTimeSettingDialog()
        })

        binding.tab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
//                viewModel.onTabChanged(tab?.position)
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

    private fun showTimeSettingDialog() {
        val now =
            DateFormat.format("EEE, MMMM dd, yyyy, h:mm aa", Date(System.currentTimeMillis()))
                .toString()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.time_source))
            .setMessage(getString(R.string.set_time_in_date, now))
            .setPositiveButton(R.string.confirm_date_set) { _, _ ->
                viewModel.onTimeSet(System.currentTimeMillis())
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showFrameworkTimePicker() {
        val timePickerDialog = TimePickerDialog(
            context,
            { _: TimePicker?, hourOfDay: Int, minute: Int ->
                onTimeSet(hourOfDay, minute)
            }, 0, 0, true
        )
        timePickerDialog.show()
    }

    private fun onTimeSet(newHour: Int, newMinute: Int) {
        viewModel.onAddTime(newHour, newMinute)
    }

}