package REPLACE_APPLICATION_ID.core.ui.inAppUpdate

import android.content.Context
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import REPLACE_APPLICATION_ID.core.common.constants.Constants.TAG

/**
 * Play In-App Updates helper. Construct as a Fragment property so
 * [registerForActivityResult] runs before the Fragment reaches STARTED.
 * Call [destroy] from [Fragment.onDestroyView].
 */
class InAppUpdateManager(
    private val fragment: Fragment,
    @StringRes private val updateDownloadedMessageRes: Int,
    @StringRes private val restartActionRes: Int,
) {

    private var appUpdateManager: AppUpdateManager? = null
    private var appUpdateInfo: AppUpdateInfo? = null

    private var isChecked = false
    private var updateType = AppUpdateType.IMMEDIATE
    private var callback: ((isUpdated: Boolean, message: String) -> Unit)? = null

    private val updateFlowResultLauncher =
        fragment.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                checkIfUpdateInstalled()
            } else {
                Log.d(TAG, "InAppUpdateManager: updateFlowResult: Failed: resultCode=${result.resultCode}")
                callback?.invoke(false, "Update flow cancelled or failed")
            }
        }

    fun initManager(context: Context) {
        if (appUpdateManager == null) {
            appUpdateManager = AppUpdateManagerFactory.create(context.applicationContext)
            Log.d(TAG, "InAppUpdateManager: initManager: Success")
        }
    }

    fun setUpdateType(updateType: Int = AppUpdateType.IMMEDIATE) {
        this.updateType = updateType
        Log.d(TAG, "InAppUpdateManager: setUpdateType: Success: type=$updateType")
    }

    fun checkForUpdate(callback: (isAvailable: Boolean, message: String) -> Unit) {
        if (isChecked) {
            Log.d(TAG, "InAppUpdateManager: checkForUpdate: Skipped: already checked this session")
            return
        }
        isChecked = true

        val manager = appUpdateManager
        if (manager == null) {
            Log.e(TAG, "InAppUpdateManager: checkForUpdate: Failed: appUpdateManager is null")
            callback(false, "appUpdateManager is null")
            return
        }

        manager.appUpdateInfo
            .addOnSuccessListener { info ->
                appUpdateInfo = info

                if (info.installStatus() == InstallStatus.DOWNLOADED) {
                    Log.d(TAG, "InAppUpdateManager: checkForUpdate: Downloaded: waiting for restart")
                    popupSnackBarForCompleteUpdate()
                    callback(true, "Update already downloaded")
                    return@addOnSuccessListener
                }

                when (info.updateAvailability()) {
                    UpdateAvailability.UNKNOWN -> {
                        Log.d(TAG, "InAppUpdateManager: checkForUpdate: Unknown")
                        callback(false, "Unknown response")
                    }
                    UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                        Log.d(TAG, "InAppUpdateManager: checkForUpdate: Empty: no updates")
                        callback(false, "No updates available")
                    }
                    UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> {
                        Log.d(TAG, "InAppUpdateManager: checkForUpdate: InProgress")
                        callback(false, "Update is in progress")
                    }
                    UpdateAvailability.UPDATE_AVAILABLE -> {
                        val allowed = info.isUpdateTypeAllowed(updateType)
                        Log.d(TAG, "InAppUpdateManager: checkForUpdate: Success: available=$allowed type=$updateType")
                        callback(allowed, if (allowed) "Update available" else "Update type not allowed")
                    }
                    else -> {
                        Log.d(TAG, "InAppUpdateManager: checkForUpdate: Failed: unhandled availability")
                        callback(false, "Unhandled update availability")
                    }
                }
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "InAppUpdateManager: checkForUpdate: Failed: ${error.message}")
                callback(false, error.message.orEmpty())
            }
    }

    fun requestForUpdate(callback: (isUpdated: Boolean, message: String) -> Unit) {
        this.callback = callback
        val manager = appUpdateManager
        val info = appUpdateInfo

        if (manager == null || info == null) {
            Log.e(TAG, "InAppUpdateManager: requestForUpdate: Failed: manager or info null")
            callback(false, "AppUpdateManager/AppUpdateInfo is null")
            return
        }

        Log.d(TAG, "InAppUpdateManager: requestForUpdate: Started: type=$updateType")
        manager.startUpdateFlowForResult(
            info,
            updateFlowResultLauncher,
            AppUpdateOptions.newBuilder(updateType).build(),
        )
    }

    private fun checkIfUpdateInstalled() {
        appUpdateManager?.appUpdateInfo?.addOnSuccessListener { info ->
            when (info.installStatus()) {
                InstallStatus.DOWNLOADED -> {
                    Log.d(TAG, "InAppUpdateManager: checkIfUpdateInstalled: Downloaded")
                    popupSnackBarForCompleteUpdate()
                }
                InstallStatus.INSTALLED -> {
                    Log.d(TAG, "InAppUpdateManager: checkIfUpdateInstalled: Success")
                    callback?.invoke(true, "Updated successfully")
                }
                InstallStatus.CANCELED -> {
                    Log.d(TAG, "InAppUpdateManager: checkIfUpdateInstalled: Cancelled")
                    callback?.invoke(false, "Cancelled by user")
                }
                else -> {
                    Log.e(TAG, "InAppUpdateManager: checkIfUpdateInstalled: Failed: status=${info.installStatus()}")
                    callback?.invoke(false, "Failed to update")
                }
            }
        }
    }

    private fun popupSnackBarForCompleteUpdate() {
        if (!fragment.isAdded || fragment.activity == null) return

        val rootView = fragment.activity?.findViewById<View>(android.R.id.content) ?: return
        val message = fragment.getString(updateDownloadedMessageRes)
        val action = fragment.getString(restartActionRes)

        Snackbar.make(rootView, message, Snackbar.LENGTH_INDEFINITE)
            .setAction(action) {
                Log.d(TAG, "InAppUpdateManager: completeUpdate: Started")
                appUpdateManager?.completeUpdate()
            }
            .show()
    }

    fun destroy() {
        Log.d(TAG, "InAppUpdateManager: destroy: Success")
        appUpdateManager = null
        appUpdateInfo = null
        callback = null
    }
}