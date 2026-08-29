package com.pal.starktest.domain.datasource

import com.pal.starktest.domain.model.User

/**
 * Remote data source. No real networking library is used per assessment spec - implementations
 * return hardcoded/mocked data.
 */
interface NetworkDataSource {
    suspend fun fetchUser(): User
}
