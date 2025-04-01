package com.question1

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.bridge.WritableNativeMap
import kotlin.math.atan2

class CompassModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext), SensorEventListener {

    private val TAG = "CompassModule"
    private val sensorManager: SensorManager =
        reactContext.getSystemService(ReactApplicationContext.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val stepCounter: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var stepCount = 0f

    init {
        if (accelerometer == null) Log.e(TAG, "Accelerometer not available")
        if (magnetometer == null) Log.e(TAG, "Magnetometer not available")
        if (stepCounter == null) Log.e(TAG, "Step counter not available")
    }

    override fun getName(): String {
        return "CompassModule"
    }

    @ReactMethod
    fun startCompass() {
        if (accelerometer == null || magnetometer == null) {
            sendError("Required sensors not available")
            return
        }
        try {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI)
            Log.d(TAG, "Compass started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting compass: ${e.message}")
            sendError("Failed to start compass: ${e.message}")
        }
    }

    @ReactMethod
    fun stopCompass() {
        try {
            sensorManager.unregisterListener(this)
            Log.d(TAG, "Compass stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping compass: ${e.message}")
            sendError("Failed to stop compass: ${e.message}")
        }
    }

    @ReactMethod
    fun startStepCounter() {
        if (stepCounter == null) {
            sendError("Step counter sensor not available")
            return
        }
        try {
            sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Step counter started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting step counter: ${e.message}")
            sendError("Failed to start step counter: ${e.message}")
        }
    }

    @ReactMethod
    fun stopStepCounter() {
        try {
            sensorManager.unregisterListener(this)
            Log.d(TAG, "Step counter stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping step counter: ${e.message}")
            sendError("Failed to stop step counter: ${e.message}")
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        try {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    System.arraycopy(event.values, 0, gravity, 0, event.values.size)
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
                }
                Sensor.TYPE_STEP_COUNTER -> {
                    stepCount = event.values[0]
                    val eventMap = WritableNativeMap().apply {
                        putDouble("steps", stepCount.toDouble())
                    }
                    sendEvent("StepUpdate", eventMap)
                }
            }

            val rotationMatrix = FloatArray(9)
            val orientationValues = FloatArray(3)

            if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                SensorManager.getOrientation(rotationMatrix, orientationValues)

                val azimuth = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
                val eventMap = WritableNativeMap().apply {
                    putDouble("azimuth", azimuth.toDouble())
                }

                sendEvent("CompassUpdate", eventMap)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing sensor data: ${e.message}")
            sendError("Error processing sensor data: ${e.message}")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        if (accuracy < SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM) {
            Log.w(TAG, "Sensor accuracy is low: $accuracy")
            sendError("Sensor accuracy is low: $accuracy")
        }
    }

    private fun sendEvent(eventName: String, params: WritableNativeMap) {
        try {
            reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(eventName, params)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending event: ${e.message}")
            sendError("Failed to send event: ${e.message}")
        }
    }

    private fun sendError(message: String) {
        try {
            val errorMap = WritableNativeMap().apply {
                putString("error", message)
            }
            sendEvent("CompassError", errorMap)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending error event: ${e.message}")
        }
    }
}
