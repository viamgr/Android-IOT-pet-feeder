package com.viam.feeder.ui.record

import android.Manifest
import android.content.DialogInterface
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.CompositeMultiplePermissionsListener
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.multi.SnackbarOnAnyDeniedMultiplePermissionsListener
import com.viam.feeder.BR
import com.viam.feeder.R
import com.viam.feeder.core.livedata.EventObserver
import com.viam.feeder.databinding.FragmentRecordBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException

@AndroidEntryPoint
class RecordFragment : BottomSheetDialogFragment() {
    companion object {
        const val REQUEST_KEY = "RECORD"
        const val PATH = "PATH"
    }

    private lateinit var binding: FragmentRecordBinding
    private val viewModel: RecordViewModel by viewModels()
    private var recorder: MediaRecorder? = null

    private var allPermissionsListener: MultiplePermissionsListener? = null
    private var result: String? = null

    private var mediaPlayer: MediaPlayer? = null

    val file = lazy {
        "${requireActivity().externalCacheDir?.absolutePath}/recording.mp3"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = FrameLayout(requireActivity()).also { layout ->
        DataBindingUtil.inflate<FragmentRecordBinding>(
            inflater,
            R.layout.fragment_record,
            layout,
            true
        )
            ?.apply {
                lifecycleOwner = viewLifecycleOwner
                setVariable(BR.vm, viewModel)

            }?.also {
                binding = it
            }


        viewModel.startRecord.observe(viewLifecycleOwner, EventObserver {
            checkPermissions()
        })
        viewModel.playClicked.observe(viewLifecycleOwner, EventObserver {
            playSound()
        })
        viewModel.cancelClicked.observe(viewLifecycleOwner, EventObserver {
            result = null
            findNavController().navigateUp()
        })
        viewModel.stopClicked.observe(viewLifecycleOwner, EventObserver {
            stopRecording()
        })

        viewModel.applyClicked.observe(viewLifecycleOwner, EventObserver {
            result = if (File(file.value).exists()) file.value else null
            findNavController().navigateUp()
        })
    }


    private fun setResult() {
        setFragmentResult(REQUEST_KEY, bundleOf(PATH to result))
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        stopRecording()
        stopPlaying()
        setResult()
    }

    private fun stopRecording() {
        try {
            recorder?.stop()
            recorder?.release()
            recorder = null
        } catch (e: Exception) {
        }
    }

    private fun checkPermissions() {
        Dexter.withContext(requireContext())
            .withPermissions(Manifest.permission.RECORD_AUDIO)
            .withListener(allPermissionsListener)
            .check()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createPermissionListeners()
    }

    private fun createPermissionListeners() {
        val feedbackViewMultiplePermissionListener =
            object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        startRecord()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>,
                    token: PermissionToken
                ) {
                    showPermissionRationale(token)
                }
            }
        allPermissionsListener = CompositeMultiplePermissionsListener(
            feedbackViewMultiplePermissionListener,
            SnackbarOnAnyDeniedMultiplePermissionsListener.Builder.with(
                requireView(),
                R.string.all_permissions_denied_feedback
            )
                .withOpenSettingsButton(R.string.permission_rationale_settings_button_text)
                .build()
        )
    }


    fun showPermissionRationale(token: PermissionToken) {
        AlertDialog.Builder(requireContext()).setTitle("R.string.permission_rationale_title")
            .setMessage("R.string.permission_rationale_message")
            .setNegativeButton(android.R.string.cancel) { _: DialogInterface, _: Int ->
                token.cancelPermissionRequest()
            }
            .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
                token.continuePermissionRequest()
            }
            .setOnDismissListener {
                token.cancelPermissionRequest()
            }
            .show()
    }

    private fun stopPlaying() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
        }
    }


    private fun startRecord() {

        stopPlaying()
        stopRecording()
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(file.value)
            setAudioSamplingRate(44100)
            setAudioEncodingBitRate(128000)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            prepare()
            start()
            viewModel.onStartRecording(file.value)
        }
    }

    private fun playSound() {
        stopPlaying()
        stopRecording()
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.apply {
                try {
                    setDataSource(file.value)
                    mediaPlayer?.prepare()
                    mediaPlayer?.start()
                } catch (e: IOException) {
                    throw e
                }
            }
        }


    }

}