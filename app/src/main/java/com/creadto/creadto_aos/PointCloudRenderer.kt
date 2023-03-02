package com.creadto.creadto_aos

import android.opengl.GLSurfaceView
import android.opengl.GLU
import com.creadto.creadto_aos.camera.model.Particle
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.CopyOnWriteArrayList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class PointCloudRenderer(
    private val pointCloud: CopyOnWriteArrayList<Particle>
) : GLSurfaceView.Renderer {

    private val vertexBuffer: FloatBuffer
    private val colorBuffer: ByteBuffer
    var camera = floatArrayOf(0f, 0f, 1f, 1f)

    init {
        val vertexData = pointCloud.flatMap { listOf(it.x, it.y, it.z) }
        val colorData = pointCloud.flatMap { listOf(it.r.toByte(), it.g.toByte(), it.b.toByte(), 255.toByte()) }

        vertexBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer.put(vertexData.toFloatArray())
        vertexBuffer.position(0)

        colorBuffer = ByteBuffer.allocateDirect(colorData.size)
            .order(ByteOrder.nativeOrder())
        colorBuffer.put(colorData.toByteArray())
        colorBuffer.position(0)
    }


    override fun onDrawFrame(gl: GL10?) {
        gl?.glClear(GL10.GL_COLOR_BUFFER_BIT or GL10.GL_DEPTH_BUFFER_BIT)
        gl?.glEnableClientState(GL10.GL_VERTEX_ARRAY)
        gl?.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer)
        gl?.glEnableClientState(GL10.GL_COLOR_ARRAY)
        gl?.glColorPointer(4, GL10.GL_UNSIGNED_BYTE, 0, colorBuffer)
        gl?.glDrawArrays(GL10.GL_POINTS, 0, pointCloud.size)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        gl?.glViewport(0, 0, width, height)
        gl?.glMatrixMode(GL10.GL_PROJECTION)
        gl?.glLoadIdentity()
        GLU.gluPerspective(gl, 45f, width.toFloat() / height.toFloat(), 0.1f, 100f)
        gl?.glMatrixMode(GL10.GL_MODELVIEW)
        gl?.glLoadIdentity()
        GLU.gluLookAt(gl, camera[0], camera[1], camera[2], 0f, 0f, 0f, 0f, 1f, 0f)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        gl?.glClearColor(1f, 1f, 1f, 1f)
        gl?.glEnable(GL10.GL_DEPTH_TEST)
        gl?.glEnable(GL10.GL_POINT_SMOOTH)
        gl?.glEnable(GL10.GL_BLEND)
        gl?.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA)

        gl?.glHint(GL10.GL_POINT_SMOOTH_HINT, GL10.GL_NICEST)
    }
}