package com.viam.feeder.ui.timer

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.viam.feeder.R
import com.viam.feeder.clock
import com.viam.feeder.core.databinding.viewBinding
import com.viam.feeder.core.interfaces.OnItemClickListener
import com.viam.feeder.core.livedata.EventObserver
import com.viam.feeder.databinding.FragmentTimerBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ObsoleteCoroutinesApi
import java.util.*

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
        }

        viewModel.clockTimer.observe(viewLifecycleOwner, { list ->
            binding.clockList.withModels {
                list.forEach {
                    clock {
                        id(it.id)
                        identifier(it.id)
                        clock(it.clock)
                        time(it.time)
                        listener(object : OnItemClickListener {
                            override fun onItemClick(item: Any?) {
                                viewModel.onClockTimerClicked(item as Long)
                            }
                        })
                    }
                }
            }
        })

        viewModel.openTimerDialog.observe(viewLifecycleOwner, EventObserver {
            showFrameworkTimePicker()
        })

    }


    private fun showFrameworkTimePicker() {
        val timePickerDialog = TimePickerDialog(
            context,
            { view: TimePicker?, hourOfDay: Int, minute: Int ->
                onTimeSet(
                    hourOfDay,
                    minute
                )
            },
            0,
            0,
            false
        )
        timePickerDialog.show()
    }

    private fun onTimeSet(newHour: Int, newMinute: Int) {
        val cal: Calendar = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, newHour)
        cal.set(Calendar.MINUTE, newMinute)
        cal.isLenient = false
//        val format: String = formatter.format(cal.getTime())
//        textView.setText(format)
//        hour = newHour
//        minute = newMinute
    }

}