package kz.archimedes.scheduleservice.exception

data class ValidationException(override val message: String): Exception(message)
