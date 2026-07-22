import Foundation
import UIKit
import ComposeApp
import AVFoundation
import Network

class SwiftRtspStreamer: NSObject, IosStreamerDelegate, AVCaptureVideoDataOutputSampleBufferDelegate {
    
    static let shared = SwiftRtspStreamer()
    
    private var captureSession: AVCaptureSession?
    private var previewLayer: AVCaptureVideoPreviewLayer?
    private var previewContainer: UIView?
    
    // Networking
    private var listener: NWListener?
    private var clients: [UUID: NWConnection] = [:]
    private let queue = DispatchQueue(label: "com.rtspstreamer.server")
    private let lock = NSLock()
    private var _isStreaming = false
    private var isStreaming: Bool {
        get {
            lock.lock(); defer { lock.unlock() }
            return _isStreaming
        }
        set {
            lock.lock()
            let oldValue = _isStreaming
            _isStreaming = newValue
            lock.unlock()
            print("isStreaming changed from \(oldValue) to \(newValue) (Thread: \(Thread.current))")
        }
    }
    
    // Frame compression
    private var latestJpegData: Data?
    private let jpegQueue = DispatchQueue(label: "com.rtspstreamer.jpeg")
    
    private override init() {
        super.init()
    }
    
    func setup() {
        IosRtspStreamer.companion.delegate = self
    }
    
    func attachPreview(view: Any) {
        guard let parentView = view as? UIView else { return }
        self.previewContainer = parentView
        
        DispatchQueue.main.async {
            self.setupCaptureSession(in: parentView)
        }
    }
    
    private func setupCaptureSession(in parentView: UIView) {
        if captureSession != nil {
            previewLayer?.frame = parentView.bounds
            if previewLayer?.superlayer == nil {
                parentView.layer.addSublayer(previewLayer!)
            }
            return
        }
        
        let session = AVCaptureSession()
        session.sessionPreset = .hd1280x720
        
        guard let camera = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .back),
              let input = try? AVCaptureDeviceInput(device: camera) else {
            print("Failed to get back camera input")
            return
        }
        
        if session.canAddInput(input) {
            session.addInput(input)
        }
        
        let output = AVCaptureVideoDataOutput()
        output.setSampleBufferDelegate(self, queue: jpegQueue)
        output.alwaysDiscardsLateVideoFrames = true
        
        let initialOrientation = getCaptureOrientation()
        
        if session.canAddOutput(output) {
            session.addOutput(output)
            if let connection = output.connection(with: .video) {
                if connection.isVideoOrientationSupported {
                    connection.videoOrientation = initialOrientation
                }
            }
        }
        
        let preview = AVCaptureVideoPreviewLayer(session: session)
        preview.videoGravity = .resizeAspectFill
        preview.frame = parentView.bounds
        if let connection = preview.connection {
            if connection.isVideoOrientationSupported {
                connection.videoOrientation = initialOrientation
            }
        }
        parentView.layer.addSublayer(preview)
        
