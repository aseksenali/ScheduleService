package kz.archimedes.scheduleservice.handler.`interface`

import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

interface IHolidayHandler {
    suspend fun createHoliday(request: ServerRequest): ServerResponse
    suspend fun findHolidays(request: ServerRequest): ServerResponse
    suspend fun deleteHoliday(request: ServerRequest): ServerResponse
}