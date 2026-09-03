package com.hypersoft.ads.practice.core.extensions

import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.withResumed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/* ---------------------------------------------- BackPress ---------------------------------------------- */

fun AppCompatActivity.onBackPressedDispatcher(callback: () -> Unit) {
    onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            callback.invoke()
        }
    })
}

fun <T> AppCompatActivity.collectWhenCreated(flow: Flow<T>, collector: suspend (T) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.CREATED) {
            flow.collect(collector)
        }
    }
}

fun <T> AppCompatActivity.collectWhenStarted(flow: Flow<T>, collector: suspend (T) -> Unit) {
    lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            flow.collect(collector)
        }
    }
}

fun AppCompatActivity.launchWhenResumed(callback: () -> Unit) {
    lifecycleScope.launch {
        lifecycle.withResumed(callback)
    }
}