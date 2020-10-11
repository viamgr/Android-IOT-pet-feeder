package com.viam.feeder.ui.dashboard

import android.os.Bundle
import android.view.View
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.viam.feeder.R
import com.viam.feeder.core.databinding.viewBinding
import com.viam.feeder.core.livedata.EventObserver
import com.viam.feeder.databinding.FragmentDashboardBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private val binding by viewBinding(FragmentDashboardBinding::bind)

    private val viewModel: DashboardViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = viewModel
        }
        viewModel.toggleMotorState.observe(viewLifecycleOwner, EventObserver {
            binding.animationView.cancelAnimation()
            binding.animationView.playAnimation()
            binding.animationView.postDelayed(5000L) {
                binding.animationView.cancelAnimation()
            }

        })
    }

}