package com.creadto.creadto_aos.camera.preview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.creadto.creadto_aos.R
import com.creadto.creadto_aos.camera.Renderer
import com.creadto.creadto_aos.camera.Renderer.particleData
import com.creadto.creadto_aos.camera.io.PlyWriter
import com.creadto.creadto_aos.camera.model.Particle
import com.creadto.creadto_aos.databinding.PreviewBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList

class PreviewBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val TAG = "[Preview]"
    }

    private var bottomSheetBehavior : BottomSheetBehavior<View>? = null
    private var _binding: PreviewBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val renderer = Renderer()

    private val _particleData : CopyOnWriteArrayList<Particle> = CopyOnWriteArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = PreviewBottomSheetBinding.inflate(inflater, container, false)
        _particleData.addAll(particleData)
        binding.GLSurfaceView.setRenderer(PointCloudRenderer(_particleData))
        return binding.root
    }

    override fun getTheme(): Int = R.style.BottomSheetDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior!!.isDraggable = false

        binding.btnDelete.setOnClickListener {
            renderer.clearParticles()
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val writePlyFile = PlyWriter()
                writePlyFile.writePlyFile(requireContext())
                renderer.clearParticles()
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}