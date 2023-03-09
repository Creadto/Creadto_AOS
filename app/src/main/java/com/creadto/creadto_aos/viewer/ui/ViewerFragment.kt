package com.creadto.creadto_aos.viewer.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import com.creadto.creadto_aos.MainActivity
import com.creadto.creadto_aos.R
import com.creadto.creadto_aos.camera.model.Particle
import com.creadto.creadto_aos.databinding.FragmentViewerBinding
import com.creadto.creadto_aos.gallery.DetailFragment
import com.creadto.creadto_aos.io.PlyLoader
import com.creadto.creadto_aos.viewer.ModelSurfaceView
import com.creadto.creadto_aos.viewer.model.Measurement
import com.creadto.creadto_aos.viewer.util.ModelViewerApplication
import com.creadto.creadto_aos.viewer.model.PlyModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.concurrent.CopyOnWriteArrayList

class ViewerFragment(
    private val directoryName : String,
    private val plyFile : File,
    private val result : Boolean
) : Fragment() {

    companion object {
        private const val TAG = "[Viewer]"
    }

    private lateinit var callback : OnBackPressedCallback
    private val plyLoader = PlyLoader()
    private var _binding : FragmentViewerBinding? = null
    private val binding get() = _binding!!
    private val _particleData : CopyOnWriteArrayList<Particle> = CopyOnWriteArrayList()

    private var modelGLSurfaceView : ModelSurfaceView? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                (activity as MainActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.nav_host_container, DetailFragment(directoryName)).commit()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        if(result) {
            binding.leftList.visibility = View.VISIBLE
            binding.rightList.visibility = View.VISIBLE
            binding.btnDetails.visibility = View.VISIBLE


            binding.btnDetails.setOnClickListener {

            }
        }

        val pointCloud = plyLoader.load(plyFile)
        _particleData.addAll(pointCloud)

        val stream = plyFile.inputStream()
        val model = PlyModel(stream)
        ModelViewerApplication.currentModel = model

        modelGLSurfaceView = ModelSurfaceView(requireContext(), model)
        binding.containerView.addView(modelGLSurfaceView)
    }

    private fun readJsonFile() {
        val filePath = context?.filesDir!!.path + "/" + directoryName + "/Measurement.json"
        val file = File(filePath)

        val jsonString = StringBuilder()
        BufferedReader(FileReader(file)).use { reader ->
            var line = reader.readLine()
            while (line != null) {
                jsonString.append(line)
                line = reader.readLine()
            }
        }


        val listType = object : TypeToken<List<Measurement>>() {}.type
        return Gson().fromJson(jsonString.toString(), listType)
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    override fun onDetach() {
        super.onDetach()
        callback.remove()
    }

}