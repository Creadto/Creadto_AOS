package com.creadto.creadto_aos

import android.content.Context
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import android.view.MotionEvent
import com.creadto.creadto_aos.camera.model.Particle
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.sqrt

class PointCloudSurfaceView(
    context: Context,
    pointCloud : CopyOnWriteArrayList<Particle>
) : GLSurfaceView(context) {
    private val renderer: PointCloudRenderer

    private var previousX: Float = 0f
    private var previousY: Float = 0f
    private var scalingFactor = 1f

    init {
        setEGLContextClientVersion(2)
        renderer = PointCloudRenderer(pointCloud)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                Log.e("TEST", "ACTION_DOWN")
                previousX = event.x
                previousY = event.y
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                Log.e("TEST", "ACTION_POINTER_DOWN")
                // Calculate distance between fingers
                val xDiff = event.getX(0) - event.getX(1)
                val yDiff = event.getY(0) - event.getY(1)
                val distance = sqrt(xDiff * xDiff + yDiff * yDiff)

                // Set scaling factor
                scalingFactor = distance / height
            }

            MotionEvent.ACTION_MOVE -> {
                Log.e("TEST", "ACTION_MOVE")
                if (event.pointerCount == 1) {
                    val deltaX = (event.x - previousX) / width
                    val deltaY = (event.y - previousY) / height

                    // Rotate the camera around the origin
                    val rotationAngleX = -deltaY * 180
                    val rotationAngleY = deltaX * 180
                    val rotationMatrix = FloatArray(16)
                    Matrix.setIdentityM(rotationMatrix, 0)
                    Matrix.rotateM(rotationMatrix, 0, rotationAngleX, 1.0f, 0.0f, 0.0f)
                    Matrix.rotateM(rotationMatrix, 0, rotationAngleY, 0.0f, 1.0f, 0.0f)
                    Matrix.multiplyMV(renderer.camera, 0, rotationMatrix, 0, renderer.camera, 0)

                    Log.e("TEST", "11111 renderer.camera = ${renderer.camera[0]}  ${renderer.camera[1]}  ${renderer.camera[2]}")

                    requestRender()
                } else if (event.pointerCount == 2) {
                    // Calculate new distance between fingers
                    val xDiff = event.getX(0) - event.getX(1)
                    val yDiff = event.getY(0) - event.getY(1)
                    val newDistance = sqrt(xDiff * xDiff + yDiff * yDiff)

                    // Calculate scaling factor
                    val newScalingFactor = newDistance / height
                    val deltaScalingFactor = newScalingFactor / scalingFactor

                    // Update scaling factor
                    scalingFactor = newScalingFactor

                    // Zoom in or out
                    renderer.camera[2] *= deltaScalingFactor

                    Log.e("TEST", "2222 renderer.camera = ${renderer.camera[0]}  ${renderer.camera[1]}  ${renderer.camera[2]}")

                    requestRender()
                }

                previousX = event.x
                previousY = event.y
            }
        }

        return true
    }
}