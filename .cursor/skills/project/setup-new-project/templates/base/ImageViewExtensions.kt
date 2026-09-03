package YOUR.PACKAGE.core.ui.extensions

import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

/**
 * Template — copy to `:core-ui` …/extensions/ImageViewExtensions.kt
 * Replace YOUR.PACKAGE with applicationId root.
 *
 * Works on `ImageView` and `ShapeableImageView` (subclass).
 *
 * Examples:
 *   sivFlag.loadImage(R.drawable.ic_svg_flag_en)
 *   sivAvatar.loadImage(url, placeholder = R.drawable.ic_placeholder, error = R.drawable.ic_broken)
 *   sivHero.loadImage(uri, cachePolicy = CachePolicy.NO_CACHE)
 */
fun ImageView.loadImage(
    source: Any?,
    crossFadeDuration: Int = 300,
    @DrawableRes placeholder: Int? = null,
    @DrawableRes error: Int? = null,
    cachePolicy: CachePolicy = CachePolicy.DEFAULT,
) {
    var request = Glide.with(this)
        .load(source)
        .transition(DrawableTransitionOptions.withCrossFade(crossFadeDuration))

    request = when (cachePolicy) {
        CachePolicy.CACHE_ONLY -> request.onlyRetrieveFromCache(true)
        CachePolicy.NETWORK_ONLY,
        CachePolicy.NO_CACHE,
            -> request.skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE)

        CachePolicy.DEFAULT -> request
    }

    placeholder?.let { request = request.placeholder(it) }
    error?.let { request = request.error(it) }

    request.into(this)
}

enum class CachePolicy {
    DEFAULT,
    CACHE_ONLY,
    NETWORK_ONLY,
    NO_CACHE,
}