package com.creadto.creadto_aos.camera.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.creadto.creadto_aos.R
import com.creadto.creadto_aos.camera.util.FileListener
import com.creadto.creadto_aos.databinding.PreviewBottomSheetBinding
import com.creadto.creadto_aos.viewer.ui.ViewerFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PreviewBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val TAG = "[Preview]"
    }

    private var bottomSheetBehavior : BottomSheetBehavior<View>? = null
    private var _binding: PreviewBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var directoryURL : String? = null
    private var plyCounter : Int = 0

    private var listener : FileListener? = null
    private lateinit var viewer: ViewerFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = PreviewBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setContentView(R.layout.preview_bottom_sheet)
        // Set the dialog to be expanded by default.
        val parentLayout = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        parentLayout.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        val bottomSheetBehavior = BottomSheetBehavior.from(parentLayout)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior.isDraggable = false
        return dialog
    }

    fun setListener(listener : FileListener) {
        this.listener = listener
    }

    override fun getTheme(): Int = R.style.BottomSheetDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = PreviewBottomSheetBinding.bind(view)

        bottomSheetBehavior = BottomSheetBehavior.from(view.parent as View)
        bottomSheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
        bottomSheetBehavior!!.isDraggable = false
        binding.progressBar.visibility = View.VISIBLE
        binding.btnDelete.isClickable = false
        binding.btnSave.isClickable = false

        directoryURL = arguments?.getString("path", null)
        plyCounter = arguments?.getInt("count", 0)!!

        if(plyCounter % 4 == 0) { makeDirectory() }

        viewLifecycleOwner.lifecycleScope.launch {
            //val writePlyFile = PlyWriter()
            //writePlyFile.writePlyFile(directoryURL!!, plyCounter)

            val dir = File(directoryURL!!)
            val plyFile = dir.listFiles().last()
            val directoryName = directoryURL!!.split("/").last()

            withContext(Dispatchers.Main) {
                viewer = ViewerFragment(
                    directoryName = directoryName,
                    plyFile = plyFile,
                    result = false
                )

                childFragmentManager.beginTransaction()
                    .add(R.id.frag_viewer,viewer)
                    .commit()

                binding.progressBar.visibility = View.GONE
                binding.btnDelete.isClickable = true
                binding.btnSave.isClickable = true
            }
        }

        binding.btnDelete.setOnClickListener {
            if(plyCounter % 4 == 0) { deleteDirectory() }
            else { deleteLastPly() }

            sendData(directoryURL, plyCounter)
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            plyCounter++
            sendData(directoryURL, plyCounter)
            dismiss()
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

    private fun deleteDirectory() {
        val dir = File(directoryURL)
        dir.delete()
        sendData(directoryURL = null, plyCounter = 0)
    }

    private fun deleteLastPly() {
        val dir = File(directoryURL)
        val files = dir.listFiles()
        files.last().delete()
    }

    private fun sendData(directoryURL : String?, plyCounter : Int) { listener?.onDataReceived(directoryURL, plyCounter) }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}