package com.hypersoft.admobads.ui.fragments.base

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.hypersoft.admobads.BuildConfig
import com.hypersoft.admobads.helpers.firebase.FirebaseUtils.recordException

open class FragmentGeneral : Fragment() {

    private val generalTAG = "GeneralTAG"

    protected fun withDelay(delay: Long = 300, block: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed(block, delay)
    }

    protected fun getResString(stringId: Int): String {
        return context?.resources?.getString(stringId) ?: ""
    }

    /* ---------- Toast ---------- */

    protected fun showToast(message: String) {
        activity?.let {
            try {
                it.runOnUiThread {
                    Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
                }
            } catch (ex: Exception) {
                ex.recordException("showToast : ${it.javaClass.simpleName}")
            }
        }
    }

    protected fun debugToast(message: String) {
        if (BuildConfig.DEBUG) {
            showToast(message)
        }
    }

    protected fun showToast(stringId: Int) {
        val message = getResString(stringId)
        showToast(message)
    }

    /* ---------- Snackbar ---------- */

    protected fun showSnackBar(message: String) {
        this.view?.let { v ->
            activity?.let {
                try {
                    it.runOnUiThread {
                        Snackbar.make(v, message, Snackbar.LENGTH_SHORT).show()
                    }
                } catch (ex: Exception) {
                    ex.recordException("showSnackBar : ${it.javaClass.simpleName}")
                }
            }
        }
    }

    /* ---------- Keyboard (Input Method) ---------- */

    protected fun showKeyboard() {
        try {
            val imm: InputMethodManager? = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }catch (ex:Exception){
            ex.recordException("showKeyBoardTag")
        }
    }

    protected fun hideKeyboard() {
        try {
            val inputMethodManager: InputMethodManager = context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            val view: IBinder? = activity?.findViewById<View?>(android.R.id.content)?.windowToken
            inputMethodManager.hideSoftInputFromWindow(view, 0)
        } catch (ex: Exception) {
            ex.recordException("hideKeyboard")
        }
    }
}