package com.lppsa.domain

import com.lppsa.domain.model.RequestType
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RequestTypeTest {
    @Test
    fun `REKLAMACJA and ZWROT values exist`() {
        assertEquals(2, RequestType.entries.size)
    }

    @Test
    fun `valueOf round-trips correctly`() {
        assertEquals(RequestType.REKLAMACJA, RequestType.valueOf("REKLAMACJA"))
        assertEquals(RequestType.ZWROT, RequestType.valueOf("ZWROT"))
    }
}
