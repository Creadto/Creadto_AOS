package com.creadto.creadto_aos.camera.model

import java.nio.FloatBuffer

data class FrameData(
    val points : FloatBuffer,
    val colors : FloatBuffer
)
