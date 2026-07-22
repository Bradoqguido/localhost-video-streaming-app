package com.rtspstreamer

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import com.rtspstreamer.domain.IRtspStreamer
import com.rtspstreamer.domain.models.StreamConfig
import com.rtspstreamer.domain.models.StreamEvent
import com.rtspstreamer.domain.models.StreamState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Android implementation of [IRtspStreamer] that serves a local MJPEG HTTP stream,
 * matching the iOS implementation for unified operation.
 */
class AndroidRtspStreamer : IRtspStreamer {

  companion object {
    private const val TAG = "AndroidRtspStreamer"
  }

  private var textureView: TextureView? = null
  private var cameraDevice: CameraDevice? = null
  private var captureSession: CameraCaptureSession? = null
  private var imageReader: ImageReader? = null

  // Threading for camera frames
  private var cameraThread: HandlerThread? = null
  private var cameraHandler: Handler? = null

  // Networking
  private var serverSocket: ServerSocket? = null
  private val clients = ConcurrentHashMap<UUID, Socket>()
  private var isStreaming = false

  private val _state = MutableStateFlow<StreamState>(StreamState.Idle)
  override val state: StateFlow<StreamState> = _state.asStateFlow()

  private val _events = MutableSharedFlow<StreamEvent>(extraBufferCapacity = 16)
  override val events: SharedFlow<StreamEvent> = _events.asSharedFlow()

  private val _isMuted = MutableStateFlow(false)
  override val isMicrophoneMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

  private var _config = StreamConfig()
  override val config: StreamConfig get() = _config

  private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

  /**
   * Attach the TextureView for camera preview rendering.
   */
  fun attachView(view: TextureView) {
    textureView = view
  }

  @SuppressLint("MissingPermission")
  override fun startPreview() {
    val view = textureView ?: run {
      _state.value = StreamState.Error("Preview TextureView not attached")
      return
    }

    try {
      val context = view.context
      val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
      
      // Find back camera
      val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
        val chars = cameraManager.getCameraCharacteristics(id)
        chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
      } ?: cameraManager.cameraIdList.firstOrNull() ?: throw IllegalStateException("No back camera found")

      // Start background thread for camera callbacks
      cameraThread = HandlerThread("CameraBackground").also { it.start() }
      cameraHandler = Handler(cameraThread!!.looper)

      cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
          cameraDevice = camera
          createCaptureSession(camera)
        }

