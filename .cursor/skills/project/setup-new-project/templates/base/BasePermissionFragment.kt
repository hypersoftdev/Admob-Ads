package YOUR.PACKAGE.presentation.base.fragment

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import YOUR.PACKAGE.core.common.constants.Constants.TAG
import YOUR.PACKAGE.core.ui.base.fragment.ParentFragment

/**
 * Template — copy to `:presentation` …/base/fragment/
 * Replace YOUR.PACKAGE with applicationId root.
 *
 * Before release: move dialog titles/messages to `:core-ui` strings.xml.
 * Trim unused permission helpers per product (keep only what the app needs).
 */
abstract class BasePermissionFragment<T : ViewBinding>(
    bindingFactory: (LayoutInflater) -> T,
) : ParentFragment<T>(bindingFactory) {

    private var sharedPreferences: SharedPreferences? = null
    private var callback: ((Boolean) -> Unit)? = null
    private var permissionName: String? = null
    private var permissionArray = arrayOf<String>()

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            callback?.invoke(it.values.contains(true))
        }

    private var settingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            permissionName?.let { permissionLauncher.launch(permissionArray) }
        }

    private var gpsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            callback?.invoke(checkGpsEnabled())
        }

    protected fun checkPermissionStorage(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) return true
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        ) == PackageManager.PERMISSION_GRANTED
    }

    protected fun checkAudioPermission(): Boolean =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    protected fun checkCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED

    protected fun checkLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    protected fun askStoragePermission(callback: (Boolean) -> Unit) {
        askPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            "Allow this app to access storage.",
            callback,
        )
    }

    protected fun askAudioPermission(callback: (Boolean) -> Unit) {
        askPermission(
            Manifest.permission.RECORD_AUDIO,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            "Allow this app to Record Audio.",
            callback,
        )
    }

    protected fun askCameraPermission(callback: (Boolean) -> Unit) {
        askPermission(
            Manifest.permission.CAMERA,
            arrayOf(Manifest.permission.CAMERA),
            "Allow this app to access your camera.",
            callback,
        )
    }

    protected fun askLocationPermission(callback: (Boolean) -> Unit) {
        askPermission(
            Manifest.permission.ACCESS_FINE_LOCATION,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            "Allow this app to access your location.",
            callback,
        )
    }

    private fun askPermission(
        permission: String,
        permissions: Array<String>,
        message: String,
        callback: (Boolean) -> Unit,
    ) {
        this.callback = callback
        this.permissionName = permission
        this.permissionArray = permissions

        val checkPermission = when (permission) {
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> checkPermissionStorage()
            Manifest.permission.RECORD_AUDIO -> checkAudioPermission()
            Manifest.permission.CAMERA -> checkCameraPermission()
            Manifest.permission.ACCESS_FINE_LOCATION -> checkLocationPermission()
            else -> false
        }
        if (checkPermission) {
            callback.invoke(true)
            return
        }

        val act = activity ?: run {
            callback.invoke(false)
            return
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(act, permission)) {
            showPermissionDialog(message, permissions)
        } else {
            if (sharedPreferences == null) {
                sharedPreferences = act.getSharedPreferences("permission_preferences", Context.MODE_PRIVATE)
            }
            if (sharedPreferences?.getBoolean(permission, true) == true) {
                sharedPreferences?.edit { putBoolean(permission, false) }
                try {
                    permissionLauncher.launch(permissions)
                } catch (ex: Exception) {
                    callback.invoke(false)
                    Log.e(TAG, "BasePermissionFragment: askPermission: Failed: ${ex.message}")
                }
            } else {
                showSettingDialog()
            }
        }
    }

    @Synchronized
    private fun showPermissionDialog(message: String, permissions: Array<String>) {
        val ctx = context ?: return
        MaterialAlertDialogBuilder(ctx)
            .setTitle("Permission")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Allow") { dialogInterface, _ ->
                dialogInterface.dismiss()
                try {
                    permissionLauncher.launch(permissions)
                } catch (ex: Exception) {
                    Log.e(TAG, "BasePermissionFragment: showPermissionDialog: Failed: ${ex.message}")
                }
            }
            .setNegativeButton("Deny") { dialogInterface, _ -> dialogInterface.dismiss() }
            .show()
    }

    private fun showSettingDialog() {
        val ctx = context ?: return
        MaterialAlertDialogBuilder(ctx)
            .setTitle("Permission required")
            .setMessage("Allow permission from 'Settings' to proceed")
            .setCancelable(false)
            .setPositiveButton("Setting") { dialogInterface, _ ->
                dialogInterface.dismiss()
                openSettingPage()
            }
            .setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.dismiss() }
            .show()
    }

    private fun openSettingPage() {
        val ctx = context ?: return
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", ctx.packageName, null)
        }
        settingLauncher.launch(intent)
    }

    protected fun checkGpsEnabled(): Boolean {
        val ctx = context ?: return false
        val locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
    }

    protected fun askEnableGPS(callback: (Boolean) -> Unit) {
        if (checkGpsEnabled()) {
            callback.invoke(true)
            return
        }
        val ctx = context ?: run {
            callback.invoke(false)
            return
        }
        this.callback = callback
        MaterialAlertDialogBuilder(ctx)
            .setTitle("Enable GPS")
            .setMessage("Location services are required for this feature. Please enable GPS.")
            .setCancelable(false)
            .setPositiveButton("Enable") { dialog, _ ->
                dialog.dismiss()
                view?.post {
                    try {
                        if (!isAdded || !lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                            callback.invoke(false)
                            return@post
                        }
                        gpsLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    } catch (e: Exception) {
                        Log.e(TAG, "BasePermissionFragment: askEnableGPS: Failed: ${e.message}")
                        callback.invoke(false)
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                callback.invoke(false)
            }
            .show()
    }

    private var postNotificationCallback: ((Boolean) -> Unit)? = null

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            Log.d(TAG, "BasePermissionFragment: notificationPermissionLauncher: isGranted=$it")
            postNotificationCallback?.invoke(it)
            postNotificationCallback = null
        }

    protected fun checkPostNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context ?: return false,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    protected fun askPostNotificationPermission(callback: (Boolean) -> Unit) {
        postNotificationCallback = callback
        if (checkPostNotificationPermission()) {
            callback.invoke(true)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            callback.invoke(true)
        }
    }

    private var alarmScheduleCallback: ((Boolean) -> Unit)? = null

    private val exactAlarmPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            val isGranted = canScheduleExactAlarms()
            Log.d(TAG, "BasePermissionFragment: exactAlarmPermissionLauncher: isGranted=$isGranted")
            alarmScheduleCallback?.invoke(isGranted)
            alarmScheduleCallback = null
        }

    protected fun canScheduleExactAlarms(): Boolean {
        val ctx = context ?: return false
        val alarmManager = ctx.getSystemService(ALARM_SERVICE) as AlarmManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    protected fun askExactAlarmSettings(callback: (Boolean) -> Unit) {
        alarmScheduleCallback = callback
        if (canScheduleExactAlarms()) {
            callback.invoke(true)
            return
        }
        val ctx = context ?: run {
            callback.invoke(false)
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = "package:${ctx.packageName}".toUri()
            }
            exactAlarmPermissionLauncher.launch(intent)
        } else {
            callback.invoke(true)
        }
    }
}
