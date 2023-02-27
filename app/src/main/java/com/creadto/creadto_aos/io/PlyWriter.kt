package com.creadto.creadto_aos.io

import com.creadto.creadto_aos.camera.Renderer.particleData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class PlyWriter {
    suspend fun writePlyFile(path : String) =
        withContext(Dispatchers.IO) {
            val cal = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmSS")
            val time = dateFormat.format(cal.time)

            val fileName = "$time.ply"
            val file = File(path, fileName)
            val vertexCount: Int = particleData.size

            BufferedWriter(FileWriter(file)).use { writer ->
                writer.write("ply\n")
                writer.write("format ascii 1.0\n")
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

                particleData.forEach { vertex ->
                    writer.write("${vertex.x} ${vertex.y} ${vertex.z} ${vertex.r} ${vertex.g} ${vertex.b} 255\n")
                }
            }
        }
}