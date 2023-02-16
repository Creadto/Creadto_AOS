package com.creadto.creadto_aos.gallery

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.creadto.creadto_aos.databinding.FragmentGalleryBinding

class GalleryFragment : Fragment() {

    companion object {
        private const val TAG = "[Convert]"
    }

    private var _binding : FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    private fun init() {
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val galleryAdapter = GalleryAdapter(mutableListOf())
        binding.rvGallery.adapter = galleryAdapter

        val fileDir = context?.filesDir
        if(fileDir!!.exists()){
            val fileNames = fileDir.listFiles()?.map { file ->
                file.name
            } ?: emptyList()
            galleryAdapter.update(fileNames)
        } else {
            Log.e(TAG, "Directory not found")
        }

        galleryAdapter.setOnItemClickListener(object: GalleryAdapter.FileClickListener{
            override fun onItemClickListener(file: String) {
                Log.d("TEST", "${file}")
            }
        })

    }
}