package com.creadto.creadto_aos.io

import com.creadto.creadto_aos.camera.model.Particle
import java.io.BufferedReader
import java.io.File
import java.io.FileReader


class PlyLoader {
    fun load(plyFile: File): ArrayList<Particle> {
        val points = ArrayList<Particle>()

        val reader = BufferedReader(FileReader(plyFile))
        var line = reader.readLine()
        var vertexCount = 0
        var propertyCount = 0
        var pointIndex = 0

        while (line != null) {
            if (line.startsWith("element vertex")) {
                vertexCount = line.split(" ")[2].toInt()
            } else if (line.startsWith("property")) {
                propertyCount++
            } else if (line.startsWith("end_header")) {
                break
            }
            line = reader.readLine()
        }

        for (i in 0 until vertexCount) {
            line = reader.readLine()
            val components = line.split(" ")
            val x = components[0].toFloat()
            val y = components[1].toFloat()
            val z = components[2].toFloat()
            val r = components[3].toInt()
            val g = components[4].toInt()
            val b = components[5].toInt()
            points.add(Particle(x, y, z, r, g, b))
            pointIndex++
        }

        reader.close()
        return points
    }
}