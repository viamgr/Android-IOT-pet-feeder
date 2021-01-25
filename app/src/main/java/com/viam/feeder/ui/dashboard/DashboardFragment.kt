package com.viam.feeder.ui.dashboard

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
import com.viam.feeder.core.livedata.EventObserver
import com.viam.feeder.core.utility.convertSeconds
import com.viam.feeder.core.utility.dexter.permissionContract
import com.viam.feeder.databinding.FragmentDashboardBinding
import com.viam.feeder.ui.record.RecordFragment
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private val binding by viewBinding(FragmentDashboardBinding::bind)
    private val viewModel: DashboardViewModel by viewModels()
    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { it ->
                permissionResult.request(Manifest.permission.READ_EXTERNAL_STORAGE) {
                    copyFileToAppData(it)
                }
            }
        }
    private val output =
        lazy { "${requireActivity().externalCacheDir?.absolutePath}/converted.mp3" }
    private val permissionResult = permissionContract()
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

        viewModel.feedVolumeList.observe(viewLifecycleOwner, { list ->
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                list.map { getString(it.label) }
            ).also { adapter ->
                binding.feedingVolumeDropDown.setAdapter(adapter)
                binding.feedingVolumeDropDown.setOnItemClickListener { _, _, position, _ ->
                    viewModel.onFeedingVolumeClicked(position)
                }
            }
        })
        viewModel.feedingVolumeValue.observe(viewLifecycleOwner, {
            binding.feedingVolumeDropDown.setText(getString(it), false)
        })

        viewModel.soundVolumeList.observe(viewLifecycleOwner, { list ->
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                list.map { getString(it.label) }
            ).also { adapter ->
                binding.soundVolumeDropDown.setAdapter(adapter)
                binding.soundVolumeDropDown.setOnItemClickListener { _, _, position, _ ->
                    viewModel.onSoundVolumeChanged(position)
                }
            }
        })

        viewModel.ledStates.observe(viewLifecycleOwner, { list ->
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                list.map { getString(it.label) }
            ).also { adapter ->
                binding.ledDropdown.setAdapter(adapter)
                binding.ledDropdown.setOnItemClickListener { _, _, position, _ ->
                    viewModel.onLedItemClickListener(position)
                }
            }
        })

        viewModel.ledTimerList.observe(viewLifecycleOwner, { list ->
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                list.map {
                    (it.value / 1000).toLong().convertSeconds(requireContext())
                }
            ).also { adapter ->
                binding.ledTimerDropDown.setAdapter(adapter)
                binding.ledTimerDropDown.setOnItemClickListener { _, _, position, _ ->
                    viewModel.onLedTimerItemClickListener(position)
                }
            }
        })
        viewModel.ledTimerValue.observe(viewLifecycleOwner) {
            binding.ledTimerDropDown.setText(
                (it.value / 1000).toLong().convertSeconds(requireContext()), false
            )
        }

        setFragmentResultListener(RecordFragment.REQUEST_KEY) { requestKey, bundle ->
            if (requestKey == RecordFragment.REQUEST_KEY) {
                val result = bundle.getString(RecordFragment.PATH)
                result?.let {
                    viewModel.onSoundFilePicked(result, output.value)
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
                viewModel.onSoundFilePicked(file.absolutePath, output.value)
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