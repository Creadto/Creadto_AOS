package com.creadto.creadto_aos.camera.io;

import android.content.Context;
import android.util.Log;

import com.creadto.creadto_aos.camera.model.Particle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PlyWriter {

    private Context context;
    private ArrayList<Particle> particleData;

    public PlyWriter(Context context, ArrayList<Particle> particleData) {
        this.context = context;
        this.particleData = particleData;
    }

    public Observable<Object> writePlyFile() {
        return Observable.create(emitter -> {
            try {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmSS");
                String time = dateFormat.format(cal.getTime());

                String fileName = "pointcloud" + time + ".ply";
                File plyFile = new File(context.getFilesDir(), fileName);
                int vertexCount = particleData.size();

                FileWriter writer = new FileWriter(plyFile);
                writer.write("ply\n");
                writer.write("format ascii 1.0\n");
                writer.write("element vertex " + vertexCount + "\n");
                writer.write("property float x\n");
                writer.write("property float y\n");
                writer.write("property float z\n");
                writer.write("property uchar red\n");
                writer.write("property uchar green\n");
                writer.write("property uchar blue\n");
                writer.write("property uchar alpha\n");
                writer.write("element face 0\n");
                writer.write("property list uchar int vertex_indices\n");
                writer.write("end_header\n");

                for (int i = 0; i < vertexCount; i++) {
                    Particle particle = particleData.get(i);
                    float x = particle.getX();
                    float y = particle.getY();
                    float z = particle.getZ();
                    int red = particle.getR();
                    int green = particle.getG();
                    int blue = particle.getB();
                    int alpha = 255;
                    writer.write(x + " " + y + " " + z + " " + red + " " + green + " " + blue + " " + alpha + "\n");
                }
                writer.flush();
                writer.close();

                emitter.onNext(true);
                emitter.onComplete();
            } catch (IOException e) {
                Log.d("TEST", "실패");
                emitter.onError(e);
            }
        }).subscribeOn(Schedulers.io());
    }
}