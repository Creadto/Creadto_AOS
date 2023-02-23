package com.creadto.creadto_aos.camera.ui

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.creadto.creadto_aos.R
import com.creadto.creadto_aos.camera.PointCloudRenderer
import com.creadto.creadto_aos.camera.Renderer
import com.creadto.creadto_aos.camera.Renderer.particleData
import com.creadto.creadto_aos.camera.io.PlyWriter
import com.creadto.creadto_aos.camera.model.Particle
import com.creadto.creadto_aos.databinding.PreviewBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
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

    private var directoryURL : String? = null
    private var plyCounter : Int = 0

    private var listener : FileListener? = null

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }

    fun setListener(listener : FileListener) {
        this.listener = listener
    }

    override fun getTheme(): Int = R.style.BottomSheetDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior!!.isDraggable = false

        directoryURL = arguments?.getString("path", null)
        plyCounter = arguments?.getInt("count", 0)!!

        binding.btnDelete.setOnClickListener {
            renderer.clearParticles()
            sendData(directoryURL, plyCounter)
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                if(plyCounter % 4 == 0) { makeDirectory() }
                val writePlyFile = PlyWriter()
                writePlyFile.writePlyFile(directoryURL!!)
                plyCounter++
                renderer.clearParticles()
                sendData(directoryURL, plyCounter)
                dismiss()
            }
        }
    }

    private fun makeDirectory() {
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmSS")
        val time = dateFormat.format(cal.time)

        directoryURL = "${requireContext().filesDir.path}/$time"
        val dir = File(directoryURL)
        dir.mkdir()
    }

    private fun sendData(directoryURL : String?, plyCounter : Int) { listener?.onDataReceived(directoryURL, plyCounter) }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}