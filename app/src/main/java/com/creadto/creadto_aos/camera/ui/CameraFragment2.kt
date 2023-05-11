package com.creadto.creadto_aos.camera.ui

import android.Manifest
import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.creadto.creadto_aos.R
import com.creadto.creadto_aos.camera.util.FrameAnalyser
import com.creadto.creadto_aos.camera.util.Logger
import com.creadto.creadto_aos.databinding.FragmentCamera2Binding
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

class CameraFragment2 : Fragment() {

    private var _binding : FragmentCamera2Binding? = null
    private val binding get() = _binding!!

    private lateinit var previewView : PreviewView
    private var preview: Preview? = null

    private lateinit var cameraProviderListenableFuture : ListenableFuture<ProcessCameraProvider>

    private var isFrontCameraOn = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       _binding = FragmentCamera2Binding.inflate(inflater, container, false)
        return binding.root
    }

    private fun requestCameraPermission() {
        requestCameraPermissionLauncher.launch( Manifest.permission.CAMERA )
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission() ) {
            isGranted : Boolean ->
        if ( isGranted ) {
            Logger.logInfo( "Camera permission granted by user." )
            setupCameraProvider( CameraSelector.LENS_FACING_FRONT )
        }
        else {
            Logger.logInfo( "Camera permission denied by user." )
            val alertDialog = AlertDialog.Builder( requireActivity() ).apply {
                setTitle( "Permissions" )
                setMessage( "The app requires the camera permission to function." )
                setPositiveButton( "GRANT") { dialog, _ ->
                    dialog.dismiss()
                    requestCameraPermission()
                }
                setNegativeButton( "CLOSE" ) { dialog, _ ->
                    dialog.dismiss()
                    activity?.supportFragmentManager
                        ?.beginTransaction()
                        ?.remove(this@CameraFragment2)
                        ?.commit()
                }
                setCancelable( false )
                create()
            }
            alertDialog.show()
        }
    }

    // Setup the PreviewView for live camera feed.
    // See the docs -> https://developer.android.com/training/camerax/preview
    // and https://developer.android.com/training/camerax/analyze
    private fun setupCameraProvider( cameraFacing : Int ) {
        cameraProviderListenableFuture = ProcessCameraProvider.getInstance( requireActivity() )
        cameraProviderListenableFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderListenableFuture.get()
                bindPreview( cameraProvider , cameraFacing )
            }
            catch (e: ExecutionException) {
                Logger.logError( e.message!! )
            }
            catch (e: InterruptedException) {
                Logger.logError( e.message!! )
            }
        }, ContextCompat.getMainExecutor( requireActivity() ))
    }

    private fun bindPreview( cameraProvider: ProcessCameraProvider , lensFacing : Int ) {
        // Unbind any previous use-cases as we'll attach them once again.
        if ( preview != null) {
            cameraProvider.unbind( preview )
        }

        Logger.logInfo( "Setting camera with ${
            if ( lensFacing == CameraSelector.LENS_FACING_FRONT ) { "front" }
            else { "rear" }
        } lens facing" )

        preview = Preview.Builder().build()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing( lensFacing )
            .build()
        preview!!.setSurfaceProvider(previewView.surfaceProvider)

        // Set the resolution which is the closest to the screen size.
        val displayMetrics = resources.displayMetrics
        val screenSize = Size( displayMetrics.widthPixels, displayMetrics.heightPixels )
        Logger.logInfo( "Screen size is $screenSize" )

    }
}