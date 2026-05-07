package dev.steenbakker.mobile_scanner

import android.app.Activity
import android.hardware.SensorManager
import android.view.OrientationEventListener
import android.view.Surface
import io.flutter.embedding.engine.systemchannels.PlatformChannel
import io.flutter.plugin.common.EventChannel

/**
 * Tracks the physical device orientation, even when the app's UI is locked to
 * portrait, so the camera output can be rotated to keep world content upright.
 *
 * The Flutter event channel is kept as a no-op stream handler to preserve the
 * existing channel registration, but no events are emitted.
 */
class DeviceOrientationListener(
    activity: Activity,
) : OrientationEventListener(activity, SensorManager.SENSOR_DELAY_NORMAL),
    EventChannel.StreamHandler {

    @Volatile
    var currentSurfaceRotation: Int = Surface.ROTATION_0
        private set

    private var onRotationChangedListener: ((Int) -> Unit)? = null

    override fun onListen(event: Any?, eventSink: EventChannel.EventSink?) {}

    override fun onCancel(event: Any?) {}

    fun setOnRotationChangedListener(listener: ((Int) -> Unit)?) {
        onRotationChangedListener = listener
    }

    override fun onOrientationChanged(orientation: Int) {
        if (orientation == ORIENTATION_UNKNOWN) return

        val newRotation = when (orientation) {
            in 45..134 -> Surface.ROTATION_270
            in 135..224 -> Surface.ROTATION_180
            in 225..314 -> Surface.ROTATION_90
            else -> Surface.ROTATION_0
        }

        if (newRotation != currentSurfaceRotation) {
            currentSurfaceRotation = newRotation
            onRotationChangedListener?.invoke(newRotation)
        }
    }

    fun start() {
        if (canDetectOrientation()) {
            enable()
        }
    }

    fun stop() {
        disable()
        onRotationChangedListener = null
    }

    fun getOrientation(): PlatformChannel.DeviceOrientation {
        return when (currentSurfaceRotation) {
            Surface.ROTATION_0 -> PlatformChannel.DeviceOrientation.PORTRAIT_UP
            Surface.ROTATION_90 -> PlatformChannel.DeviceOrientation.LANDSCAPE_LEFT
            Surface.ROTATION_180 -> PlatformChannel.DeviceOrientation.PORTRAIT_DOWN
            Surface.ROTATION_270 -> PlatformChannel.DeviceOrientation.LANDSCAPE_RIGHT
            else -> PlatformChannel.DeviceOrientation.PORTRAIT_UP
        }
    }
}
