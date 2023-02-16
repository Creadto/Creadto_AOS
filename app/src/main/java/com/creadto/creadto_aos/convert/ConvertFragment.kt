package com.creadto.creadto_aos.convert

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.creadto.creadto_aos.databinding.FragmentConvertBinding

class ConvertFragment : Fragment(), ConvertAdapter.FileClickListener {

    companion object {
        private const val TAG = "[Convert]"
    }

    private var _binding : FragmentConvertBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConvertBinding.inflate(inflater, container, false)
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
        val fileDir = context?.filesDir
        if(fileDir!!.exists()){
            val fileNames = fileDir.listFiles()?.map { file ->
                file.name
            } ?: emptyList()
        } else {
            Log.e(TAG, "Directory not found")
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    override fun onItemClickListener(file: String) {

    }

}