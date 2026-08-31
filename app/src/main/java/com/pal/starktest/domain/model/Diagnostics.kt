package com.pal.starktest.domain.model

enum class WarningSeverity {
    INFO,
    WARNING,
    CRITICAL,
}

enum class FaultCode {
    MOTOR_OVERHEAT,
    SENSOR_FAILURE,
    UNKNOWN
}

data class Warning(
    val code: String,
    val message: String,
    val severity: WarningSeverity,
)

data class Diagnostics(
    val faultCodes: List<FaultCode>,
    val warnings: List<Warning>,
)
