package com.creadto.creadto_aos.camera.ui;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.media.Image;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.creadto.creadto_aos.R;
import com.creadto.creadto_aos.camera.Renderer;
import com.creadto.creadto_aos.camera.model.DepthData;
import com.creadto.creadto_aos.camera.util.CameraPermissionHelper;
import com.creadto.creadto_aos.camera.util.DisplayRotationHelper;
import com.creadto.creadto_aos.camera.util.SnackbarHelper;
import com.creadto.creadto_aos.camera.util.TrackingStateHelper;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * This is a simple example that shows how to use ARCore Raw Depth API. The application will display
 * a 3D point cloud and allow the user control the number of points based on depth confidence.
 */
public class CameraFragment extends Fragment implements GLSurfaceView.Renderer, FileListener {
    private static final String TAG = CameraFragment.class.getSimpleName();

    // Rendering. The Renderers are created here, and initialized when the GL surface is created.
    private GLSurfaceView surfaceView;
    private ImageView btn_camera;
    private ImageView btn_blind;
    private ImageView btn_switch;

    private boolean installRequested;
    private boolean depthReceived;

    private Session session;
    private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
    private DisplayRotationHelper displayRotationHelper;
    private final TrackingStateHelper trackingStateHelper = new TrackingStateHelper(getActivity());

    private final Renderer renderer = new Renderer();

    // This lock prevents accessing the frame images while Session is paused.
    private final Object frameInUseLock = new Object();

    /** The current raw depth image timestamp. */
    private long depthTimestamp = -1;

    private enum CameraState {
        IDLE,
        RUNNING
    }

    private CameraState _state = CameraState.values()[0];

