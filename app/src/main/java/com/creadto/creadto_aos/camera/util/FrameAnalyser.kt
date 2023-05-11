package com.creadto.creadto_aos.camera.util

import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class FrameAnalyser : ImageAnalysis.Analyzer {

    private var frameBitmap: Bitmap? = null

    override fun analyze(image: ImageProxy) {
        if(image.image != null) {
            frameBitmap = BitmapUtils.imageToBitmap(image.image!!, image.imageInfo.rotationDegrees)
            image.close()
        }
    }

}