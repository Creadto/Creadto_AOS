package com.creadto.creadto_aos.convert.network


import android.util.Log
import com.creadto.creadto_aos.convert.network.model.UserData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import java.io.File

class ApiRemoteSource {

    companion object {
        const val TAG = "[Api]"
        const val MULTIPART_FORM_DATA = "multipart/form-data"
    }

    suspend fun sendDataCounter(path : String) : MutableList<MultipartBody.Part> {
        val directory = File(path)
        val _files = directory.listFiles()
        val plyTotalCounter = _files.size

        val files : MutableList<MultipartBody.Part> = ArrayList()
        val res = RetrofitInstance.api.sendDataCounter(plyTotalCounter)
        when(res.isSuccessful){
            true -> {
                Log.d(TAG, "sendDataCounter response Success")
                for(file in _files){
                    val requestFile = file.asRequestBody(MULTIPART_FORM_DATA.toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("file", file.name, requestFile)
                    files.add(part)
                }
            }

            false -> {
                Log.e(TAG, res.errorBody().toString())
            }

        }
        return files
    }

    suspend fun fileUpload(
       files : List<MultipartBody.Part>
    ) : Boolean  {
        for(file in files){
            val res = RetrofitInstance.api.fileUpload(file)
            when(res.isSuccessful) {
                true -> {
                    Log.d(TAG, "fileUpload response Success")
                }

                false -> {
                    Log.e(TAG, res.errorBody().toString())
                    return false
                }
            }
        }
        return true
    }

    suspend fun observeStatus(
        check : String
    ) : UserData? {
        val res = RetrofitInstance.api.observeStatus(check)
        when(res.isSuccessful){
            true -> {
                return res.body()!!
            }

            false -> {
                Log.e(TAG, res.errorBody().toString())
                return null
            }
        }
    }

    suspend fun fileDownload(
        request : String
    ) : ResponseBody {
        val res = RetrofitInstance.api.fileDownload(request)
        when(res.isSuccessful){
            true -> {
                return res.body()!!
            }

            false -> {
                Log.e(TAG, res.errorBody().toString())
                return res.errorBody()!!
            }
        }
    }
}