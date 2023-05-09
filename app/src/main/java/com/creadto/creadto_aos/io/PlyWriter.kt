package com.creadto.creadto_aos.io

import android.opengl.Matrix
import android.util.Log
import com.creadto.creadto_aos.camera.Renderer.particleData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class PlyWriter {
    suspend fun writePlyFile(path : String, plyCounter : Int) =
        withContext(Dispatchers.IO) {
            val cal = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmSS")
            val time = dateFormat.format(cal.time)

            val directions = listOf("Front", "Left", "Back", "Right")
            val fileName = "${directions.get(plyCounter % 4)}_$time.ply"

            val file = File(path, fileName)
            val vertexCount: Int = particleData.size

            // rotate -90 degrees around its y-axis(z- -> x+)
            val rotationMatrix = floatArrayOf(
                0f, 0f, 1f, 0f,
                0f, 1f, 0f, 0f,
                -1f, 0f, 0f, 0f,
                0f, 0f, 0f, 1f
            )

            BufferedWriter(FileWriter(file)).use { writer ->
                writer.write("ply\n")
                writer.write("format ascii 1.0\n")
                writer.write("comment direction ${directions.get(plyCounter % 4)}\n")
                writer.write("element vertex $vertexCount\n")
                writer.write("property float x\n")
                writer.write("property float y\n")
                writer.write("property float z\n")
                writer.write("property uchar red\n")
                writer.write("property uchar green\n")
                writer.write("property uchar blue\n")
                writer.write("property uchar alpha\n")
                writer.write("element face 0\n")
                writer.write("property list uchar int vertex_indices\n")
                writer.write("end_header\n")

                var vector: FloatArray? = null

                particleData.forEach { vertex ->
                    vector = floatArrayOf(vertex.x, vertex.y, vertex.z, 1f)
                    val rotatedVector = FloatArray(4)
                    Matrix.multiplyMV(rotatedVector, 0 , rotationMatrix, 0, vector, 0)
                    writer.write("${rotatedVector[0]} ${rotatedVector[1]} ${rotatedVector[2]} ${vertex.r} ${vertex.g} ${vertex.b} 255\n")
                }

            }
        }
}