package com.viam.feeder.ui.specification

import android.Manifest
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.viam.feeder.R
import com.viam.feeder.core.databinding.viewBinding
import com.viam.feeder.core.interfaces.OnItemClickListener
import com.viam.feeder.core.livedata.EventObserver
import com.viam.feeder.core.utility.dexter.permissionContract
import com.viam.feeder.databinding.FragmentSpecificationBinding
import com.viam.feeder.feedVolume
import com.viam.feeder.ui.record.RecordFragment.Companion.PATH
import com.viam.feeder.ui.record.RecordFragment.Companion.REQUEST_KEY
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File


@AndroidEntryPoint
class SpecificationFragment : Fragment(R.layout.fragment_specification) {

    private val binding by viewBinding(FragmentSpecificationBinding::bind)
    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { it ->
                permissionResult.request(Manifest.permission.READ_EXTERNAL_STORAGE) {
                    copyFileToAppData(it)
                }
            }
        }
    val output = lazy { "${requireActivity().externalCacheDir?.absolutePath}/converted.mp3" }
    val permissionResult = permissionContract()
    private val viewModel: SpecificationViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = viewModel
        }

        viewModel.openRecordDialog.observe(viewLifecycleOwner, EventObserver {
            permissionResult.request(Manifest.permission.RECORD_AUDIO) {
                findNavController().navigate(R.id.record_fragment)
            }
        })
        viewModel.chooseIntentSound.observe(viewLifecycleOwner, EventObserver {
            openChooseIntent()
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
                    viewModel.onRecordFile(result, output.value)
                }

            }
        }
    }

    private fun copyFileToAppData(uri: Uri) {
        try {
            val filePath =
                "${requireActivity().externalCacheDir?.absolutePath}/recording.mp3"
            val contentResolver = requireContext().contentResolver
            contentResolver.openInputStream(uri)?.use {
                val file = File(filePath)
                it.copyTo(file.outputStream())
                viewModel.onRecordFile(file.absolutePath, output.value)
            }
        } catch (exception: Exception) {
            // TODO: 11/11/2020 Post non fatal firebase Exception
            Timber.e(exception)
        }
    }

    private fun openChooseIntent() {
        getContent.launch("audio/mpeg")
    }

}