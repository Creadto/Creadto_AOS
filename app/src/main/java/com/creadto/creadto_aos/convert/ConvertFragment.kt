package com.creadto.creadto_aos.convert

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.creadto.creadto_aos.R
import com.creadto.creadto_aos.convert.network.ApiRemoteSource
import com.creadto.creadto_aos.databinding.FragmentConvertBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class ConvertFragment(
    val apiRemoteSource: ApiRemoteSource
) : Fragment() {

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
        val convertAdapter = ConvertAdapter(mutableListOf())
        binding.rvConvert.adapter = convertAdapter

        val fileDir = context?.filesDir
        if(fileDir!!.exists()){
            val fileNames = fileDir.listFiles()?.map { file ->
                file.name
            } ?: emptyList()
            val _fileNames = fileNames.sorted()
            convertAdapter.update(_fileNames)
            setSwipeToDelete(_fileNames.toMutableList())
        } else {
            Log.e(TAG, "Directory not found")
        }

        convertAdapter.setOnItemClickListener(object: ConvertAdapter.FileClickListener{
            override fun onItemClickListener(file: String) {
                sendToServer(file)
            }
        })
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    // Swipe Delete helper
    private fun setSwipeToDelete(list : MutableList<String>) {
        ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val fileName = list.get(position)
                    val fileDir = context?.filesDir
                    val deleteFile = File(fileDir, fileName)
                    deleteFile.deleteRecursively()
                    list.removeAt(position)
                    (binding.rvConvert.adapter as ConvertAdapter).update(list)
                }

                override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
                    return 1f
                }

                override fun onChildDraw(
                    c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                    dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
                ) {
                    setDeleteIcon(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            }).attachToRecyclerView(binding.rvConvert)
    }

    private fun setDeleteIcon(
        c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {
        val mClearPaint = Paint()
        mClearPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))
        val mBackground = ColorDrawable()
        val backgroundColor = Color.parseColor("#b80f0a")
        val deleteDrawable = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_baseline_delete)
        val intrinsicWidth = deleteDrawable!!.intrinsicWidth
        val intrinsicHeight = deleteDrawable!!.intrinsicHeight

        val itemView = viewHolder.itemView
        val itemHeight = viewHolder.itemView.height

        val isCancelled = dX == 0f && !isCurrentlyActive

        if(isCancelled) {
            c.drawRect(itemView.right + dX, itemView
    .top.toFloat(),
                itemView.right.toFloat(), itemView.bottom.toFloat(), mClearPaint)
            return
        }

        mBackground.color = backgroundColor
        mBackground.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
        mBackground.draw(c)

        val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
        val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
        val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
        val deleteIconRight = itemView.right - deleteIconMargin
        val deleteIconBottom = deleteIconTop + intrinsicHeight

        deleteDrawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
        deleteDrawable.draw(c)
    }

    private fun sendToServer(dirName : String) {
        val dir_path = context?.filesDir!!.absolutePath + "/" + dirName

        GlobalScope.launch(Dispatchers.IO){
            try {
                val files = apiRemoteSource.sendDataCounter(dir_path)
                val res = apiRemoteSource.fileUpload(files)

                when(res){
                    true -> {
                        while(true){
                            val userData = apiRemoteSource.observeStatus("check")
                            when(userData!!.status) {
                                "Meshed" -> {
                                    Log.d(TAG, "Status = ${userData.status}")
                                    val file = apiRemoteSource.fileDownload("request").bytes()
                                    val fileName = "Mesh.ply"
                                    val filePath = dir_path + "/" + fileName

                                    val outputFile = File(filePath)
                                    val outputStream = FileOutputStream(outputFile)
                                    outputStream.write(file)
                                    outputStream.close()
                                    break
                                }

                                "Measured" -> {
                                    Log.d(TAG, "Status = ${userData.status}")
                                    val jsonData = userData.data!!.toByteArray(Charsets.UTF_8)
                                    val fileName = "Measurement.json"
                                    val filePath = dir_path + "/" +fileName

                                    val outputFile = File(filePath)
                                    val outputStream = FileOutputStream(outputFile)
                                    outputStream.write(jsonData)
                                    outputStream.close()
                                    continue
                                }

                                else -> {
                                    delay(5000)
                                    Log.d(TAG, "Status = ${userData.status}")
                                    continue
                                }
                            }
                        }
                    }

                    false -> {
                        Log.e(TAG,"File Upload Failed")
                    }
                }


            } catch(exception : Exception) {
                Log.e(TAG, exception.localizedMessage)
            }
        }

    }

}