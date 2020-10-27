package com.viam.feeder.ui.loading

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.viam.feeder.BR
import com.viam.feeder.R
import com.viam.feeder.databinding.FragmentRecordBinding
import com.viam.feeder.ui.record.RecordViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LoadingFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentRecordBinding
    private val viewModel: RecordViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = FrameLayout(requireActivity()).also { layout ->
        DataBindingUtil.inflate<FragmentRecordBinding>(
            inflater, R.layout.fragment_loading,
            layout,
            true
        )
            ?.apply {
                lifecycleOwner = viewLifecycleOwner
                setVariable(BR.vm, viewModel)

            }?.also {
                binding = it
            }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}