package com.pal.starktest.data.remote

import com.pal.starktest.domain.datasource.NetworkDataSource
import com.pal.starktest.domain.model.User
import kotlinx.coroutines.delay

/**
 * Mock network implementation - no Retrofit/OkHttp per assessment spec. Returns hardcoded data
 * after a small simulated delay so callers exercise the same loading states as a real call.
 */
class NetworkDataSourceImpl : NetworkDataSource {
    override suspend fun fetchUser(): User {
        delay(300)
        return User(
            email = "rider@starkfuture.com",
            name = "Alex Rider",
            phone = "+34 600 123 456",
            country = "Spain",
        )
    }
}
