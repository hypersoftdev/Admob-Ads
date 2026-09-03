package YOUR.PACKAGE.core.ui.base.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.viewbinding.ViewBinding
import com.google.android.material.color.DynamicColors
import YOUR.PACKAGE.core.common.constants.Constants.TAG

/**
 * Template — copy to `:core-ui` …/base/activity/
 * Replace YOUR.PACKAGE with applicationId root.
 */
abstract class ParentActivity<T : ViewBinding>(
    private val bindingFactory: (LayoutInflater) -> T,
) : AppCompatActivity() {

    protected val binding by lazy { bindingFactory(layoutInflater) }

    protected var includeTopPadding = false
    protected var includeBottomPadding = true
    protected var enableKeyboardInsets = false
    protected var isLightStatusBars = false

    var statusBarHeight = 0
    var navigationBarHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        onPreCreated()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        setPadding()
        onCreated()
        initObservers()
    }

    open fun onPreCreated() {}
    open fun initObservers() {}

    private fun setPadding() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            WindowCompat.getInsetsController(window, window.decorView)
            when (enableKeyboardInsets) {
                true -> setPaddingKeyboard(v, insets)
                false -> setPaddingNormal(v, insets)
            }
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun setPaddingNormal(v: View, insets: WindowInsetsCompat) {
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
        )
        if (bars.top != 0) statusBarHeight = bars.top
        if (bars.bottom != 0) navigationBarHeight = bars.bottom
        val topPadding = if (includeTopPadding) bars.top else 0
        val bottomPadding = if (includeBottomPadding) bars.bottom else 0
        v.updatePadding(left = bars.left, top = topPadding, right = bars.right, bottom = bottomPadding)
    }

    private fun setPaddingKeyboard(v: View, insets: WindowInsetsCompat) {
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
        val cutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
        if (systemBars.top != 0) statusBarHeight = systemBars.top
        if (systemBars.bottom != 0) navigationBarHeight = systemBars.bottom
        val topPadding = if (includeTopPadding) maxOf(systemBars.top, cutout.top, statusBarHeight) else 0
        val bottomPadding =
            if (includeBottomPadding) maxOf(systemBars.bottom, imeInsets.bottom, navigationBarHeight) else 0
        v.updatePadding(
            left = systemBars.left,
            top = topPadding,
            right = systemBars.right,
            bottom = bottomPadding,
        )
    }

    protected open fun installSplashTheme() {
        Log.d(TAG, "ParentActivity: installSplashTheme: Success")
        installSplashScreen()
    }

    protected open fun enableMaterialDynamicTheme() {
        Log.d(TAG, "ParentActivity: enableMaterialDynamicTheme: Started")
        DynamicColors.applyToActivityIfAvailable(this)
    }

    protected open fun hideStatusBar(type: Int) {
        Log.d(TAG, "ParentActivity: hideStatusBar: type=$type")
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = when (type) {
                0 -> WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                else -> WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            when (type) {
                0 -> show(WindowInsetsCompat.Type.systemBars())
                1 -> hide(WindowInsetsCompat.Type.systemBars())
                2 -> hide(WindowInsetsCompat.Type.statusBars())
                3 -> hide(WindowInsetsCompat.Type.navigationBars())
                else -> hide(WindowInsetsCompat.Type.systemBars())
            }
        }
    }

    abstract fun onCreated()
}
