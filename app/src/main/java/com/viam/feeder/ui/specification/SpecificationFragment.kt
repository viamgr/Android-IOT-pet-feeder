package com.viam.feeder.ui.specification

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.viam.feeder.R
import com.viam.feeder.core.databinding.viewBinding
import com.viam.feeder.core.interfaces.OnItemClickListener
import com.viam.feeder.core.livedata.EventObserver
import com.viam.feeder.databinding.FragmentSpecificationBinding
import com.viam.feeder.feedVolume
import com.viam.feeder.ui.record.RecordFragment.Companion.PATH
import com.viam.feeder.ui.record.RecordFragment.Companion.REQUEST_KEY
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SpecificationFragment : Fragment(R.layout.fragment_specification) {

    private val binding by viewBinding(FragmentSpecificationBinding::bind)

    private val viewModel: SpecificationViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = viewModel
        }

        viewModel.openRecordDialog.observe(viewLifecycleOwner, EventObserver {
            findNavController().navigate(R.id.record_fragment)
        })

        viewModel.feedSounds.observe(viewLifecycleOwner, { list ->
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                list
            ).also { adapter ->
                binding.feedingSoundDropDown.setAdapter(adapter)
                binding.feedingSoundDropDown.setOnItemClickListener { _, _, position, _ ->
                    viewModel.onFeedSoundItemClicked(position)
                }
            }
        })

        viewModel.ledStates.observe(viewLifecycleOwner, { list ->
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                list
            ).also { adapter ->
                binding.ledDropdown.setAdapter(adapter)
                binding.ledDropdown.setOnItemClickListener { _, _, position, _ ->
                    viewModel.onLedItemClickListener(position)
                }
            }
        })

        binding.soundVolume.addOnChangeListener { _, value, fromUser ->
            viewModel.onSoundVolumeChanged(value)
        }

        viewModel.feedVolumeList.observe(viewLifecycleOwner, { list ->
            binding.foodVolume.withModels {
                list.forEach {
                    feedVolume {
                        id(it.id)
                        identifier(it.id)
                        label(it.label)
                        scale(it.scale)
                        selected(it.selected)
                        listener(object : OnItemClickListener {
                            override fun onItemClick(item: Any?) {
                                viewModel.onFeedVolumeClicked(item as Int)
                            }
                        })
                    }
                }
            }
        })

        setFragmentResultListener(REQUEST_KEY) { requestKey, bundle ->
            if (requestKey == REQUEST_KEY) {
                val result = bundle.getString(PATH)
                result?.let {
                    viewModel.onRecordFile(result)
                }

            }
        }

    }

}