package kz.archimedes.scheduleservice.handler.`interface`

import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

interface IScheduleHandler {
    suspend fun findAllSchedules(request: ServerRequest): ServerResponse
    suspend fun findSchedule(request: ServerRequest): ServerResponse
    suspend fun createSchedule(request: ServerRequest): ServerResponse
    suspend fun createSpecialCaseDay(request: ServerRequest): ServerResponse
    suspend fun findScheduleHistory(request: ServerRequest): ServerResponse
    suspend fun createRelocation(request: ServerRequest): ServerResponse
    suspend fun deleteRelocation(request: ServerRequest): ServerResponse
}