        // Listen to device orientation changes to adjust stream rotation
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleOrientationChange),
            name: UIDevice.orientationDidChangeNotification,
            object: nil
        )
        
        self.captureSession = session
        self.previewLayer = preview
        
        self.startPreview()
    }
    
    func startPreview() {
        guard let session = captureSession, !session.isRunning else { return }
        DispatchQueue.global(qos: .userInitiated).async {
            session.startRunning()
            DispatchQueue.main.async {
                IosRtspStreamer.companion.instance.updateState(state: StreamState.Previewing.shared)
            }
        }
    }
    
    func stopPreview() {
        guard let session = captureSession, session.isRunning else { return }
        DispatchQueue.global(qos: .userInitiated).async {
            session.stopRunning()
            DispatchQueue.main.async {
                IosRtspStreamer.companion.instance.updateState(state: StreamState.Idle.shared)
            }
        }
    }
    
    func startStreaming() {
        guard !isStreaming else { return }
        
        let config = IosRtspStreamer.companion.instance.config
        let port = NWEndpoint.Port(rawValue: UInt16(config.port)) ?? 8080
        
        do {
            let listener = try NWListener(using: .tcp, on: port)
            listener.stateUpdateHandler = { state in
                switch state {
                case .ready:
                    print("MJPEG HTTP Server ready on port \(port)")
                case .failed(let error):
                    print("MJPEG Server failed: \(error)")
                    self.stopStreaming()
                default:
                    break
                }
            }
            
            listener.newConnectionHandler = { connection in
                self.handleNewConnection(connection)
            }
            
            listener.start(queue: queue)
            self.listener = listener
            self.isStreaming = true
            
            let localIp = getLocalIPAddress()
            let streamUrl = "http://\(localIp):\(config.port)/"
            
            IosRtspStreamer.companion.instance.updateState(
                state: StreamState.Streaming(
                    rtspUrl: streamUrl,
                    connectedClients: 0,
                    fps: 30.0,
                    uptimeMs: 0
                )
            )
        } catch {
            print("Failed to start listener: \(error)")
        }
    }
    
    func stopStreaming() {
        DispatchQueue.main.async {
            print("stopStreaming called. isStreaming: \(self.isStreaming)")
            guard self.isStreaming else { return }
            self.isStreaming = false
            
            self.listener?.cancel()
            self.listener = nil
            
            self.lock.lock()
            let clientsToCancel = self.clients
            self.clients.removeAll()
            self.lock.unlock()
            
            for (id, connection) in clientsToCancel {
                connection.cancel()
                print("Cancelled connection: \(id)")
            }
            
            IosRtspStreamer.companion.instance.updateState(state: StreamState.Previewing.shared)
            print("MJPEG Stream stopped, state set back to Previewing")
        }
    }
    
    func switchCamera() {
        // Excluded camera switching per user requirements (use back camera only)
    }
    
    func setMicrophoneMuted(muted: Bool) {
        // Excluded audio processing for MJPEG HTTP server
    }
    
    // MARK: - AVCaptureVideoDataOutputSampleBufferDelegate
    func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
        guard isStreaming else { return }
        
        guard let imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else { return }
        let ciImage = CIImage(cvImageBuffer: imageBuffer)
        
        let cgOrientation = getCGImageOrientation()
        let rotatedImage = ciImage.oriented(cgOrientation)
        
        let context = CIContext()
        guard let cgImage = context.createCGImage(rotatedImage, from: rotatedImage.extent) else { return }
        let uiImage = UIImage(cgImage: cgImage)
        
        guard let jpeg = uiImage.jpegData(compressionQuality: 0.6) else { return }
        
        jpegQueue.async {
            self.latestJpegData = jpeg
            self.broadcastFrame(jpeg)
        }
    }
    
    // MARK: - HTTP TCP Connection Handling
    private func handleNewConnection(_ connection: NWConnection) {
        let id = UUID()
        self.lock.lock()
        self.clients[id] = connection
        self.lock.unlock()
        
        connection.stateUpdateHandler = { state in
            switch state {
            case .failed, .cancelled:
                self.lock.lock()
                self.clients.removeValue(forKey: id)
                self.lock.unlock()
                self.updateClientsCount()
            default:
                break
            }
        }
        
        connection.start(queue: queue)
        
        // Read HTTP request header
        connection.receive(minimumIncompleteLength: 1, maximumLength: 1024) { data, _, isComplete, error in
            if let error = error {
                print("Receive error: \(error)")
                connection.cancel()
                return
            }
            
            // Send MJPEG HTTP stream response headers
            let responseHeaders = 
                "HTTP/1.1 200 OK\r\n" +
                "Content-Type: multipart/x-mixed-replace; boundary=mjpegboundary\r\n" +
                "Connection: keep-alive\r\n\r\n"
            
            if let headerData = responseHeaders.data(using: .utf8) {
                connection.send(content: headerData, completion: .contentProcessed({ error in
                    if error != nil {
                        connection.cancel()
                        return
                    }
                    self.updateClientsCount()
                }))
            }
        }
    }
    
    private func broadcastFrame(_ jpegData: Data) {
        let frameHeader = 
            "--mjpegboundary\r\n" +
            "Content-Type: image/jpeg\r\n" +
            "Content-Length: \(jpegData.count)\r\n\r\n"
        
        guard let headerData = frameHeader.data(using: .utf8) else { return }
        
        let fullPacket = headerData + jpegData + "\r\n".data(using: .utf8)!
        
        self.lock.lock()
        let activeClients = self.clients
        self.lock.unlock()
        
        for (id, connection) in activeClients {
            connection.send(content: fullPacket, completion: .contentProcessed({ error in
                if error != nil {
                    connection.cancel()
                    self.lock.lock()
                    self.clients.removeValue(forKey: id)
                    self.lock.unlock()
                    self.updateClientsCount()
                }
            }))
        }
    }
    
    private func updateClientsCount() {
        self.lock.lock()
        let streaming = self._isStreaming
        let count = Int32(self.clients.count)
        self.lock.unlock()
        
        guard streaming else { return }
        
        let config = IosRtspStreamer.companion.instance.config
        let localIp = getLocalIPAddress()
        let streamUrl = "http://\(localIp):\(config.port)/"
        
        DispatchQueue.main.async {
            IosRtspStreamer.companion.instance.updateState(
                state: StreamState.Streaming(
                    rtspUrl: streamUrl,
                    connectedClients: count,
                    fps: 30.0,
                    uptimeMs: 0
                )
            )
        }
    }
    
    private func getLocalIPAddress() -> String {
        var address: String?
        var ifaddr: UnsafeMutablePointer<ifaddrs>?
        if getifaddrs(&ifaddr) == 0 {
            var ptr = ifaddr
            while ptr != nil {
                defer { ptr = ptr?.pointee.ifa_next }
                guard let interface = ptr?.pointee else { return "0.0.0.0" }
                let addrFamily = interface.ifa_addr.pointee.sa_family
                if addrFamily == UInt8(AF_INET) {
                    let name = String(cString: interface.ifa_name)
                    if name == "en0" { // WiFi interface
                        var hostname = [CChar](repeating: 0, count: Int(NI_MAXHOST))
                        getnameinfo(interface.ifa_addr, socklen_t(interface.ifa_addr.pointee.sa_len),
                                    &hostname, socklen_t(hostname.count),
                                    nil, 0, NI_NUMERICHOST)
                        address = String(cString: hostname)
                    }
                }
            }
            freeifaddrs(ifaddr)
        }
        return address ?? "0.0.0.0"
    }
    
    private func getCaptureOrientation() -> AVCaptureVideoOrientation {
        let orientation = UIDevice.current.orientation
        switch orientation {
        case .portrait:
            return .portrait
        case .portraitUpsideDown:
            return .portraitUpsideDown
        case .landscapeLeft:
            return .landscapeRight // Map device landscapeLeft to capture landscapeRight
        case .landscapeRight:
            return .landscapeLeft  // Map device landscapeRight to capture landscapeLeft
        default:
            if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene {
                switch windowScene.interfaceOrientation {
                case .portrait:
                    return .portrait
                case .portraitUpsideDown:
                    return .portraitUpsideDown
                case .landscapeLeft:
                    return .landscapeRight
                case .landscapeRight:
                    return .landscapeLeft
                default:
                    return .portrait
                }
            }
            return .portrait
        }
    }
    
    @objc private func handleOrientationChange() {
        guard let session = captureSession, session.isRunning else { return }
        let videoOrientation = getCaptureOrientation()
        
        DispatchQueue.main.async {
            if let previewConn = self.previewLayer?.connection, previewConn.isVideoOrientationSupported {
                previewConn.videoOrientation = videoOrientation
            }
            if let parent = self.previewContainer {
                self.previewLayer?.frame = parent.bounds
            }
        }
        
        if let sessionOutputs = captureSession?.outputs {
            for output in sessionOutputs {
                if let videoOutput = output as? AVCaptureVideoDataOutput {
                    if let connection = videoOutput.connection(with: .video), connection.isVideoOrientationSupported {
                        connection.videoOrientation = videoOrientation
                    }
                }
            }
        }
    }
    
    private func getCGImageOrientation() -> CGImagePropertyOrientation {
        guard let previewConnection = previewLayer?.connection else {
            return .right
        }
        switch previewConnection.videoOrientation {
        case .portrait:
            return .right
        case .portraitUpsideDown:
            return .left
        case .landscapeLeft:
            return .down
        case .landscapeRight:
            return .up
        @unknown default:
            return .right
        }
    }
}
