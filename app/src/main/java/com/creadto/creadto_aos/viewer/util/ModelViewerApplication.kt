package com.creadto.creadto_aos.viewer.util

import android.app.Application
import com.creadto.creadto_aos.viewer.model.Model

class ModelViewerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: ModelViewerApplication

        // Store the current model globally, so that we don't have to re-decode it upon
        // relaunching the main or VR activities.
        // TODO: handle this a bit better.
        var currentModel: Model? = null
    }
}