    private String directoryURL = null;
    private int plyCounter = 0;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
        if (permissions.containsValue(false)) {
            Toast.makeText(requireActivity(), "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(requireActivity());
            }
            getParentFragmentManager()
                    .beginTransaction()
                    .remove(this)
                    .commit();
        }
    });


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        surfaceView = view.findViewById(R.id.surfaceview);
        btn_camera =  view.findViewById(R.id.btn_camera);
        btn_blind = view.findViewById(R.id.btn_blind);
        btn_switch = view.findViewById(R.id.btn_switch);
        displayRotationHelper = new DisplayRotationHelper(requireActivity());

        // Set up rendering.
        surfaceView.setPreserveEGLContextOnPause(true);
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setEGLConfigChooser(8, 8, 8, 0, 16, 0);
        surfaceView.setRenderer(this);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        surfaceView.setWillNotDraw(false);

        installRequested = false;
        depthReceived = false;
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requestPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA});

        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(_state){
                    case IDLE :
                        btn_camera.setImageResource(R.drawable.camera_button_recording);
                        _state = CameraState.RUNNING;
                        setCameraDirection();
                        break;
                    case RUNNING :
                        btn_camera.setImageResource(R.drawable.camera_button);
                        _state = CameraState.IDLE;
                        showPreview();
                        break;
                }
            }
        });

        btn_blind.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

            }
        });

        btn_switch.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

            }
        });

        Log.e("TEST", "onViewCreated");

    }

    private void setCameraDirection() {
        // Note that order matters - see the note in onPause(), the reverse applies here.
        try {
            // Wait until the frame is no longer being processed.
            synchronized (frameInUseLock) {
                // Enable raw depth estimation and auto focus mode while ARCore is running.
                Config config = session.getConfig();
                config.setDepthMode(Config.DepthMode.RAW_DEPTH_ONLY);
                session.configure(config);
                session.resume();
            }
        } catch (CameraNotAvailableException e) {
            messageSnackbarHelper.showError(requireActivity(), "Camera not available. Try restarting the app.");
            session = null;
            return;
        }
    }

    @Override
    public void onDestroy() {
        if (session != null) {
            // Explicitly close ARCore Session to release native resources.
            // Review the API reference for important considerations before calling close() in apps with
            // more complicated lifecycle requirements:
            // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Session#close()
            session.close();
            session = null;
        }

        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        //FullScreenHelper.setFullScreenOnWindowFocusChanged(requireActivity(), true);

        if (session == null) {
            Exception exception = null;
            String message = null;
            try {
                switch (ArCoreApk.getInstance().requestInstall(requireActivity(), !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
                if (!CameraPermissionHelper.hasCameraPermission(requireActivity())) {
                    CameraPermissionHelper.requestCameraPermission(requireActivity());
                    return;
                }

                // Create the session.
                session = new Session(/* context= */ requireActivity());
            } catch (UnavailableArcoreNotInstalledException
                    | UnavailableUserDeclinedInstallationException e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (UnavailableDeviceNotCompatibleException e) {
                message = "This device does not support AR";
                exception = e;
            } catch (RuntimeException e) {
                message = "Failed to create AR session";
                exception = e;
            }

            if (!session.isDepthModeSupported(Config.DepthMode.RAW_DEPTH_ONLY)) {
                message = "This device does not support the ARCore Raw Depth API.";
                session = null;
            }

            if (message != null) {
                messageSnackbarHelper.showError(requireActivity(), message);
                Log.e(TAG, "Exception creating session", exception);
                return;
            }
        }

//        // Note that order matters - see the note in onPause(), the reverse applies here.
//        try {
//            // Wait until the frame is no longer being processed.
//            synchronized (frameInUseLock) {
//                // Enable raw depth estimation and auto focus mode while ARCore is running.
//                Config config = session.getConfig();
//                config.setDepthMode(Config.DepthMode.RAW_DEPTH_ONLY);
//                session.configure(config);
//                session.resume();
//            }
//        } catch (CameraNotAvailableException e) {
//            messageSnackbarHelper.showError(requireActivity(), "Camera not available. Try restarting the app.");
//            session = null;
//            return;
//        }

        surfaceView.onResume();
        displayRotationHelper.onResume();

        //messageSnackbarHelper.showMessage(this, "No depth yet. Try moving the device.");
    }

    @Override
    public void onPause() {
        super.onPause();
        //FullScreenHelper.setFullScreenOnWindowFocusChanged(requireActivity(), false);
        if (session != null) {
            // Note that the order matters - see note in onResume().
            // GLSurfaceView is paused before pausing the ARCore session, to prevent onDrawFrame() from
            // calling session.update() on a paused session.
            displayRotationHelper.onPause();
            surfaceView.onPause();
            session.pause();
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        // Prepare the rendering objects. This involves reading shaders, so may throw an IOException.
        try {
            renderer.createOnGlThread(/*context=*/ requireActivity());
        } catch (IOException e) {
            Log.e(TAG, "Failed to read an asset file", e);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        displayRotationHelper.onSurfaceChanged(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // Clear screen to notify driver it should not load any pixels from previous frame.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (session == null) {
            return;
        }

        if(_state == CameraState.RUNNING){
            // Synchronize prevents session.update() call while paused, see note in onPause().
            synchronized (frameInUseLock) {
                // Notify ARCore that the view size changed so that the perspective matrix can be adjusted.
                displayRotationHelper.updateSessionIfNeeded(session);

                try {
                    session.setCameraTextureNames(new int[] {0});

                    Frame frame = session.update();
                    Camera camera = frame.getCamera();

                    // Keep the screen unlocked while tracking, but allow it to lock when tracking stops.
                    trackingStateHelper.updateKeepScreenOnFlag(camera.getTrackingState());

                    if (camera.getTrackingState() != TrackingState.TRACKING) {
                        // If motion tracking is not available but previous depth is available, notify the user
                        // that the app will resume with tracking.
                        if (depthReceived) {
                            messageSnackbarHelper.showMessage(
                                    requireActivity(), TrackingStateHelper.getTrackingFailureReasonString(camera));
                        }

                        // If not tracking, do not render the point cloud.
                        return;
                    }

                    // Check if the frame contains new depth data or a 3D reprojection of the previous data. See
                    // documentation of acquireRawDepthImage16Bits for more details.
                    boolean containsNewDepthData;
                    try (Image depthImage = frame.acquireRawDepthImage16Bits()) {
                        containsNewDepthData = depthTimestamp == depthImage.getTimestamp();
                        depthTimestamp = depthImage.getTimestamp();
                    } catch (NotYetAvailableException e) {
                        // This is normal at the beginning of session, where depth hasn't been estimated yet.
                        containsNewDepthData = false;
                    }

                    if (containsNewDepthData) {
                        // Get Raw Depth data of the current frame.
                        final DepthData depth = DepthData.create(session, frame);

                        // Skip rendering the current frame if an exception arises during depth data processing.
                        // For example, before depth estimation finishes initializing.
                        if (depth != null) {
                            depthReceived = true;
                            renderer.update(depth);
                        }
                    }

                    float[] projectionMatrix = new float[16];
                    camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100.0f);
                    float[] viewMatrix = new float[16];
                    camera.getViewMatrix(viewMatrix, 0);

                    // Visualize depth points.
                    renderer.draw(viewMatrix, projectionMatrix);

                    // Hide all user notifications when the frame has been rendered successfully.
                    messageSnackbarHelper.hide(requireActivity());
                } catch (Throwable t) {
                    // Avoid crashing the application due to unhandled exceptions.
                    Log.e(TAG, "Exception on the OpenGL thread", t);
                }
            }
        }
    }

    @Override
    public void onDataReceived(@Nullable String directoryURL, int plyCounter) {
        this.directoryURL = directoryURL;
        this.plyCounter = plyCounter;
        Log.e(TAG,"directoryURL = " + directoryURL);
        Log.e(TAG, "plyCounter = " + plyCounter);
    }

    private void showPreview() {
        if(!depthReceived) {
            Toast.makeText(requireActivity(), "Depth is not accumulated", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        PreviewBottomSheetFragment preview = new PreviewBottomSheetFragment();
        preview.setListener(this);

        Bundle bundle = new Bundle();
        bundle.putString("path", directoryURL);
        bundle.putInt("count", plyCounter);
        preview.setArguments(bundle);

        preview.show(getParentFragmentManager(), preview.getTag());
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e("TEST", "onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e("TEST", "onDestoryView");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.e("TEST","onStart");
    }
}