package com.pal.starktest.domain.model

enum class WarningSeverity {
    INFO,
    WARNING,
    CRITICAL,
}

data class Warning(
    val code: String,
    val message: String,
    val severity: WarningSeverity,
)

data class Diagnostics(
    val faultCodes: List<String>,
    val warnings: List<Warning>,
)
