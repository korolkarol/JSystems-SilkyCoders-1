package com.lppsa

import org.mockito.ArgumentCaptor

/**
 * Kotlin-safe capture helper for Mockito ArgumentCaptor.
 * Suppresses the null check that Kotlin generates for non-null types,
 * which would otherwise throw NPE before Mockito can intercept the call.
 */
@Suppress("UNCHECKED_CAST")
fun <T> ArgumentCaptor<T>.captureNonNull(): T = capture() as T
