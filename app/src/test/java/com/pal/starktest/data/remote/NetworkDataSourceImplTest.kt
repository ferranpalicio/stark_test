package com.pal.starktest.data.remote

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkDataSourceImplTest {

    private val dataSource = NetworkDataSourceImpl()

    @Test
    fun `fetchUser returns mocked user`() = runTest {
        val user = dataSource.fetchUser()

        assertEquals("rider@starkfuture.com", user.email)
        assertEquals("Alex Rider", user.name)
        assertEquals("+34 600 123 456", user.phone)
        assertEquals("Spain", user.country)
    }
}
