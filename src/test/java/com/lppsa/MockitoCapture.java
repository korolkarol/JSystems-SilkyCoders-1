package com.lppsa;

import org.mockito.ArgumentCaptor;

/**
 * Java bridge for Mockito ArgumentCaptor to avoid Kotlin null-safety issues.
 * Kotlin generates intrinsic null checks on non-null return types,
 * which causes NPE when Mockito's capture() returns null at matcher-setup time.
 */
public class MockitoCapture {
    @SuppressWarnings("unchecked")
    public static <T> T capture(ArgumentCaptor<T> captor) {
        return captor.capture();
    }
}
