package kz.archimedes.scheduleservice.handler.`interface`

import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

interface IRelocationHandler {
    suspend fun createRelocation(request: ServerRequest): ServerResponse
    suspend fun chooseMedic(request: ServerRequest): ServerResponse
    suspend fun chooseRelocationData(request: ServerRequest): ServerResponse
    suspend fun finishRelocation(request: ServerRequest): ServerResponse
}