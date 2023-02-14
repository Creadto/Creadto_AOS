package com.creadto.creadto_aos.camera.preview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.creadto.creadto_aos.R
import com.creadto.creadto_aos.camera.Renderer.particleData
import com.creadto.creadto_aos.databinding.PreviewBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PreviewBottomSheetFragment : BottomSheetDialogFragment() {

    private var bottomSheetBehavior : BottomSheetBehavior<View>? = null
    private lateinit var binding : PreviewBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PreviewBottomSheetBinding.inflate(inflater, container, false)
        binding.GLSurfaceView.setRenderer(PointCloudRenderer(particleData))
        return binding.root
    //val view = inflater.inflate(R.layout.preview_bottom_sheet, container, false)
        //return view
    }

    override fun getTheme(): Int = R.style.BottomSheetDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
    }

}