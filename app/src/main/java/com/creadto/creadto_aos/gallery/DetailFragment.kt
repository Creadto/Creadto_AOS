package com.creadto.creadto_aos.gallery

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import com.creadto.creadto_aos.MainActivity
import com.creadto.creadto_aos.R
import com.creadto.creadto_aos.databinding.FragmentDetailBinding
import java.io.File

class DetailFragment(
    private val directoryName : String
) : Fragment() {
    companion object {
        private const val TAG = "[Detail]"
    }
    private lateinit var callback : OnBackPressedCallback
    private var _binding : FragmentDetailBinding? = null
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                (activity as MainActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.nav_host_container, GalleryFragment()).commit()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val detailAdapter = DetailAdapter(mutableListOf())
        binding.rvDetail.adapter = detailAdapter

        val fileDir = File(context?.filesDir, directoryName)

        if(fileDir!!.exists()){
            val fileNames = fileDir.listFiles()?.map { file ->
                file.name
            } ?: emptyList()
            val _fileNames = fileNames.sorted()
            detailAdapter.update(_fileNames)
        } else {
            Log.e(TAG, "Directory not found")
        }

        detailAdapter.setOnItemClickListener(object: GalleryAdapter.FileClickListener{
            override fun onItemClickListener(file: String) {
                val filePath = context?.filesDir!!.path + "/" + directoryName + "/" + file
                val file = File(filePath)

                (activity as MainActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.nav_host_container, ViewerFragment(directoryName, file)).commit()
            }
        })
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