        override fun onDisconnected(camera: CameraDevice) {
          camera.close()
          cameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
          camera.close()
          cameraDevice = null
          _state.value = StreamState.Error("Camera open error: $error")
        }
      }, cameraHandler)

    } catch (e: Exception) {
      _state.value = StreamState.Error("Preview failed: ${e.message}")
      Log.e(TAG, "startPreview error", e)
    }
  }

  private fun createCaptureSession(camera: CameraDevice) {
    val view = textureView ?: return
    val surfaceTexture = view.surfaceTexture ?: return
    
    // Set buffer size to standard 1280x720
    surfaceTexture.setDefaultBufferSize(1280, 720)
    val previewSurface = Surface(surfaceTexture)

    // ImageReader to capture YUV frames for HTTP streaming
    val reader = ImageReader.newInstance(1280, 720, ImageFormat.YUV_420_888, 2)
    reader.setOnImageAvailableListener({ r ->
      val image = r.acquireLatestImage() ?: return@setOnImageAvailableListener
      try {
        if (isStreaming) {
          val jpegBytes = imageToJpeg(image)
          broadcastFrame(jpegBytes)
        }
      } catch (e: Exception) {
        Log.e(TAG, "Frame capture process failed", e)
      } finally {
        image.close()
      }
    }, cameraHandler)
    imageReader = reader

    val targets = listOf(previewSurface, reader.surface)
    
    @Suppress("DEPRECATION")
    camera.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {
      override fun onConfigured(session: CameraCaptureSession) {
        captureSession = session
        try {
          val builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
          builder.addTarget(previewSurface)
          builder.addTarget(reader.surface)
          builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
          
          session.setRepeatingRequest(builder.build(), null, cameraHandler)
          _state.value = StreamState.Previewing
        } catch (e: Exception) {
          Log.e(TAG, "Repeating request start failed", e)
        }
      }

      override fun onConfigureFailed(session: CameraCaptureSession) {
        Log.e(TAG, "Capture session configuration failed")
        _state.value = StreamState.Error("Camera configuration failed")
      }
    }, cameraHandler)
  }

  override fun stopPreview() {
    try {
      captureSession?.stopRepeating()
      captureSession?.close()
      captureSession = null
      
      cameraDevice?.close()
      cameraDevice = null
      
      imageReader?.close()
      imageReader = null

      cameraThread?.quitSafely()
      cameraThread = null
      cameraHandler = null

      _state.value = StreamState.Idle
    } catch (e: Exception) {
      Log.e(TAG, "stopPreview error", e)
    }
  }

  override fun startStreaming() {
    if (isStreaming) return
    val port = _config.port

    scope.launch(Dispatchers.IO) {
      try {
        val server = ServerSocket(port)
        serverSocket = server
        isStreaming = true

        val localIp = getLocalIpAddress()
        val streamUrl = "http://$localIp:$port/"

        withContext(Dispatchers.Main) {
          _state.value = StreamState.Streaming(
            rtspUrl = streamUrl,
            connectedClients = 0,
            fps = 30.0f,
            uptimeMs = 0L
          )
        }

        while (isStreaming) {
          val client = try {
            server.accept()
          } catch (e: Exception) {
            break
          }
          handleNewConnection(client)
        }
      } catch (e: Exception) {
        Log.e(TAG, "Server socket start failed", e)
        withContext(Dispatchers.Main) {
          _state.value = StreamState.Error(e.message ?: "Failed to start HTTP server")
        }
      }
    }
  }

  override fun stopStreaming() {
    if (!isStreaming) return
    isStreaming = false

    try {
      serverSocket?.close()
      serverSocket = null
    } catch (e: Exception) {
      Log.e(TAG, "Error closing server socket", e)
    }

    val activeClients = clients.values.toList()
    clients.clear()
    for (socket in activeClients) {
      try {
        socket.close()
      } catch (ignored: Exception) {}
    }

    _state.value = StreamState.Previewing
  }

  override fun switchCamera() {
    // Excluded camera switching to match iOS back-camera preference
  }

  override fun setMicrophoneMuted(muted: Boolean) {
    _isMuted.value = muted
  }

  override fun updateConfig(newConfig: StreamConfig) {
    val wasStreaming = _state.value is StreamState.Streaming
    if (wasStreaming) {
      stopStreaming()
    }
    stopPreview()

    _config = newConfig

    startPreview()
    if (wasStreaming) {
      startStreaming()
    }
  }

  override fun release() {
    stopStreaming()
    stopPreview()
    scope.cancel()
  }

  private fun handleNewConnection(socket: Socket) {
    val id = UUID.randomUUID()
    clients[id] = socket
    updateClientsCount()

    scope.launch(Dispatchers.IO) {
      try {
        val output = socket.getOutputStream()
        // Read header packet request to clear buffer
        val input = socket.getInputStream()
        val buffer = ByteArray(1024)
        input.read(buffer)

        val responseHeaders = 
            "HTTP/1.1 200 OK\r\n" +
            "Content-Type: multipart/x-mixed-replace; boundary=mjpegboundary\r\n" +
            "Connection: keep-alive\r\n\r\n"

        output.write(responseHeaders.toByteArray())
        output.flush()
      } catch (e: Exception) {
        try { socket.close() } catch (ignored: Exception) {}
        clients.remove(id)
        updateClientsCount()
      }
    }
  }

  private fun broadcastFrame(jpegBytes: ByteArray) {
    val header = 
        "--mjpegboundary\r\n" +
        "Content-Type: image/jpeg\r\n" +
        "Content-Length: ${jpegBytes.size}\r\n\r\n"
        
    val packet = header.toByteArray() + jpegBytes + "\r\n".toByteArray()

    val activeClients = clients.entries.toList()
    for (entry in activeClients) {
      val id = entry.key
      val socket = entry.value
      try {
        val out = socket.getOutputStream()
        out.write(packet)
        out.flush()
      } catch (e: Exception) {
        try { socket.close() } catch (ignored: Exception) {}
        clients.remove(id)
        updateClientsCount()
      }
    }
  }

  private fun updateClientsCount() {
    val count = clients.size
    val localIp = getLocalIpAddress()
    val streamUrl = "http://$localIp:${_config.port}/"
    scope.launch(Dispatchers.Main) {
      if (isStreaming) {
        _state.value = StreamState.Streaming(
          rtspUrl = streamUrl,
          connectedClients = count,
          fps = 30.0f,
          uptimeMs = 0L
        )
      }
    }
  }

  private fun imageToJpeg(image: Image): ByteArray {
    val planes = image.planes
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    yBuffer.get(nv21, 0, ySize)

    // Interleave VU bytes
    val vBytes = ByteArray(vSize)
    val uBytes = ByteArray(uSize)
    vBuffer.get(vBytes)
    uBuffer.get(uBytes)

    var nv21Idx = ySize
    val minSize = minOf(vBytes.size, uBytes.size)
    for (i in 0 until minSize) {
      if (nv21Idx < nv21.size) {
        nv21[nv21Idx++] = vBytes[i]
      }
      if (nv21Idx < nv21.size) {
        nv21[nv21Idx++] = uBytes[i]
      }
    }

    val out = ByteArrayOutputStream()
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
    yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 70, out)
    val rawJpeg = out.toByteArray()

    // Rotate frame to align upright based on device sensor orientation
    val rotation = getRotationAngle()
    if (rotation == 0) return rawJpeg

    val bitmap = BitmapFactory.decodeByteArray(rawJpeg, 0, rawJpeg.size) ?: return rawJpeg
    val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
    val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    
    val rotatedOut = ByteArrayOutputStream()
    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, rotatedOut)
    
    bitmap.recycle()
    rotatedBitmap.recycle()
    return rotatedOut.toByteArray()
  }

  private fun getRotationAngle(): Int {
    val view = textureView ?: return 90
    val context = view.context
    
    val display = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
      context.display
    } else {
      @Suppress("DEPRECATION")
      (context.getSystemService(Context.WINDOW_SERVICE) as android.view.WindowManager).defaultDisplay
    }
    
    val rotation = display?.rotation ?: Surface.ROTATION_0
    val sensorOrientation = 90 // Default back camera sensor rotation orientation

    return when (rotation) {
      Surface.ROTATION_0 -> sensorOrientation
      Surface.ROTATION_90 -> 0
      Surface.ROTATION_180 -> 270
      Surface.ROTATION_270 -> 180
      else -> sensorOrientation
    }
  }
}
